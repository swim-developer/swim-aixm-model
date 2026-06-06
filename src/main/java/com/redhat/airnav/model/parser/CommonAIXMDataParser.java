package com.redhat.airnav.model.parser;

import com.redhat.airnav.model.CommonAIXMData;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

public class CommonAIXMDataParser {

    private CommonAIXMDataParser() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static CommonAIXMData parse(String xml) throws AIXMParseException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);
            DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new InputSource(new StringReader(xml)));

        XPath xpath = XPathFactory.newInstance().newXPath();
        xpath.setNamespaceContext(new AIXMNamespaceContext());

        CommonAIXMData data = new CommonAIXMData();

        Node scenarioNode = (Node) xpath.evaluate("//event:scenario", doc, XPathConstants.NODE);
        Node eventTimeSlice = scenarioNode.getParentNode();

        data.setId(xpath.evaluate("@gml:id", eventTimeSlice));
        data.setScenario(xpath.evaluate("event:scenario/text()", eventTimeSlice));
        data.setInterpretation(xpath.evaluate("aixm:interpretation/text()", eventTimeSlice));
        String sequenceNumber = xpath.evaluate("aixm:sequenceNumber/text()", eventTimeSlice);
        if (!sequenceNumber.isEmpty()) {
            data.setSequenceNumber(Integer.parseInt(sequenceNumber));
        }

        NodeList notificationNodes = (NodeList) xpath.evaluate("//event:notification", eventTimeSlice, XPathConstants.NODESET);

        for (int i = 0; i < notificationNodes.getLength(); i++) {
            Node notifNode = notificationNodes.item(i);

            Map<String, String> notifMap = new HashMap<>();

            Node notamNode = (Node) xpath.evaluate("event:NOTAM", notifNode, XPathConstants.NODE);
            if (notamNode != null) {
                notifMap.put("id", xpath.evaluate("@gml:id", notamNode));
                NodeList fields = notamNode.getChildNodes();
                for (int j = 0; j < fields.getLength(); j++) {
                    Node field = fields.item(j);
                    if (field.getNodeType() == Node.ELEMENT_NODE) {
                        String tag = field.getLocalName();
                        String value = field.getTextContent().trim();
                        notifMap.put(tag, value);
                    }
                }
            }

            data.getNotifications().add(notifMap);
        }

        return data;
        } catch (Exception e) {
            throw new AIXMParseException("Failed to parse AIXM XML", e);
        }
    }
}