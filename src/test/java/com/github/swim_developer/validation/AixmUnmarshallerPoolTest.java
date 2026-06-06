package com.github.swim_developer.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class AixmUnmarshallerPoolTest {

    private static final String XML_EXTENSION = ".xml";
    private AixmUnmarshallerPool pool;

    @BeforeEach
    void setUp() {
        pool = new AixmUnmarshallerPool();
    }

    @Test
    void shouldUnmarshalRandomDnotamXml() throws Exception {
        String xmlContent = loadRandomXmlFromResources();

        assertThat(xmlContent).isNotEmpty();

        assertThatCode(() -> {
            var result = pool.unmarshalAndValidate(xmlContent);
            assertThat(result).isNotNull();
            assertThat(result.getId()).isNotNull();
        }).doesNotThrowAnyException();
    }

    @Test
    void shouldUnmarshalAllDnotamXmls() throws Exception {
        List<String> xmlFiles = listAllXmlFiles();

        assertThat(xmlFiles).isNotEmpty();

        for (String xmlFile : xmlFiles) {
            String xmlContent = loadXmlFromResources(xmlFile);

            assertThatCode(() -> {
                var result = pool.unmarshalAndValidate(xmlContent);
                assertThat(result).isNotNull();
                assertThat(result.getId()).isNotNull();
            }).as("Unmarshal failed for file: %s", xmlFile)
              .doesNotThrowAnyException();
        }
    }

    @Test
    void shouldThrowExceptionForNullInput() {
        assertThatCode(() -> pool.unmarshalAndValidate(null))
                .isInstanceOf(AixmUnmarshallerPool.AixmUnmarshalException.class)
                .hasMessageContaining("null or empty");
    }

    @Test
    void shouldThrowExceptionForBlankInput() {
        assertThatCode(() -> pool.unmarshalAndValidate("  "))
                .isInstanceOf(AixmUnmarshallerPool.AixmUnmarshalException.class)
                .hasMessageContaining("null or empty");
    }

    private String loadRandomXmlFromResources() throws Exception {
        List<String> xmlFiles = listAllXmlFiles();
        assertThat(xmlFiles).isNotEmpty();

        String randomFile = xmlFiles.get(new Random().nextInt(xmlFiles.size()));
        System.out.println("Testing with random XML: " + randomFile);

        return loadXmlFromResources(randomFile);
    }

    private List<String> listAllXmlFiles() throws IOException, URISyntaxException {
        URI resourceUri = getClass().getClassLoader().getResource("").toURI();
        Path resourcePath = Paths.get(resourceUri);

        return Files.walk(resourcePath)
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(XML_EXTENSION))
                .map(path -> resourcePath.relativize(path).toString())
                .toList();
    }

    private String loadXmlFromResources(String fileName) throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(fileName)) {
            if (is == null) {
                throw new IOException("XML file not found: " + fileName);
            }
            return new String(is.readAllBytes());
        }
    }
}
