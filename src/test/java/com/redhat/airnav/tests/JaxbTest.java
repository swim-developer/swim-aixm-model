package com.redhat.airnav.tests;


import aero.aixm.AbstractAIXMFeatureType;
import aero.aixm.AirportHeliportType;
import aero.aixm.message.AIXMBasicMessageType;
import aero.aixm.message.BasicMessageMemberAIXMPropertyType;
import jakarta.xml.bind.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.util.Optional;

class JaxbTest extends BaseAixmTest {

    @Test
    void testJaxbConversion() throws JAXBException, IOException {
        JAXBContext jaxbContext = JAXBContext.newInstance(AIXMBasicMessageType.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        String xml = Files.readString(new File(TEST_RESOURCES, AIXM_SAMPLE_FILE).toPath());
        Assertions.assertTrue(xml.contains("<aixm:AirportHeliport gml:id=\"uuid.1b54b2d6-a5ff-4e57-94c2-f4047a381c64\">"));
        try (StringReader reader = new StringReader(xml)) {
            AIXMBasicMessageType message = (AIXMBasicMessageType) JAXBIntrospector.getValue(unmarshaller.unmarshal(reader));
            Assertions.assertNotNull(message);
            Assertions.assertEquals("M00001", message.getId());
            Optional<BasicMessageMemberAIXMPropertyType> firstMessageMember = message.getHasMember().stream().findFirst();
            Assertions.assertTrue(firstMessageMember.isPresent());
            BasicMessageMemberAIXMPropertyType firstMember = firstMessageMember.get();
            JAXBElement<? extends AbstractAIXMFeatureType> abstractAIXMFeature = firstMember.getAbstractAIXMFeature();
            Assertions.assertInstanceOf(AirportHeliportType.class, abstractAIXMFeature.getValue());
            AirportHeliportType airportHeliport = (AirportHeliportType) abstractAIXMFeature.getValue();
            airportHeliport.getTimeSlice().forEach(ts -> {
                Assertions.assertEquals("TEMPDELTA", ts.getAirportHeliportTimeSlice().getInterpretation());
            });
        } catch (JAXBException e) {
            Assertions.fail("Failed to unmarshal message", e);
        }

    }
}
