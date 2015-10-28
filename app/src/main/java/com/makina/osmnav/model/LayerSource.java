package com.makina.osmnav.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Describes a layer source and its indoor levels.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class LayerSource
        implements Parcelable {

    private static final String TAG = LayerSource.class.getName();

    public String base;
    public final List<String> layers = new ArrayList<>();
    public final Set<Double> levels = new TreeSet<>(new Comparator<Double>() {
        @Override
        public int compare(Double lhs,
                           Double rhs) {
            return Double.compare(rhs,
                                  lhs);
        }
    });

    public LayerSource(@NonNull final String base,
                       @NonNull final List<String> layers) {
        this.base = base;

        for (String layer : layers) {
            final Double level = parseLevel(layer);

            if (level == null) {
                Log.w(TAG,
                      "wrong layer name: " + layer);

                continue;
            }

            this.layers.add(layer);
            this.levels.add(level);
        }
    }

    protected LayerSource(Parcel source) {
        this(source.readString(),
             source.createStringArrayList());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest,
                              int flags) {
        dest.writeString(base);
        dest.writeStringList(layers);
    }

    @Nullable
    private Double parseLevel(@NonNull final String layer) {
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

    public static final Parcelable.Creator<LayerSource> CREATOR = new Parcelable.Creator<LayerSource>() {
        @Override
        public LayerSource createFromParcel(Parcel source) {
            return new LayerSource(source);
        }

        @Override
        public LayerSource[] newArray(int size) {
            return new LayerSource[size];
        }
    };
}
