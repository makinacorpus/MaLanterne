package com.makina.osmnav.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.makina.osmnav.R;

import org.osmdroid.api.IMapController;
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

        mMapView.setBuiltInZoomControls(true);
        mMapView.setMultiTouchControls(true);

        final IMapController mapController = mMapView.getController();
        mapController.setZoom(9);

        final GeoPoint startPoint = new GeoPoint(48.8583,
                                                 2,
                                                 2944);
        mapController.setCenter(startPoint);
    }
}
