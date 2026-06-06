package com.redhat.airnav.tests;


import com.redhat.airnav.model.CommonAIXMData;
import com.redhat.airnav.model.ScenarioCode;
import com.redhat.airnav.model.parser.CommonAIXMDataParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class XMLToPojoConverterTest extends BaseAixmTest {

    @Test
    void testXMLConversionSpecificXML() throws Exception {
        Assertions.assertTrue(TEST_RESOURCES.exists());
        File xmlFile = new File(TEST_RESOURCES, AIXM_SAMPLE_FILE);
        Assertions.assertTrue(xmlFile.exists());
        String xml = Files.readString(xmlFile.toPath());
        CommonAIXMData data = CommonAIXMDataParser.parse(xml);

        assertThat(data).isNotNull();
        assertThat(data.getId()).isNotNull().isNotEmpty().isEqualTo("IDE_ACT_81");
        assertThat(data.getScenario()).isNotNull().isNotEmpty().isEqualTo("AD.CLS");
        assertThatCode(()->ScenarioCode.fromCode(data.getScenario())).doesNotThrowAnyException();
        assertThat(data.getInterpretation()).isNotNull().isNotEmpty().isEqualTo("BASELINE");
        assertThat(data.getSequenceNumber()).isNotNull().isEqualTo(1);

        List<Map<String, String>> notifications = data.getNotifications();
        assertThat(notifications).isNotNull().isNotEmpty().hasSize(2);
        assertThat(notifications.getFirst())
                .containsKeys(
                        "series",
                        "number",
                        "year",
                        "type",
                        "affectedFIR",
                        "selectionCode",
                        "traffic",
                        "purpose",
                        "scope",
                        "minimumFL",
                        "maximumFL",
                        "coordinates",
                        "radius",
                        "location",
                        "effectiveStart",
                        "effectiveEnd",
                        "schedule",
                        "text"
                )
                .containsEntry("id", "IDE_ACT_84")
                .containsEntry("series", "B")
                .containsEntry("number", "0214")
                .containsEntry("year", "2022")
                .containsEntry("type", "N")
                .containsEntry("affectedFIR", "EAAD")
                .containsEntry("selectionCode", "QFALC")
                .containsEntry("traffic", "IV")
                .containsEntry("purpose", "NBO")
                .containsEntry("scope", "A")
                .containsEntry("minimumFL", "000")
                .containsEntry("maximumFL", "999")
                .containsEntry("coordinates", "5222N03157W")
                .containsEntry("radius", "005")
                .containsEntry("location", "EADD")
                .containsEntry("effectiveStart", "2202011100")
                .containsEntry("effectiveEnd", "2202101300")
                .containsEntry("schedule", "Daily 1100-1300")
                .containsEntry("text", "AD closed.");

        assertThat(notifications.getLast())
                .containsEntry("id", "IDE_ACT_85")
                .containsEntry("series", "B")
                .containsEntry("number", "0288")
                .containsEntry("year", "2022")
                .containsEntry("type", "C")
                .containsEntry("referredSeries", "B")
                .containsEntry("referredNumber", "0214")
                .containsEntry("referredYear", "2022")
                .containsEntry("affectedFIR", "EAAD")
                .containsEntry("selectionCode", "QFAAK")
                .containsEntry("traffic", "IV")
                .containsEntry("purpose", "NBO")
                .containsEntry("scope", "A")
                .containsEntry("minimumFL", "000")
                .containsEntry("maximumFL", "999")
                .containsEntry("coordinates", "5222N03157W")
                .containsEntry("radius", "005")
                .containsEntry("location", "EADD")
                .containsEntry("effectiveStart", "2202081200")
                .containsEntry("text", "AD resumed normal operations");

    }

    @Test
    void testXMLConversionAllXMLs() {
        Assertions.assertTrue(TEST_RESOURCES.exists());
        File[] xmls = TEST_RESOURCES.listFiles((dir, name) -> name.endsWith(XML_EXTENSION));
        assertThat(xmls).isNotNull().hasSizeGreaterThan(0);
        Stream.of(xmls).forEach(xmlFile -> {
            try {

                Assertions.assertTrue(xmlFile.exists());
                String xml = Files.readString(xmlFile.toPath());
                CommonAIXMData data = CommonAIXMDataParser.parse(xml);

                assertThat(data).isNotNull();
                assertThat(data.getId()).isNotNull().isNotEmpty();
                assertThat(data.getScenario()).isNotNull().isNotEmpty();
                assertThatCode(()->ScenarioCode.fromCode(data.getScenario())).doesNotThrowAnyException();
                assertThat(data.getInterpretation()).isNotNull().isNotEmpty();
                assertThat(data.getSequenceNumber()).isNotNull();
                assertThat(data.getNotifications()).isNotNull().isNotEmpty();

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}
