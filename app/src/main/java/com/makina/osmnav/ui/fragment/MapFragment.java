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
import com.makina.osmnav.util.FileUtils;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapController;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.MapTileProviderArray;
import org.osmdroid.tileprovider.modules.IArchiveFile;
import org.osmdroid.tileprovider.modules.MBTilesFileArchive;
import org.osmdroid.tileprovider.modules.MapTileFileArchiveProvider;
import org.osmdroid.tileprovider.modules.MapTileModuleProviderBase;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.views.MapView;

import java.io.File;

/**
 * Simple {@code Fragment} displaying a {@code MapView}.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class MapFragment
        extends Fragment {

    private static final String TAG = MapFragment.class.getName();

    // FIXME: hardcoded tile size (in px)
    private static final int TILE_SIZE = 512;

    // FIXME: hardcoded default MBTiles source to load
    private static final String MBTILES_FILE = "garedelyon.mbtiles";

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

    private MapView setupMap() {
        final MapView mapView;

        final DefaultResourceProxyImpl resourceProxy = new DefaultResourceProxyImpl(getContext().getApplicationContext());

        final MapTileModuleProviderBase mbTilesProvider = getMBTilesProvider(MBTILES_FILE);

        if (mbTilesProvider == null) {
            // failed to load the MBtiles as file, use the default configuration
            mapView = new MapView(getContext(),
                                  TILE_SIZE,
                                  resourceProxy);
            mapView.setTileSource(TileSourceFactory.MAPNIK);

            Toast.makeText(getContext(),
                           getString(R.string.toast_mbtiles_load_failed,
                                     MBTILES_FILE),
                           Toast.LENGTH_LONG)
                 .show();
        }
        else {
            mapView = new MapView(getContext(),
                                  TILE_SIZE,
                                  resourceProxy,
                                  new MapTileProviderArray(getDefaultTileSource(),
                                                           null,
                                                           new MapTileModuleProviderBase[] {
                                                                   mbTilesProvider
                                                           }));

            mapView.setScrollableAreaLimit(MBTILES_BOUNDING_BOX);
        }

        mapView.setMultiTouchControls(true);
        mapView.setTilesScaledToDpi(false);

        if (BuildConfig.DEBUG) {
            mapView.setMapListener(new MapListener() {
                @Override
                public boolean onScroll(ScrollEvent event) {
                    final IGeoPoint geoPoint = mapView.getMapCenter();

                    if (BuildConfig.DEBUG) {
                        Log.d(TAG,
                              "onScroll: " + "[lat=" + geoPoint.getLatitude() + ", lon=" + geoPoint.getLongitude() + ", zoom=" + mapView.getZoomLevel());
                    }

                    return false;
                }

                @Override
                public boolean onZoom(ZoomEvent event) {
                    final IGeoPoint geoPoint = mapView.getMapCenter();

                    if (BuildConfig.DEBUG) {
                        Log.d(TAG,
                              "onZoom: " + "[lat=" + geoPoint.getLatitude() + ", lon=" + geoPoint.getLongitude() + ", zoom=" + mapView.getZoomLevel());
                    }

                    return false;
                }
            });
        }

        final IMapController mapController = mapView.getController();
        mapController.setCenter(mMapCenter);
        mapController.setZoom(mZoomLevel);

        return mapView;
    }

    @NonNull
    private XYTileSource getDefaultTileSource() {
        // this is a dummy TileSource needed by MapTileFileArchiveProvider and MapTileProviderBase ...
        return new XYTileSource("mbtiles",
                                ResourceProxy.string.offline_mode,
                                0,
                                21, // FIXME: hardcoded max zoom (should be read from the MBTiles)
                                TILE_SIZE,
                                ".png",
                                new String[] {
                                        "http://a.tile.openstreetmap.org/",
                                        "http://b.tile.openstreetmap.org/",
                                        "http://c.tile.openstreetmap.org/"
                                });
    }

    @Nullable
    private MapTileModuleProviderBase getMBTilesProvider(@NonNull final String filename) {
        final File mbtiles = FileUtils.getFileFromApplicationStorage(getContext(),
                                                                     filename);

        if ((mbtiles == null) || !mbtiles.exists()) {
            Log.w(TAG,
                  "getMBTilesProvider: unable to load MBTiles '" + filename + "'");

            return null;
        }

        return new MapTileFileArchiveProvider(new SimpleRegisterReceiver(getContext()),
                                              getDefaultTileSource(),
                                              new IArchiveFile[] {
                                                      MBTilesFileArchive.getDatabaseFileArchive(mbtiles)
                                              });
    }
}
