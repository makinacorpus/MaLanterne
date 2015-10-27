package com.makina.osmnav.ui.map;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.osmdroid.ResourceProxy;
import org.osmdroid.tileprovider.IRegisterReceiver;
import org.osmdroid.tileprovider.MapTileProviderArray;
import org.osmdroid.tileprovider.MapTileRequestState;
import org.osmdroid.tileprovider.modules.MapTileModuleProviderBase;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.XYTileSource;

import java.util.ArrayList;
import java.util.List;

/**
 * Default {@code MapTileProviderBase} about loading MBTiles sources.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class MBTilesProvider
        extends MapTileProviderArray {

    private Double mSelectedIndoorLevel;

    public static MBTilesProvider createFromProviders(int defaultTileSize,
                                                      @NonNull final MapTileModuleProviderBase... tileProviderArray) {
        if (tileProviderArray.length == 0) {
            throw new IllegalArgumentException("MBTilesProvider should use at least one MapTileModuleProviderBase implementation");
        }

        // this is a dummy TileSource needed by MapTileProviderBase ...
        final ITileSource defaultTileSource = new XYTileSource("default",
                                                               ResourceProxy.string.offline_mode,
                                                               0,
                                                               21,
                                                               defaultTileSize,
                                                               ".png",
                                                               new String[] {
                                                                       "http://a.tile.openstreetmap.org/",
                                                                       "http://b.tile.openstreetmap.org/",
                                                                       "http://c.tile.openstreetmap.org/"
                                                               });

        return new MBTilesProvider(defaultTileSource,
                                   null,
                                   tileProviderArray);
    }

    protected MBTilesProvider(ITileSource pTileSource,
                              IRegisterReceiver aRegisterReceiver,
                              MapTileModuleProviderBase[] pTileProviderArray) {
        super(pTileSource,
              aRegisterReceiver,
              pTileProviderArray);
    }

    public void setSelectedIndoorLevel(@Nullable final Double selectedIndoorLevel) {
        this.mSelectedIndoorLevel = selectedIndoorLevel;

        clearTileCache();
    }

    @Override
    protected MapTileModuleProviderBase findNextAppropriateProvider(MapTileRequestState aState) {
        MapTileModuleProviderBase provider = null;

        final int zoomLevel = aState.getMapTile()
                                    .getZoomLevel();

        final List<MapTileModuleProviderBase> moduleProvidersCandidates = new ArrayList<>();

        // first: try to find all matching module providers
        synchronized (mTileProviderList) {
            for (MapTileModuleProviderBase moduleProvider : mTileProviderList) {
                // should never occur ...
                if (moduleProvider == null) {
                    continue;
                }

                // require a data connection
                if (!useDataConnection() && moduleProvider.getUsesDataConnection()) {
                    continue;
                }

                // the current zoom level doesn't match the available zoom levels of this provider
                if (zoomLevel > moduleProvider.getMaximumZoomLevel() || zoomLevel < moduleProvider.getMinimumZoomLevel()) {
                    continue;
                }

                // check if the indoor level is defined for this module provider and is the same of the current selected one
                if ((mSelectedIndoorLevel != null) && (moduleProvider instanceof MBTilesModuleProvider) && (((MBTilesModuleProvider) moduleProvider).getIndoorLevel() != null) && (!mSelectedIndoorLevel.equals(((MBTilesModuleProvider) moduleProvider).getIndoorLevel()))) {
                    continue;
                }

                moduleProvidersCandidates.add(moduleProvider);
            }
        }

        // then try to select the best candidate
        for (MapTileModuleProviderBase moduleProvider : moduleProvidersCandidates) {
            if (provider == null) {
                provider = moduleProvider;
            }

            if ((moduleProvider instanceof MBTilesModuleProvider) && (((MBTilesModuleProvider) moduleProvider).getIndoorLevel() != null)) {
                provider = moduleProvider;
            }
        }

        MapTileModuleProviderBase nextProvider;

        do {
            nextProvider = aState.getNextProvider();

        }
        while ((nextProvider != null) && (nextProvider != provider));

        return nextProvider;
    }
}
