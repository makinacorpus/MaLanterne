package com.makina.osmnav.ui.map;

import android.support.annotation.NonNull;
import android.util.Log;

import com.makina.osmnav.BuildConfig;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.views.MapView;

/**
 * For debugging purpose only.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class MapLoggerListener
        implements MapListener {

    private static final String TAG = MapLoggerListener.class.getName();

    @NonNull
    private final MapView mMapView;

    public MapLoggerListener(@NonNull final MapView pMapView) {
        this.mMapView = pMapView;

        this.mMapView.setMapListener(this);
    }

    @Override
    public boolean onScroll(ScrollEvent event) {
        final IGeoPoint geoPoint = mMapView.getMapCenter();

        if (BuildConfig.DEBUG) {
            Log.d(TAG,
                  "onScroll: " + "[lat=" + geoPoint.getLatitude() + ", lon=" + geoPoint.getLongitude() + ", zoom=" + mMapView.getZoomLevel());
        }

        return false;
    }

    @Override
    public boolean onZoom(ZoomEvent event) {
        final IGeoPoint geoPoint = mMapView.getMapCenter();

        if (BuildConfig.DEBUG) {
            Log.d(TAG,
                  "onZoom: " + "[lat=" + geoPoint.getLatitude() + ", lon=" + geoPoint.getLongitude() + ", zoom=" + mMapView.getZoomLevel());
        }

        return false;
    }
}
