package com.makina.osmnav.util;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.stream.JsonReader;
import com.makina.osmnav.model.LayerSource;
import com.makina.osmnav.model.LayersSettings;
import com.makina.osmnav.model.LayersSource;

import org.osmdroid.util.BoundingBoxE6;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Helper class about {@link LayersSettings}.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class LayersSettingsUtils {

    private static final String TAG = LayersSettingsUtils.class.getName();

    @Nullable
    public static LayersSettings loadLayersSettingsFromAssets(final Context context,
                                                              @NonNull final String layersSettingsName) {
        final String assetResource = layersSettingsName + ".json";

        try {
            return readLayersSettings(new InputStreamReader(context.getAssets()
                                                                   .open(assetResource)));
        }
        catch (IOException ioe) {
            Log.w(TAG,
                  "I/O error: " + assetResource);
        }

        return null;
    }

    @Nullable
    public static LayersSettings readLayersSettings(@NonNull final Reader in) throws
                                                                          IOException {
        LayersSettings layersSettings = null;
        JsonReader jsonReader = new JsonReader(in);

        // noinspection TryFinallyCanBeTryWithResources
        try {
            jsonReader.beginObject();

            String layersSettingsName = null;
            BoundingBoxE6 boundingBoxE6 = null;
            LayersSource layersSource = null;

            while (jsonReader.hasNext()) {
                final String name = jsonReader.nextName();

                switch (name) {
                    case "name":
                        layersSettingsName = jsonReader.nextString();
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

                        LayerSource base = null;
                        final List<LayerSource> layers = new ArrayList<>();

                        while (jsonReader.hasNext()) {
                            final String nameForLayers = jsonReader.nextName();

                            switch (nameForLayers) {
                                case "base":
                                    base = new LayerSource(jsonReader.nextString());
                                    break;
                                case "layers":
                                    jsonReader.beginArray();

                                    while (jsonReader.hasNext()) {
                                        final String layerSourceName = jsonReader.nextString();
                                        final Double level = parseLevel(layerSourceName);

                                        if (!TextUtils.isEmpty(layerSourceName) && (level != null)) {
                                            layers.add(new LayerSource(layerSourceName,
                                                                       level));
                                        }
                                    }

                                    jsonReader.endArray();
                            }
                        }

                        jsonReader.endObject();

                        if (base != null) {
                            layersSource = new LayersSource(base,
                                                            layers);
                        }

                        break;
                }
            }

            if (!TextUtils.isEmpty(layersSettingsName) && (boundingBoxE6 != null) && (layersSource != null)) {
                layersSettings = new LayersSettings(layersSettingsName,
                                                    boundingBoxE6,
                                                    layersSource);
            }
        }
        finally {
            jsonReader.close();
        }

        return layersSettings;
    }

    @Nullable
    private static Double parseLevel(@NonNull final String layer) {
        Double level = null;

        final Pattern patternLevel = Pattern.compile("^\\p{Alpha}+_(-?\\d.\\d+).*");
        final Matcher matcherLevel = patternLevel.matcher(layer);

        if (matcherLevel.find() && matcherLevel.groupCount() == 1) {
            try {
                level = Double.valueOf(matcherLevel.group(1));
            }
            catch (NumberFormatException nfe) {
                Log.w(TAG,
                      nfe.getMessage());
            }
        }

        return level;
    }
}
