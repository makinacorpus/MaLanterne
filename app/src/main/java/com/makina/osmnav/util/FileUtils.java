package com.makina.osmnav.util;

import android.content.Context;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.File;

/**
 * Helper class about {@code File}.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class FileUtils {

    private static final String TAG = FileUtils.class.getName();

    /**
     * {@link FileUtils} instances should NOT be constructed in standard programming.
     */
    private FileUtils() {

    }

    /**
     * Gets the relative path used by this context.
     *
     * @param context the current context
     *
     * @return the relative path
     */
    @NonNull
    public static String getRelativeSharedPath(@NonNull final Context context) {
        return "Android" + File.separator + "data" + File.separator + context.getPackageName() + File.separator;
    }

    /**
     * Gets a given filename as {@code File} according to the current application storage used
     * (default or external storage).
     *
     * @param context  the current context
     * @param filename filename to load as {@code File}
     *
     * @return the filename to load as {@code File} of {@code null} if the given filename cannot be
     * loaded
     */
    @Nullable
    public static File getFileFromApplicationStorage(Context context,
                                                     String filename) {
        File externalStorageDirectory = Environment.getExternalStorageDirectory();

        if (externalStorageDirectory == null) {
            Log.w(TAG,
                  "unable to load '" + filename + "'");

            return null;
        }

        return new File(externalStorageDirectory.getPath() + File.separator + getRelativeSharedPath(context) + filename);
    }
}
