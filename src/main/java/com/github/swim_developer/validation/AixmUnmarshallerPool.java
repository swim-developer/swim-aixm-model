package com.github.swim_developer.validation;

import aero.aixm.message.AIXMBasicMessageType;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.JAXBIntrospector;
import jakarta.xml.bind.UnmarshalException;
import jakarta.xml.bind.Unmarshaller;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class AixmUnmarshallerPool {

    private static final String ROOT_XSD = "schemas/aixm-wrapper.xsd";
    private static final int POOL_SIZE = 200;

    private final JAXBContext jaxbContext;
    private final SAXParserFactory saxParserFactory;
    private final Schema schema;
    private final BlockingQueue<Unmarshaller> pool;

    public AixmUnmarshallerPool() {
        try {
            this.jaxbContext = JAXBContext.newInstance(AIXMBasicMessageType.class);
            this.saxParserFactory = buildSecureSaxParserFactory();
            this.schema = loadSchema();
            this.pool = buildPool();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize AixmUnmarshallerPool", e);
        }
    }

    public AIXMBasicMessageType unmarshalAndValidate(String xml) throws AixmUnmarshalException {
        if (xml == null || xml.isBlank()) {
            throw new AixmUnmarshalException("XML content is null or empty");
        }

        Unmarshaller u = pool.poll();
        if (u == null) {
            u = createUnmarshaller();
        }

        try (StringReader reader = new StringReader(xml)) {
            SAXSource source = new SAXSource(
                    saxParserFactory.newSAXParser().getXMLReader(),
                    new InputSource(reader));
            Object result = JAXBIntrospector.getValue(u.unmarshal(source));
            if (result instanceof AIXMBasicMessageType msg) {
                return msg;
            }
            throw new AixmUnmarshalException("Unexpected root element type: " + result.getClass().getName());
        } catch (UnmarshalException e) {
            throw new AixmUnmarshalException("XML validation failed: " + extractMessage(e), e);
        } catch (JAXBException | SAXException | ParserConfigurationException e) {
            throw new AixmUnmarshalException("XML unmarshal failed: " + extractMessage(e), e);
        } finally {
            pool.offer(u);
        }
    }

    @SuppressWarnings("java:S2755")
    private Schema loadSchema() throws SAXException {
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        factory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "file,resource");
        factory.setResourceResolver(new ClasspathResourceResolver());
        URL rootUrl = getClass().getClassLoader().getResource(ROOT_XSD);
        if (rootUrl == null) {
            throw new IllegalStateException("AIXM root XSD not found on classpath: " + ROOT_XSD);
        }
        try (InputStream is = rootUrl.openStream()) {
            return factory.newSchema(new StreamSource(is, rootUrl.toString()));
        } catch (java.io.IOException e) {
            throw new IllegalStateException("Failed to read AIXM root XSD: " + ROOT_XSD, e);
        }
    }

    private BlockingQueue<Unmarshaller> buildPool() throws JAXBException {
        BlockingQueue<Unmarshaller> q = new ArrayBlockingQueue<>(POOL_SIZE);
        for (int i = 0; i < POOL_SIZE; i++) {
            q.offer(createUnmarshaller());
        }
        return q;
    }

    private Unmarshaller createUnmarshaller() {
        try {
            Unmarshaller u = jaxbContext.createUnmarshaller();
            u.setSchema(schema);
            return u;
        } catch (JAXBException e) {
            throw new IllegalStateException("Failed to create Unmarshaller", e);
        }
    }

    private static String extractMessage(Exception e) {
        if (e.getMessage() != null) {
            return e.getMessage();
        }
        if (e instanceof JAXBException jaxb && jaxb.getLinkedException() != null) {
            return jaxb.getLinkedException().getMessage();
        }
        if (e.getCause() != null && e.getCause().getMessage() != null) {
            return e.getCause().getMessage();
        }
        return e.getClass().getSimpleName();
    }

    private static SAXParserFactory buildSecureSaxParserFactory() {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        try {
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            factory.setXIncludeAware(false);
        } catch (ParserConfigurationException | SAXException e) {
            throw new IllegalStateException("Failed to configure secure SAX parser", e);
        }
        return factory;
    }

    public static class AixmUnmarshalException extends Exception {
        public AixmUnmarshalException(String message) {
            super(message);
        }

        public AixmUnmarshalException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    private static class ClasspathResourceResolver implements LSResourceResolver {

        @Override
        public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI) {
            if (systemId == null) {
                return null;
            }
            if (systemId.startsWith("http://") || systemId.startsWith("https://")) {
                return null;
            }

            String classpathPath = resolveClasspathPath(systemId, baseURI);
            if (classpathPath == null) {
                return null;
            }

            URL resourceUrl = AixmUnmarshallerPool.class.getClassLoader().getResource(classpathPath);
            if (resourceUrl == null) {
                return null;
            }

            try {
                return new StreamLSInput(publicId, resourceUrl.toString(), resourceUrl.openStream());
            } catch (java.io.IOException e) {
                return null;
            }
        }

        private String resolveClasspathPath(String systemId, String baseURI) {
            try {
                if (baseURI == null) {
                    return systemId.startsWith("/") ? systemId.substring(1) : systemId;
                }

                String basePath;
                if (baseURI.contains("!/")) {
                    basePath = baseURI.substring(baseURI.indexOf("!/") + 2);
                } else if (baseURI.startsWith("file:")) {
                    basePath = URI.create(baseURI).getPath();
                    if (basePath.startsWith("/")) {
                        basePath = basePath.substring(1);
                    }
                } else {
                    basePath = baseURI;
                }

                String baseDir = basePath.contains("/")
                        ? basePath.substring(0, basePath.lastIndexOf('/') + 1)
                        : "";

                String normalized = URI.create(baseDir + systemId).normalize().toString();
                return normalized.startsWith("/") ? normalized.substring(1) : normalized;
            } catch (Exception e) {
                return null;
            }
        }
    }

    private static class StreamLSInput implements LSInput {
        private final String publicId;
        private final String systemId;
        private final InputStream inputStream;

        StreamLSInput(String publicId, String systemId, InputStream inputStream) {
            this.publicId = publicId;
            this.systemId = systemId;
            this.inputStream = inputStream;
        }

        @Override public InputStream getByteStream() { return inputStream; }
        @Override public String getPublicId() { return publicId; }
        @Override public String getSystemId() { return systemId; }
        @Override public String getBaseURI() { return null; }
        @Override public java.io.Reader getCharacterStream() { return null; }
        @Override public String getStringData() { return null; }
        @Override public String getEncoding() { return null; }
        @Override public boolean getCertifiedText() { return false; }
        @Override public void setByteStream(InputStream is) { throw new UnsupportedOperationException(); }
        @Override public void setCharacterStream(java.io.Reader r) { throw new UnsupportedOperationException(); }
        @Override public void setStringData(String s) { throw new UnsupportedOperationException(); }
        @Override public void setSystemId(String id) { throw new UnsupportedOperationException(); }
        @Override public void setPublicId(String id) { throw new UnsupportedOperationException(); }
        @Override public void setBaseURI(String uri) { throw new UnsupportedOperationException(); }
        @Override public void setEncoding(String enc) { throw new UnsupportedOperationException(); }
        @Override public void setCertifiedText(boolean b) { throw new UnsupportedOperationException(); }
    }
}
