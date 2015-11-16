package com.makina.osmnav.ui.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
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
import com.makina.osmnav.model.LayerSource;
import com.makina.osmnav.model.LayersSettings;
import com.makina.osmnav.model.LayersSource;
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
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.TilesOverlay;

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

    private static final String ARG_LAYERS_SETTINGS = "ARG_LAYERS_SETTINGS";

    private static final String STATE_MAP_POSITION = "STATE_MAP_POSITION";
    private static final String STATE_MAP_ZOOM_LEVEL = "STATE_MAP_ZOOM_LEVEL";
    private static final String STATE_INDOOR_LEVEL = "STATE_INDOOR_LEVEL";

    private MapView mMapView;

    private LayersSettings mLayersSettings;
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
    public static MapFragment newInstance(@Nullable final LayersSettings layersSettings) {
        final MapFragment fragment = new MapFragment();
        final Bundle args = new Bundle();

        if (layersSettings != null) {
            args.putParcelable(ARG_LAYERS_SETTINGS,
                               layersSettings);
        }

        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mLayersSettings = getArguments().getParcelable(ARG_LAYERS_SETTINGS);

        if (savedInstanceState == null) {
            // FIXME: hardcoded default map position
            mMapCenter = mLayersSettings == null ? new GeoPoint(48.853307d,
                                                                2.348864d) : mLayersSettings.boundingBoxE6.getCenter();
            // FIXME: default hardcoded zoom level
            mZoomLevel = 17;
            // as default indoor level
            mIndoorLevel = 0d;

            if (BuildConfig.DEBUG) {
                Log.d(TAG, "default tile size: " + getTileSize());
            }
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
        mMapView = setupMapView();

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
        if (BuildConfig.DEBUG) {
            Log.d(TAG,
                  "longPressHelper: " + "lat= " + geoPoint.getLatitude() + ", lon=" + geoPoint.getLongitude());
        }

        Toast.makeText(getContext(),
                       getString(R.string.toast_geopoint,
                                 "lat= " + geoPoint.getLatitude() + ", lon=" + geoPoint.getLongitude()),
                       Toast.LENGTH_LONG)
             .show();

        return true;
    }

    private MapView setupMapView() {
        final MapView mapView;

        if (mLayersSettings == null) {
            mapView = setupDefaultMapView();

            Toast.makeText(getContext(),
                           getString(R.string.toast_no_layers_settings_found),
                           Toast.LENGTH_LONG)
                 .show();
        }
        else {
            final MapTileModuleProviderBase baseModuleProvider = getMBTilesProvider(mLayersSettings.layersSource.base);

            if (baseModuleProvider == null) {
                // failed to load the MBTiles as file, use the default configuration
                mapView = setupDefaultMapView();

                Toast.makeText(getContext(),
                               getString(R.string.toast_mbtiles_load_failed),
                               Toast.LENGTH_LONG)
                     .show();
            }
            else {
                updateTitle(mLayersSettings.name);

                final MBTilesProvider baseProviders = MBTilesProvider.createFromProviders(getTileSize(),
                                                                                          baseModuleProvider);

                mapView = new MapView(getContext(),
                                      getTileSize(),
                                      new DefaultResourceProxyImpl(getContext().getApplicationContext()),
                                      baseProviders);

                mapView.setScrollableAreaLimit(mLayersSettings.boundingBoxE6);
                mapView.setMaxZoomLevel(baseProviders.getMaximumZoomLevel() - getResources().getInteger(R.integer.last_zoom_offset));

                final List<MapTileModuleProviderBase> levelModuleProviders = loadLevelModuleProviders(mLayersSettings.layersSource);

                if (!levelModuleProviders.isEmpty()) {
                    final MBTilesProvider levelProviders = MBTilesProvider.createFromProviders(getTileSize(),
                                                                                               levelModuleProviders.toArray(new MapTileModuleProviderBase[levelModuleProviders.size()]));
                    levelProviders.setSelectedIndoorLevel(mIndoorLevel);

                    final TilesOverlay tilesOverlay = new TilesOverlay(levelProviders,
                                                                       getContext());
                    tilesOverlay.setLoadingBackgroundColor(Color.TRANSPARENT);
                    mapView.getOverlays()
                           .add(tilesOverlay);

                    final LevelsFilterNavigationListView levelsFilterNavigationListView = new LevelsFilterNavigationListView(((AppCompatActivity) getActivity()).getSupportActionBar());
                    levelsFilterNavigationListView.setDefaultLevel(mIndoorLevel);

                    final List<Double> levels = new ArrayList<>();

                    for (MapTileModuleProviderBase levelModuleProvider : levelModuleProviders) {
                        if (levelModuleProvider instanceof MBTilesModuleProvider) {
                            levels.add(((MBTilesModuleProvider) levelModuleProvider).getIndoorLevel());
                        }
                    }

                    levelsFilterNavigationListView.setLevels(levels);
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
        }

        configureMapView(mapView);

        return mapView;
    }

    @NonNull
    private MapView setupDefaultMapView() {
        final MapView mapView = new MapView(getContext(),
                                            getTileSize(),
                                            new DefaultResourceProxyImpl(getContext().getApplicationContext()));
        mapView.setTileSource(TileSourceFactory.MAPNIK);

        return mapView;
    }

    private void configureMapView(@NonNull final MapView mapView) {
        mapView.setMultiTouchControls(true);
        mapView.setTilesScaledToDpi(false);

        if (BuildConfig.DEBUG) {
            new MapLoggerListener(mapView);
        }

        final IMapController mapController = mapView.getController();

        if (mMapCenter != null) {
            mapController.setCenter(mMapCenter);
        }

        mapController.setZoom(mZoomLevel);

        final MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(getContext(),
                                                                       this);

        // add MapEventsOverlay as primary Map overlay
        mapView.getOverlays()
               .add(0,
                    mapEventsOverlay);
    }

    @NonNull
    private List<MapTileModuleProviderBase> loadLevelModuleProviders(@NonNull final LayersSource layersSource) {
        final List<MapTileModuleProviderBase> moduleProviders = new ArrayList<>();

        for (LayerSource layerSource : layersSource.layers) {
            if (layerSource.level == null) {
                continue;
            }

            final MapTileModuleProviderBase moduleProvider = getMBTilesProvider(layerSource);

            if (moduleProvider != null) {
                moduleProviders.add(moduleProvider);
            }
        }

        return moduleProviders;
    }

    @Nullable
    private MBTilesModuleProvider getMBTilesProvider(@NonNull final LayerSource layerSource) {
        final File mbtiles = FileUtils.getFileFromApplicationStorage(getContext(),
                                                                     layerSource.source);

        if ((mbtiles == null) || !mbtiles.exists()) {
            Log.w(TAG,
                  "getMBTilesProvider: unable to load MBTiles '" + layerSource.source + "'");

            return null;
        }

        final MBTilesModuleProvider moduleProvider = new MBTilesModuleProvider(new SimpleRegisterReceiver(getContext()),
                                                                               mbtiles);
        moduleProvider.setIndoorLevel(layerSource.level);

        return moduleProvider;
    }

    private void updateTitle(@NonNull final String title) {
        final AppCompatActivity activity = (AppCompatActivity) getActivity();

        if (activity != null) {
            final ActionBar actionBar = activity.getSupportActionBar();

            if (actionBar == null) {
                return;
            }

            actionBar.setTitle(title);
        }
    }

    private int getTileSize() {
        return getResources().getInteger(R.integer.tile_size);
    }
}
