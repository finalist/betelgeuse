package com.gamaray.arex.xml;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

public class AndroidXMLHandler extends DefaultHandler {
    private RootElement root = new RootElement();
    private Element cur;

    public AndroidXMLHandler() {
        cur = root;
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        NonRootElement newElem = new NonRootElement();
        newElem.setName(localName);
        newElem.setParentElement(cur);
        for (int i = 0; i < attributes.getLength(); i++) {
            newElem.putAttribute(attributes.getLocalName(i), attributes.getValue(i));
        }

        cur.addChildElement(newElem);
        cur = newElem;
    }

    public void endElement(String uri, String localName, String qName) {
    	if(cur instanceof RootElement) {
    		cur = null;
    	} else {
    		cur = ((NonRootElement)cur).getParentElement();
    	}
    }

    public void characters(char[] ch, int start, int length) {
        cur.addContent(String.valueOf(ch, start, length));
    }

    public RootElement getRoot() {
        return root;
    }
}