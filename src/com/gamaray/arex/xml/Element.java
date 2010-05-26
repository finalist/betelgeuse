/*
    This file is part of betelgeuse.

    betelgeuse is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    betelgeuse is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with betelgeuse.  If not, see <http://www.gnu.org/licenses/>.

 */
package com.gamaray.arex.xml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Element {

    private String name;
    private String content;
    private Map<String, String> attribs = new HashMap<String, String>();

    private List<NonRootElement> childElements = new ArrayList<NonRootElement>();


	public NonRootElement getChildElement(String name) {
        // TODO: If this method is used a lot and if the order is not important,
        // it might be better to use a hashmap to store the childelements.
        for (NonRootElement childElement : childElements) {
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
        NonRootElement e = (NonRootElement) getChildElement(name);
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

    
    protected void print(StringBuffer buf) {
        if (!isRoot()) {
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

        for (NonRootElement childElement : childElements) {
            childElement.print(buf);
        }

        if (!isRoot()) {
            buf.append("</" + name + ">\n");
        }
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getAttribute(Object key) {
        return attribs.get(key);
    }

    public String putAttribute(String key, String value) {
        return attribs.put(key, value);
    }

    public void addChildElement(NonRootElement child) {
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

    public List<NonRootElement> getChildElements() {
        return childElements;
    }

    public String getName() {
        return name;
    }
    
	public abstract boolean isRoot();
}
