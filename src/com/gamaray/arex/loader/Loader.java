package com.gamaray.arex.loader;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class Loader {

    public static Element readXML(String urlString) {

        String correctedUrl = "" + urlString;

        if (correctedUrl.startsWith("gamaray://")) {
            correctedUrl.replaceFirst("gamaray", "http");
        }

        try {
            URL url = new URL(correctedUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(10000);
            return readXML(conn.getInputStream());
        } catch (MalformedURLException e) {
            // TODO Create logging
        } catch (IOException e) {
            // TODO Auto-generated catch block
        } finally {
            // TODO : close connection ?
        }
        return null;

    }

    public static Element readXML(InputStream inputStream) {

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document dom = builder.parse(inputStream);
            Element root = dom.getDocumentElement();
            return root;
        } catch (IOException e) {
            // TODO Auto-generated catch block
        } catch (ParserConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SAXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return null;

    }


}
