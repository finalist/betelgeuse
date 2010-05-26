package com.gamaray.arex.xml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Element {
    private boolean isRoot = false;
    private String name;
    private String content;
    private Map<String, String> attribs = new HashMap<String, String>();

    private Element parentElement;
    private List<Element> childElements = new ArrayList<Element>();

    public Element getChildElement(String name) {
        // TODO: If this method is used a lot and if the order is not important,
        // it might be better to use a hashmap to store the childelements.
        for (Element childElement : childElements) {
            if (childElement.getName().equals(name)) {
                return childElement;
            }

        }
        return null;
    }

    public String getChildElementValue(String name) {
        return getChildElementValue(name, null);
    }

    public String getChildElementValue(String name, String defaultValue) {
        Element e = (Element) getChildElement(name);
        return e.getAttribValue(name, defaultValue);
    }

    public String getAttribValue(String name) {
        return getAttribValue(name, null);
    }

    public String getAttribValue(String name, String defaultValue) {
        String value = (String) attribs.get(name);

        if (value != null) {
            return value;
        } else {
            return defaultValue;
        }
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        print(buf);

        return buf.toString();
    }

    private void print(StringBuffer buf) {
        if (!isRoot) {
            buf.append("<" + name + " ");
            if (attribs.size() > 0) {
                for (String attribName : attribs.keySet()) {
                    String attribValue = (String) attribs.get(attribName);
                    buf.append(attribName + "=\"" + attribValue + "\"");
                    buf.append(" ");
                }
                // TODO:Maybe remove the unnecesarry empty space at the end.
            }
            buf.append(">\n");

            if (content != null && content.trim().length() > 0){
                buf.append(content.trim() + "\n");
            }
        }

        for (Element childElement : childElements) {
            childElement.print(buf);
        }

        if (!isRoot) {
            buf.append("</" + name + ">\n");
        }
    }

    public void setParentElement(Element parentElement) {
        this.parentElement = parentElement;
    }

    public boolean isRoot() {
        return isRoot;
    }

    public void setRoot(boolean isRoot) {
        this.isRoot = isRoot;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Element getParentElement() {
        return parentElement;
    }

    public String getAttribute(Object key) {
        return attribs.get(key);
    }

    public String putAttribute(String key, String value) {
        return attribs.put(key, value);
    }

    public void addChildElement(Element child) {
        childElements.add(child);
    }

    public String getContent() {
        return content;
    }

    public void addContent(String content) {
        if (this.content == null) {
            this.content = content;
        } else {
            this.content += content;
        }
    }

    public List<Element> getChildElements() {
        return childElements;
    }

    public String getName() {
        return name;
    }
}
