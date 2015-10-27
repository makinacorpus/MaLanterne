package com.makina.osmnav.ui.fragment;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.makina.osmnav.BuildConfig;
import com.makina.osmnav.R;
import com.makina.osmnav.map.MBTilesModuleProvider;
import com.makina.osmnav.map.MBTilesProvider;
import com.makina.osmnav.map.MapLoggerListener;
import com.makina.osmnav.util.FileUtils;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.overlays.MapEventsOverlay;
import org.osmdroid.bonuspack.overlays.MapEventsReceiver;
import org.osmdroid.tileprovider.modules.MapTileModuleProviderBase;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple {@code Fragment} displaying a {@code MapView}.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class MapFragment
        extends Fragment
        implements MapEventsReceiver {

    private static final String TAG = MapFragment.class.getName();

    // FIXME: hardcoded tile size (in px)
    private static final int TILE_SIZE = 512;

    // FIXME: hardcoded bounding box
    private static final BoundingBoxE6 MBTILES_BOUNDING_BOX = new BoundingBoxE6(48.8487,
                                                                                2.3866,
                                                                                48.8368,
                                                                                2.3664);

    private static final String STATE_MAP_POSITION = "STATE_MAP_POSITION";
    private static final String STATE_MAP_ZOOM_LEVEL = "STATE_MAP_ZOOM_LEVEL";

    private MapView mMapView;

    private IGeoPoint mMapCenter;
    private int mZoomLevel;

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

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            mMapCenter = MBTILES_BOUNDING_BOX.getCenter();
            // FIXME: default hardcoded zoom level
            mZoomLevel = 14;
        }
        else {
            mMapCenter = savedInstanceState.getParcelable(STATE_MAP_POSITION);
            mZoomLevel = savedInstanceState.getInt(STATE_MAP_ZOOM_LEVEL);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        mMapView = setupMap();

        return mMapView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(STATE_MAP_POSITION,
                               (Parcelable) mMapView.getMapCenter());
        outState.putInt(STATE_MAP_ZOOM_LEVEL,
                        mMapView.getZoomLevel());

        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean singleTapConfirmedHelper(GeoPoint geoPoint) {
        return false;
    }

    @Override
    public boolean longPressHelper(GeoPoint geoPoint) {
        Toast.makeText(getContext(),
                       getString(R.string.toast_geopoint,
                                 "lat= " + geoPoint.getLatitude() + ", lon=" + geoPoint.getLongitude()),
                       Toast.LENGTH_LONG)
             .show();

        return true;
    }

    private MapView setupMap() {
        final MapView mapView;

        final DefaultResourceProxyImpl resourceProxy = new DefaultResourceProxyImpl(getContext().getApplicationContext());
        final List<MapTileModuleProviderBase> moduleProviders = loadModuleProviders();

        if (moduleProviders.isEmpty()) {
            // failed to load the MBTiles as file, use the default configuration
            mapView = new MapView(getContext(),
                                  TILE_SIZE,
                                  resourceProxy);
            mapView.setTileSource(TileSourceFactory.MAPNIK);

            Toast.makeText(getContext(),
                           getString(R.string.toast_mbtiles_load_failed),
                           Toast.LENGTH_LONG)
                 .show();
        }
        else {
            final MBTilesProvider providers = MBTilesProvider.createFromProviders(TILE_SIZE,
                                                                                  moduleProviders.toArray(new MapTileModuleProviderBase[moduleProviders.size()]));
            providers.setSelectedIndoorLevel(0.0d);

            mapView = new MapView(getContext(),
                                  TILE_SIZE,
                                  resourceProxy,
                                  providers);

            mapView.setScrollableAreaLimit(MBTILES_BOUNDING_BOX);
        }

        mapView.setMultiTouchControls(true);
        mapView.setTilesScaledToDpi(false);

        if (BuildConfig.DEBUG) {
            new MapLoggerListener(mapView);
        }

        final IMapController mapController = mapView.getController();
        mapController.setCenter(mMapCenter);
        mapController.setZoom(mZoomLevel);

        final MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(getContext(),
                                                                       this);

        // add MapEventsOverlay as primary Map overlay
        mapView.getOverlays()
               .add(0,
                    mapEventsOverlay);

        return mapView;
    }

    @NonNull
    private List<MapTileModuleProviderBase> loadModuleProviders() {
        final List<MapTileModuleProviderBase> moduleProviders = new ArrayList<>();

        final MapTileModuleProviderBase defaultMBTilesProvider = getMBTilesProvider("garedelyon.mbtiles",
                                                                             null);

        if (defaultMBTilesProvider != null) {
            moduleProviders.add(defaultMBTilesProvider);
        }

        final MapTileModuleProviderBase mbTilesProviderLevel0 = getMBTilesProvider("gdl_0.mbtiles",
                                                                                   0.0d);

        if (mbTilesProviderLevel0 != null) {
            moduleProviders.add(mbTilesProviderLevel0);
        }

        return moduleProviders;
    }

    @Nullable
    private MBTilesModuleProvider getMBTilesProvider(@NonNull final String filename,
                                                     @Nullable Double indoorLevel) {
        final File mbtiles = FileUtils.getFileFromApplicationStorage(getContext(),
                                                                     filename);

        if ((mbtiles == null) || !mbtiles.exists()) {
            Log.w(TAG,
                  "getMBTilesProvider: unable to load MBTiles '" + filename + "'");

            return null;
        }

        final MBTilesModuleProvider moduleProvider = new MBTilesModuleProvider(new SimpleRegisterReceiver(getContext()),
                                                                                      mbtiles);
        moduleProvider.setIndoorLevel(indoorLevel);

        return moduleProvider;
    }
}
