package com.redhat.airnav.model.parser;

import javax.xml.namespace.NamespaceContext;
import java.util.Iterator;

public class AIXMNamespaceContext implements NamespaceContext {
    public String getNamespaceURI(String prefix) {
        return switch (prefix) {
            case "gml" -> "http://www.opengis.net/gml/3.2";
            case "aixm" -> "http://www.aixm.aero/schema/5.1.1";
            case "event" -> "http://www.aixm.aero/schema/5.1.1/event";
            case "xsi" -> "http://www.w3.org/2001/XMLSchema-instance";
            default -> null;
        };
    }

    public String getPrefix(String uri) {
        return null;
    }

    public Iterator getPrefixes(String uri) {
        return null;
    }
}
