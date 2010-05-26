package com.gamaray.arex.xml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NonRootElement extends Element {


    private Element parentElement;


    public Element getParentElement() {
		return parentElement;
	}
    
    public void setParentElement(Element parentElement) {
        this.parentElement = parentElement;
    }

    public boolean isRoot() {
        return false;
    }


}
