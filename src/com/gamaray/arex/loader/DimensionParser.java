package com.gamaray.arex.loader;

import java.util.ArrayList;
import java.util.Collection;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.gamaray.arex.model.Asset;
import com.gamaray.arex.model.Dimension;
import com.gamaray.arex.model.Feature;
import com.gamaray.arex.model.Feature3D;
import com.gamaray.arex.model.FeatureImg;
import com.gamaray.arex.model.FeatureTxt;
import com.gamaray.arex.model.Location;
import com.gamaray.arex.model.Overlay;
import com.gamaray.arex.model.OverlayImg;
import com.gamaray.arex.model.OverlayTxt;

public class DimensionParser {

    private static String getStringValue(Element element, String label) {
        if (element.hasAttribute(label)) {
            return element.getAttribute(label);
        } else {
            String[] labelPath = label.split("/");
            if (labelPath.length > 1) {
                Element childElement = getFirstChildElement(element, labelPath[0]);
                if (childElement != null) {
                    return getStringValue(childElement, label.substring(labelPath[0].length() + 1));
                }
            } else {
                Element childElement = getFirstChildElement(element, label);
                if (childElement != null) {
                    return getTextContent(childElement);
                }
            }
        }
        return null;
    }

    private static String getTextContent(Node node){
        if (node.hasChildNodes()){
            Node child = node.getFirstChild();
            StringBuffer result =new StringBuffer();
            while (child!=null){
                result.append(getTextContent(child));
                child = child.getNextSibling();
            }
            return result.toString();
        } else {
            return node.getNodeValue();
        }
    }


