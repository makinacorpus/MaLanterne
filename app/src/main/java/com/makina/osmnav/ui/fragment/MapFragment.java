package com.makina.osmnav.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.makina.osmnav.BuildConfig;
import com.makina.osmnav.R;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapController;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

/**
 * Simple {@code Fragment} displaying a {@code MapView}.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class MapFragment
        extends Fragment {

    private static final String TAG = MapFragment.class.getName();

    private MapView mMapView;

    public MapFragment() {
        // required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of this {@code Fragment} using the provided
     * parameters.
     *
     * @return a new instance of fragment {@link MapFragment}.
     */
    public static MapFragment newInstance() {
        final MapFragment fragment = new MapFragment();
        final Bundle args = new Bundle();

        fragment.setArguments(args);

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        // inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_map,
                                container,
                                false);
    }

    @Override
    public void onViewCreated(View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view,
                            savedInstanceState);

        mMapView = (MapView) view.findViewById(R.id.mapView);

        setupMap();
    }

    private void setupMap() {
        mMapView.setTileSource(TileSourceFactory.MAPNIK);

        mMapView.setMultiTouchControls(true);

        if (BuildConfig.DEBUG) {
            mMapView.setMapListener(new MapListener() {
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
            });
        }

        final IMapController mapController = mMapView.getController();
        mapController.setZoom(12);

        final GeoPoint startPoint = new GeoPoint(48.854776,
                                                 2.3404309999999997);
        mapController.setCenter(startPoint);
    }
}
