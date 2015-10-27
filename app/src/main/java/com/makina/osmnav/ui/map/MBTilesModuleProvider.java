package com.makina.osmnav.ui.map;

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import org.osmdroid.tileprovider.IRegisterReceiver;
import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.MapTileRequestState;
import org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants;
import org.osmdroid.tileprovider.modules.MapTileFileStorageProviderBase;
import org.osmdroid.tileprovider.modules.MapTileModuleProviderBase;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.util.StreamUtils;

import java.io.File;
import java.io.InputStream;

/**
 * Default provider for {@link MBTilesSource}.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class MBTilesModuleProvider
        extends MapTileFileStorageProviderBase {

    private static final String TAG = MBTilesModuleProvider.class.getName();

    private final MBTilesSource mTileSource;
    private Double mIndoorLevel;

    public MBTilesModuleProvider(IRegisterReceiver pRegisterReceiver,
                                 @NonNull final File mbTilesFile) {
        super(pRegisterReceiver,
              NUMBER_OF_TILE_FILESYSTEM_THREADS,
              TILE_FILESYSTEM_MAXIMUM_QUEUE_SIZE);

        // gets the TileSource to use from the given file
        mTileSource = MBTilesSource.createFromFile(mbTilesFile);
    }

    @Nullable
    public Double getIndoorLevel() {
        return mIndoorLevel;
    }

    public void setIndoorLevel(@Nullable final Double indoorLevel) {
        this.mIndoorLevel = indoorLevel;
    }

    @Override
    protected String getName() {
        return MBTilesModuleProvider.class.getName();
    }

    @Override
    protected String getThreadGroupName() {
        return getName() + "ThreadGroup";
    }

    @Override
    protected Runnable getTileLoader() {
        return new TileLoader();
    }

    @Override
    public boolean getUsesDataConnection() {
        return false;
    }

    @Override
    public int getMinimumZoomLevel() {
        return mTileSource.getMinimumZoomLevel();
    }

    @Override
    public int getMaximumZoomLevel() {
        return mTileSource.getMaximumZoomLevel();
    }

    @Override
    public void setTileSource(ITileSource tileSource) {
        // do nothing
    }

    @Override
    public void detach() {
        mTileSource.close();

        super.detach();
    }

    protected class TileLoader
            extends MapTileModuleProviderBase.TileLoader {

        @Override
        public Drawable loadTile(final MapTileRequestState pState) {
            final MapTile mapTile = pState.getMapTile();

            // if there's no sdcard then don't do anything
            if (!getSdCardAvailable()) {
                if (OpenStreetMapTileProviderConstants.DEBUGMODE) {
                    Log.d(TAG,
                          "No sdcard - do nothing for tile: " + mapTile);
                }

                return null;
            }

            InputStream inputStream = null;

            try {
                if (OpenStreetMapTileProviderConstants.DEBUGMODE) {
                    Log.d(TAG,
                          "Tile doesn't exist: " + mapTile);
                }

                inputStream = mTileSource.getInputStream(mTileSource,
                                                         mapTile);
                if (inputStream != null) {
                    if (OpenStreetMapTileProviderConstants.DEBUGMODE) {
                        Log.d(TAG,
                              "Use tile from archive: " + mapTile);
                    }

                    return mTileSource.getDrawable(inputStream);
                }
            }
            catch (final Throwable e) {
                Log.e(TAG,
                      "Error loading tile",
                      e);
            }
            finally {
                StreamUtils.closeStream(inputStream);
            }

            return null;
        }
    }
}
