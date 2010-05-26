package com.gamaray.arex.xml;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import junit.framework.TestCase;

import org.junit.Test;
import org.xml.sax.SAXException;

public class XMLParseTest extends TestCase {

    @Test
    public void testXML() throws SAXException, ParserConfigurationException, IOException {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        SAXParser sp = spf.newSAXParser();
        AndroidXMLHandler handler = new AndroidXMLHandler();
        InputStream is = this.getClass().getResourceAsStream("test.xml");
        sp.parse(is, handler);
        Element dimensionElement=handler.getRoot().getChildElements().get(0);
        Element nameElement=dimensionElement.getChildElements().get(0);
        assertEquals("Stefaan Dimension",nameElement.getContent());
    }

}