    private static Element getFirstChildElement(Element element, String label) {
        NodeList nodeList = element.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element childElement = ((Element) node);
                if (childElement.getTagName().trim().equalsIgnoreCase(label)) {
                    return childElement;
                }
            }
        }
        return null;
    }

    private static Collection<Element> getChildrenElements(Node root, String label) {
        NodeList nodeList = root.getChildNodes();
        Collection<Element> result = new ArrayList<Element>();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element childElement = ((Element) node);
                if (childElement.getTagName().trim().equalsIgnoreCase(label)) {
                    result.add(childElement);
                }
            }
        }
        return result;
    }

    private static Collection<Element> getAllChildElements(Node root) {
        NodeList nodeList = root.getChildNodes();
        Collection<Element> result = new ArrayList<Element>();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                result.add((Element) node);
            }
        }
        return result;
    }

    private static Collection<Element> getGrandChildrenElements(Element element, String childLabel,
            String grandChildLabel) {
        Collection<Element> result = new ArrayList<Element>();
        Collection<Element> children = getChildrenElements(element, childLabel);
        for (Node childNode : children) {
            result.addAll(getChildrenElements(childNode, grandChildLabel));
        }
        return result;
    }

    private static Collection<Element> getGrandChildrenElements(Element element, String childLabel) {
        Collection<Element> result = new ArrayList<Element>();
        Collection<Element> children = getChildrenElements(element, childLabel);
        for (Node childNode : children) {
            result.addAll(getAllChildElements(childNode));
        }
        return result;
    }

    private static Boolean getBooleanValue(Element element, String label) {
        String value = getStringValue(element, label);
        if (value != null) {
            return Boolean.valueOf(value);
        }
        return null;
    }

    private static Integer getIntegerValue(Element element, String label) {
        String value = getStringValue(element, label);
        if (value != null) {
            return Integer.valueOf(value);
        }
        return null;
    }

    private static Double getDoubleValue(Element element, String label) {
        String value = getStringValue(element, label);
        if (value != null) {
            return Double.valueOf(value);
        }
        return null;
    }

    private static Float getFloatValue(Element element, String label) {
        String value = getStringValue(element, label);
        if (value != null) {
            return Float.valueOf(value);
        }
        return null;
    }

    public static Dimension loadDimension(Element rootElement) {
        final String name = getStringValue(rootElement, "name");
        final String version = getStringValue(rootElement, "version");
        final Boolean relativeAltitude = getBooleanValue(rootElement, "relativeAltitude");
        final Boolean radarAvailable = getBooleanValue(rootElement, "radarAvailable");
        final String refreshUrl = getStringValue(rootElement, "refreshUrl");
        final Boolean waitForAssets = getBooleanValue(rootElement, "waitForAssets");

        final Integer refreshTime = getIntegerValue(rootElement, "refreshTime/validFor");
        final Integer refreshDistance = getIntegerValue(rootElement, "refreshDistance/validWithinRange");
        final String playSoundUrl = getStringValue(rootElement, "playSound/url");
        final Integer radarRange = getIntegerValue(rootElement, "radarRange");

        final Collection<Location> locations = loadLocations(getGrandChildrenElements(rootElement, "locations",
                "location"));
        final Collection<Asset> assets = loadAssets(getGrandChildrenElements(rootElement, "assets", "asset"));
        final Collection<Feature> features = loadFeatures(getGrandChildrenElements(rootElement, "features"));
        final Collection<Overlay> overlays = loadOverlays(getGrandChildrenElements(rootElement, "overlays"));

        return new Dimension(name, version, playSoundUrl, relativeAltitude, radarAvailable, waitForAssets, refreshUrl,
                refreshTime, refreshDistance, radarRange, locations, assets, features, overlays);
    }

    private static Collection<Overlay> loadOverlays(Collection<Element> overlayElements) {
        Collection<Overlay> result = new ArrayList<Overlay>();
        for (Element element : overlayElements) {
            if (element.getTagName().trim().equalsIgnoreCase("overlayImg")) {
                result.add(createOverlayImg(element));
            } else if (element.getTagName().trim().equalsIgnoreCase("overlayTxt")) {
                result.add(createOverlayTxt(element));
            }
        }
        return result;
    }

    private static Overlay createOverlayTxt(Element element) {
        String anchor = getStringValue(element, "anchor");
        String id = getStringValue(element, "id");
        String onPress = getStringValue(element, "onPress");
        Boolean hidden = getBooleanValue(element, "hidden");
        Float x = getFloatValue(element, "x");
        Float y = getFloatValue(element, "y");
        Float width = getFloatValue(element, "width");
        String text = getStringValue(element, "text");
        return new OverlayTxt(id, anchor, x, y, onPress, hidden, text, width);
    }

    private static Overlay createOverlayImg(Element element) {
        String id = getStringValue(element, "id");
        String anchor = getStringValue(element, "anchor");
        String onPress = getStringValue(element, "onPress");
        Boolean hidden = getBooleanValue(element, "hidden");
        Float x = getFloatValue(element, "x");
        Float y = getFloatValue(element, "y");
        String assetId = getStringValue(element, "assetId");
        return new OverlayImg(id, anchor, x, y, onPress, hidden, assetId);
    }

    private static Collection<Feature> loadFeatures(Collection<Element> featureElements) {
        Collection<Feature> result = new ArrayList<Feature>();
        for (Element element : featureElements) {
            if (element.getTagName().trim().equalsIgnoreCase("feature3d")) {
                result.add(createFeature3d(element));
            } else if (element.getTagName().trim().equalsIgnoreCase("featureTxt")) {
                result.add(createFeatureTxt(element));
            } else if (element.getTagName().trim().equalsIgnoreCase("featureImg")) {
                result.add(createFeatureImg(element));
            }
        }
        return result;
    }

    private static Feature createFeature3d(Element element) {
        String id = getStringValue(element, "id");
        Boolean showInRadar = getBooleanValue(element, "showInRadar");
        Location location = null;
        Collection<Element> locationElements = getChildrenElements(element, "location");
        if (locationElements.size() == 1) {
            location = createLocation(locationElements.iterator().next());
        }
        String onPress = getStringValue(element, "onPress");
        String showOnFocus = getStringValue(element, "showOnFocus");
        String assetId = getStringValue(element, "assetId");
        Float scale = getFloatValue(element, "scale");
        Float xRot = getFloatValue(element, "xRot");
        Float yRot = getFloatValue(element, "yRot");
        Float zRot = getFloatValue(element, "zRot");

        if (location != null) {
            return new Feature3D(id, showInRadar, location, onPress, showOnFocus, assetId, scale, xRot, yRot, zRot);
        } else {
            String locationId = getStringValue(element, "locationId");
            Float xLoc = getFloatValue(element, "xLoc");
            Float yLoc = getFloatValue(element, "yLoc");
            Float zLoc = getFloatValue(element, "zLoc");
            return new Feature3D(id, showInRadar, locationId, xLoc, yLoc, zLoc, onPress, showOnFocus, assetId, scale,
                    xRot, yRot, zRot);
        }
    }

    private static Feature createFeatureImg(Element element) {
        String id = getStringValue(element, "id");
        Boolean showInRadar = getBooleanValue(element, "showInRadar");
        String showOnFocus = getStringValue(element, "showOnFocus");

        Location location = null;
        Collection<Element> locationElements = getChildrenElements(element, "location");
        if (locationElements.size() == 1) {
            location = createLocation(locationElements.iterator().next());
        }
        String onPress = getStringValue(element, "onPress");
        String assetId = getStringValue(element, "assetId");
        String anchor = getStringValue(element, "anchor");

        if (location != null) {
            return new FeatureImg(id, showInRadar, location, onPress, showOnFocus, assetId, anchor);
        } else {
            String locationId = getStringValue(element, "locationId");
            Float xLoc = getFloatValue(element, "xLoc");
            Float yLoc = getFloatValue(element, "yLoc");
            Float zLoc = getFloatValue(element, "zLoc");
            return new FeatureImg(assetId, showInRadar, locationId, xLoc, yLoc, zLoc, onPress, showOnFocus, assetId,
                    anchor);
        }
    }

    private static Feature createFeatureTxt(Element element) {
        String id = getStringValue(element, "id");
        Boolean showInRadar = getBooleanValue(element, "showInRadar");
        String showOnFocus = getStringValue(element, "showOnFocus");
        Location location = null;
        Collection<Element> locationElements = getChildrenElements(element, "location");
        if (locationElements.size() == 1) {
            location = createLocation(locationElements.iterator().next());
        }
        String onPress = getStringValue(element, "onPress");
        String anchor = getStringValue(element, "anchor");
        String text = getStringValue(element, "text");

        if (location != null) {
            return new FeatureTxt(id, showInRadar, location, onPress, showOnFocus, text, anchor);
        } else {
            String locationId = getStringValue(element, "locationId");
            Float xLoc = getFloatValue(element, "xLoc");
            Float yLoc = getFloatValue(element, "yLoc");
            Float zLoc = getFloatValue(element, "zLoc");
            return new FeatureTxt(id, showInRadar, locationId, xLoc, yLoc, zLoc, onPress, showOnFocus, text, anchor);
        }
    }

    private static Collection<Asset> loadAssets(Collection<Element> assetElements) {
        Collection<Asset> result = new ArrayList<Asset>();
        for (Element element : assetElements) {
            String id = getStringValue(element, "id");
            String format = getStringValue(element, "format");
            String url = getStringValue(element, "url");
            result.add(new Asset(id, format, url));
        }
        return result;
    }

    private static Collection<Location> loadLocations(Collection<Element> locationElements) {
        Collection<Location> result = new ArrayList<Location>();
        for (Element element : locationElements) {
            result.add(createLocation(element));
        }
        return result;
    }

    private static Location createLocation(Element element) {
        String id = getStringValue(element, "id");
        Double lat = getDoubleValue(element, "lat");
        Double lon = getDoubleValue(element, "lon");
        Double alt = getDoubleValue(element, "alt");
        return new Location(id, lat, lon, alt);
    }

}
