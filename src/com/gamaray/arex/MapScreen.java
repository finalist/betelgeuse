package com.gamaray.arex;

import java.util.ArrayList;
import java.util.List;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.gamaray.arex.databus.DataBus;
import com.gamaray.arex.databus.DataChannel;
import com.gamaray.arex.eventbus.DataChangeEvent;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.OverlayItem;

public class MapScreen extends MapActivity implements DataChangeEvent.Handler {

    private PlacemarkersOverLay placemarkersOverLay;
    private MyLocationOverlay myLocationOverlay;
    private MapView mapView = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mapview);
        mapView = (MapView) findViewById(R.id.mapview);
        mapView.setBuiltInZoomControls(true);

        myLocationOverlay = new MyLocationOverlay(this, mapView);
        mapView.getOverlays().add(myLocationOverlay);
        updateLocations();
        DataBus.instance().registerListener(DataChannel.LAYER, this);

    }

    @Override
    public void onResume() {
        super.onResume();
        myLocationOverlay.enableMyLocation();
    }

    @Override
    public void onPause() {
        super.onPause();

        myLocationOverlay.disableMyLocation();
    }

    private void updateLocations() {
        if (mapView != null) {
            Layer layer = (Layer) DataBus.instance().get(DataChannel.LAYER);
            List<Placemark> visibleMarkers = new ArrayList<Placemark>();
            Log.i("Mapscreen", "starting updating locations, total number of placemarks:"+layer.getZOrderedPlacemarks().size());
            for (Placemark placemark : layer.getZOrderedPlacemarks()) {
                if (placemark.xmlShowInRadar) {
                    Log.i("Mapscreen", "placemarker:"+placemark.xmlId+" made it");
                    visibleMarkers.add(placemark);
                }
            }
            placemarkersOverLay = new PlacemarkersOverLay(visibleMarkers);

            if (placemarkersOverLay != null) {
                mapView.getOverlays().remove(placemarkersOverLay);
            }
            mapView.getOverlays().add(placemarkersOverLay);
            mapView.invalidate();
        }
    }

    
    @Override
    protected boolean isRouteDisplayed() {
        return (false);
    }

    private GeoPoint getPoint(double lat, double lon) {
        return (new GeoPoint((int) (lat * 1000000.0), (int) (lon * 1000000.0)));
    }

    private class PlacemarkersOverLay extends ItemizedOverlay<CustomItem> {
        private List<CustomItem> items = new ArrayList<CustomItem>();

        public PlacemarkersOverLay(List<Placemark> placemarks) {
            super(null);

            Drawable marker = getMarker(R.drawable.marker_red);
            for (Placemark placemark : placemarks) {
                items.add(new CustomItem(getPoint(placemark.geoLoc.lat, placemark.geoLoc.lon), placemark.xmlName, "",
                        marker));
                Log.i("Mapview", "adding element with id :" + placemark.xmlId);
            }

            populate();
        }

        @Override
        protected CustomItem createItem(int i) {
            return (items.get(i));
        }

        @Override
        public int size() {
            return (items.size());
        }

        private Drawable getMarker(int resource) {
            Drawable marker = getResources().getDrawable(resource);

            marker.setBounds(0, 0, marker.getIntrinsicWidth(), marker.getIntrinsicHeight());
            boundCenter(marker);

            return (marker);
        }
    }

    class CustomItem extends OverlayItem {
        Drawable marker = null;

        CustomItem(GeoPoint pt, String name, String snippet, Drawable marker) {
            super(pt, name, snippet);
            this.marker = marker;
        }

        @Override
        public Drawable getMarker(int stateBitset) {
            setState(marker, stateBitset);
            return (marker);
        }
    }

    @Override
    public void onDataChange(DataChangeEvent p) {
        updateLocations();
    }

    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.map_menu, menu);
        return true;
    }
    
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        if (item.getItemId()==R.id.close_map){
            this.finish();
            return true;
        } 
        if (item.getItemId()==R.id.center_map){
            if (myLocationOverlay!=null&&myLocationOverlay.getMyLocation()!=null){
                mapView.getController().animateTo(myLocationOverlay.getMyLocation());
                return true;
            }
        }

        return false;
        
    }
}
