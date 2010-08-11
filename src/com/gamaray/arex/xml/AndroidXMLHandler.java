package com.gamaray.arex.xml;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

public class AndroidXMLHandler extends DefaultHandler {
    private Element root = new Element();
    private Element cur;

    public AndroidXMLHandler() {
        root.setParentElement(null);
        root.setRoot(true);
        cur = root;
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        Element newElem = new Element();
        newElem.setName(localName);
        newElem.setParentElement(cur);
        for (int i = 0; i < attributes.getLength(); i++) {
            newElem.putAttribute(attributes.getLocalName(i), attributes.getValue(i));
        }

        cur.addChildElement(newElem);
        cur = newElem;
    }

    public void endElement(String uri, String localName, String qName) {
        cur = cur.getParentElement();
    }

    public void characters(char[] ch, int start, int length) {
        cur.addContent(String.valueOf(ch, start, length));
    }

    public Element getRoot() {
        return root;
    }
}