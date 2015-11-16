package com.makina.osmnav.map;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.util.Log;

import com.makina.osmnav.BuildConfig;

import org.osmdroid.ResourceProxy;
import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.modules.IArchiveFile;
import org.osmdroid.tileprovider.tilesource.BitmapTileSourceBase;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.util.StreamUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;

/**
 * Default MBTiles tiles source from {@code BitmapTileSourceBase}.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class MBTilesSource
        extends BitmapTileSourceBase
        implements IArchiveFile {

    private static final String TAG = MBTilesSource.class.getName();

    public static final int DEFAULT_MIN_ZOOM_LEVEL = 1;
    public static final int DEFAULT_MAX_ZOOM_LEVEL = 22;
    public static final int DEFAULT_TILE_SIZE = 256;

    private final static String TABLE_TILES = "tiles";
    private final static String COL_TILES_ZOOM_LEVEL = "zoom_level";
    private final static String COL_TILES_TILE_COLUMN = "tile_column";
    private final static String COL_TILES_TILE_ROW = "tile_row";
    private final static String COL_TILES_TILE_DATA = "tile_data";

    private final SQLiteDatabase mDatabase;

    @NonNull
    public static MBTilesSource createFromFile(@NonNull final File file) {
        // open the SQLite database
        final SQLiteDatabase db = SQLiteDatabase.openDatabase(file.getAbsolutePath(),
                                                              null,
                                                              SQLiteDatabase.NO_LOCALIZED_COLLATORS | SQLiteDatabase.OPEN_READONLY);

        // find the minimum zoom level from the MBTiles file
        int minZoomLevel = getInt(db,
                                  "SELECT MIN(zoom_level) FROM tiles;");

        // find the maximum zoom level from the MBTiles file
        int maxZoomLevel = getInt(db,
                                  "SELECT MAX(zoom_level) FROM tiles;");

        // find the tile size
        InputStream is = null;
        int tileSize = -1;

        Cursor cursor = db.rawQuery("SELECT tile_data FROM images LIMIT 0,1",
                                    new String[] {});

        if (cursor.getCount() != 0) {
            cursor.moveToFirst();
            is = new ByteArrayInputStream(cursor.getBlob(0));

            final Bitmap bitmap = BitmapFactory.decodeStream(is);
            tileSize = bitmap.getHeight();
        }

        cursor.close();
        StreamUtils.closeStream(is);

        if (BuildConfig.DEBUG) {
            Log.d(TAG,
                  "createFromFile: " + file.getName() + ", min zoom level found: " + minZoomLevel + ", max zoom level found: " + maxZoomLevel + ", tile size found: " + tileSize);
        }

        return new MBTilesSource(minZoomLevel > -1 ? minZoomLevel : DEFAULT_MIN_ZOOM_LEVEL,
                                 maxZoomLevel > -1 ? maxZoomLevel : DEFAULT_MAX_ZOOM_LEVEL,
                                 tileSize > -1 ? tileSize : DEFAULT_TILE_SIZE,
                                 db);
    }

    protected static int getInt(@NonNull final SQLiteDatabase db,
                                @NonNull final String sql) {
        final Cursor cursor = db.rawQuery(sql,
                                          new String[] {});
        int value = -1;

        if (cursor.getCount() != 0) {
            cursor.moveToFirst();
            value = cursor.getInt(0);
        }

        cursor.close();

        return value;
    }

    protected MBTilesSource(int aZoomMinLevel,
                            int aZoomMaxLevel,
                            int aTileSizePixels,
                            final SQLiteDatabase pDatabase) {
        super("mbtiles",
              ResourceProxy.string.offline_mode,
              aZoomMinLevel,
              aZoomMaxLevel,
              aTileSizePixels,
              ".png");

        mDatabase = pDatabase;
    }

    @Override
    public InputStream getInputStream(ITileSource tileSource,
                                      MapTile tile) {
        // from MBTilesFileArchive
        try {
            InputStream inputStream = null;

            final String selection = String.format("%1$s=? and %2$s=? and %3$s=?",
                                                   COL_TILES_TILE_COLUMN,
                                                   COL_TILES_TILE_ROW,
                                                   COL_TILES_ZOOM_LEVEL);
            final String[] selectionArgs = {
                    Integer.toString(tile.getX()),
                    // Use Google Tiling Spec
                    Double.toString(Math.pow(2,
                                             tile.getZoomLevel()) - tile.getY() - 1),
                    Integer.toString(tile.getZoomLevel())
            };

            final Cursor cur = mDatabase.query(TABLE_TILES,
                                               new String[] {
                                                       COL_TILES_TILE_DATA
                                               },
                                               selection,
                                               selectionArgs,
                                               null,
                                               null,
                                               null);

            if (cur.getCount() != 0) {
                cur.moveToFirst();
                inputStream = new ByteArrayInputStream(cur.getBlob(0));
            }

            cur.close();

            if (inputStream != null) {
                return inputStream;
            }
        }
        catch (final Throwable e) {
            Log.w(TAG,
                  "getInputStream: error while getting the stream for the given tile " + tile,
                  e);
        }

        return null;
    }

    @Override
    public void close() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG,
                  "close");
        }

        if (mDatabase != null) {
            mDatabase.close();
        }
    }
}
