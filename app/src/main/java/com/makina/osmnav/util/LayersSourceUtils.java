package com.makina.osmnav.util;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.stream.JsonReader;
import com.makina.osmnav.model.LayerSource;
import com.makina.osmnav.model.LayersSource;

import org.osmdroid.util.BoundingBoxE6;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper class about {@link com.makina.osmnav.model.LayersSource}.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class LayersSourceUtils {

    private static final String TAG = LayersSourceUtils.class.getName();

    @Nullable
    public static LayersSource loadLayersSourceFromAssets(final Context context,
                                                          @NonNull final String layersSourceName) {
        final String assetResource = layersSourceName + ".json";

        try {
            return readLayersSource(new InputStreamReader(context.getAssets()
                                                                 .open(assetResource)));
        }
        catch (IOException ioe) {
            Log.w(TAG,
                  "I/O error: " + assetResource);
        }

        return null;
    }

    @Nullable
    public static LayersSource readLayersSource(@NonNull final Reader in) throws
                                                                          IOException {
        LayersSource layersSource = null;
        JsonReader jsonReader = new JsonReader(in);

        // noinspection TryFinallyCanBeTryWithResources
        try {
            jsonReader.beginObject();

            String layersSourceName = null;
            BoundingBoxE6 boundingBoxE6 = null;
            LayerSource layerSource = null;

            while (jsonReader.hasNext()) {
                final String name = jsonReader.nextName();

                switch (name) {
                    case "name":
                        layersSourceName = jsonReader.nextString();
                        break;
                    case "bbox":
                        jsonReader.beginArray();

                        double[] bboxArray = new double[4];
                        int i = 0;

                        while (jsonReader.hasNext()) {
                            final double value = jsonReader.nextDouble();

                            if (i < 4) {
                                bboxArray[i] = value;
                            }

                            i++;
                        }

                        jsonReader.endArray();

                        if (i == 4) {
                            boundingBoxE6 = new BoundingBoxE6(bboxArray[0],
                                                     bboxArray[1],
                                                     bboxArray[2],
                                                     bboxArray[3]);
                        }

                        break;
                    case "layers":
                        jsonReader.beginObject();

                        String base = null;
                        final List<String> layers = new ArrayList<>();

                        while (jsonReader.hasNext()) {
                            final String nameForLayers = jsonReader.nextName();

                            switch (nameForLayers) {
                                case "base":
                                    base = jsonReader.nextString();
                                    break;
                                case "layers":
                                    jsonReader.beginArray();

                                    while (jsonReader.hasNext()) {
                                        final String layerSourceName = jsonReader.nextString();

                                        if (!TextUtils.isEmpty(layerSourceName)) {
                                            layers.add(layerSourceName);
                                        }
                                    }

                                    jsonReader.endArray();
                            }
                        }

                        jsonReader.endObject();

                        if (!TextUtils.isEmpty(base)) {
                            layerSource = new LayerSource(base,
                                                          layers);
                        }

                        break;
                }
            }

            if (!TextUtils.isEmpty(layersSourceName) && (boundingBoxE6 != null) && (layerSource != null)) {
                layersSource = new LayersSource(layersSourceName,
                                                boundingBoxE6,
                                                layerSource);
            }
        }
        finally {
            jsonReader.close();
        }

        return layersSource;
    }
}
