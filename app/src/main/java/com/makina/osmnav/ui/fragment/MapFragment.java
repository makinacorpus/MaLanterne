package com.makina.osmnav.ui.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
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
import com.makina.osmnav.ui.widget.LevelsFilterNavigationListView;
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
import org.osmdroid.views.overlay.TilesOverlay;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
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
    private static final String STATE_INDOOR_LEVEL = "STATE_INDOOR_LEVEL";

    private MapView mMapView;

    private IGeoPoint mMapCenter;
    private int mZoomLevel;
    private double mIndoorLevel;

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
            mZoomLevel = 15;
            // as default indoor level
            mIndoorLevel = 0d;
        }
        else {
            mMapCenter = savedInstanceState.getParcelable(STATE_MAP_POSITION);
            mZoomLevel = savedInstanceState.getInt(STATE_MAP_ZOOM_LEVEL);
            mIndoorLevel = savedInstanceState.getDouble(STATE_INDOOR_LEVEL);
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
        outState.putDouble(STATE_INDOOR_LEVEL,
                           mIndoorLevel);

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

        // FIXME: hardcoded default MBTiles source
        final MapTileModuleProviderBase baseModuleProvider = getMBTilesProvider("gdl.mbtiles",
                                                                                null);

        if (baseModuleProvider == null) {
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
            final MBTilesProvider baseProviders = MBTilesProvider.createFromProviders(TILE_SIZE,
                                                                                  baseModuleProvider);
            baseProviders.setSelectedIndoorLevel(mIndoorLevel);

            mapView = new MapView(getContext(),
                                  TILE_SIZE,
                                  resourceProxy,
                                  baseProviders);

            mapView.setScrollableAreaLimit(MBTILES_BOUNDING_BOX);

            final List<MapTileModuleProviderBase> levelModuleProviders = loadLevelModuleProviders();

            if (!levelModuleProviders.isEmpty()) {
                final MBTilesProvider levelProviders = MBTilesProvider.createFromProviders(TILE_SIZE,
                                                                                           levelModuleProviders.toArray(new MapTileModuleProviderBase[levelModuleProviders.size()]));
                levelProviders.setSelectedIndoorLevel(mIndoorLevel);

                final TilesOverlay tilesOverlay = new TilesOverlay(levelProviders,
                                                                   getContext());
                tilesOverlay.setLoadingBackgroundColor(Color.TRANSPARENT);
                mapView.getOverlays()
                       .add(tilesOverlay);

                final LevelsFilterNavigationListView levelsFilterNavigationListView = new LevelsFilterNavigationListView(((AppCompatActivity) getActivity()).getSupportActionBar());
                levelsFilterNavigationListView.setDefaultLevel(mIndoorLevel);
                // FIXME: hardcoded available levels
                levelsFilterNavigationListView.setLevels(Arrays.asList(2d,
                                                                       1d,
                                                                       0d,
                                                                       -0.25d,
                                                                       -0.5d,
                                                                       -0.75d,
                                                                       -1d,
                                                                       -2d,
                                                                       -3d));
                levelsFilterNavigationListView.setLevelsFilterViewCallback(new LevelsFilterNavigationListView.LevelsFilterViewCallback() {
                    @Override
                    public void onLevelSelected(double level) {
                        mIndoorLevel = level;

                        levelProviders.setSelectedIndoorLevel(level);
                        mapView.invalidate();
                        mapView.getController()
                               .animateTo(mMapView.getMapCenter());

                    }
                });
            }
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
    private List<MapTileModuleProviderBase> loadLevelModuleProviders() {
        final List<MapTileModuleProviderBase> moduleProviders = new ArrayList<>();

        // FIXME: hardcoded MBTiles sources
        final List<String> mbTilesSources = Arrays.asList("gdl_2.0.mbtiles",
                                                          "gdl_1.0.mbtiles",
                                                          "gdl_0.0.mbtiles",
                                                          "gdl_-0.25.mbtiles",
                                                          "gdl_-0.5.mbtiles",
                                                          "gdl_-0.75.mbtiles",
                                                          "gdl_-1.0.mbtiles",
                                                          "gdl_-2.0.mbtiles",
                                                          "gdl_-3.0.mbtiles");

        for (String mbTilesSource : mbTilesSources) {
            Double level = null;
            final String[] tokens = mbTilesSource.split("_");

            if (tokens.length > 1) {
                final String levelsAsString = tokens[1].split(".mbtiles")[0];

                try {
                    level = Double.valueOf(levelsAsString);
                }
                catch (NumberFormatException nfe) {
                    Log.w(TAG,
                          nfe.getMessage());
                }
            }

            final MapTileModuleProviderBase moduleProvider = getMBTilesProvider(mbTilesSource,
                                                                                level);
            if (moduleProvider != null) {
                moduleProviders.add(moduleProvider);
            }
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
