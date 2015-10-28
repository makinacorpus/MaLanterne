package com.makina.osmnav.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Describes a set of {@link LayerSource}.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class LayersSource
        implements Parcelable {

    @NonNull
    public final LayerSource base;

    @NonNull
    public final List<LayerSource> layers = new ArrayList<>();

    public LayersSource(@NonNull final LayerSource base,
                        @NonNull final List<LayerSource> layers) {
        this.base = base;
        this.layers.addAll(layers);
    }

    protected LayersSource(Parcel source) {
        base = source.readParcelable(LayerSource.class.getClassLoader());
        source.readTypedList(layers,
                             LayerSource.CREATOR);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest,
                              int flags) {
        dest.writeParcelable(base,
                             flags);
        dest.writeTypedList(layers);
    }

    public static final Parcelable.Creator<LayersSource> CREATOR = new Parcelable.Creator<LayersSource>() {
        @Override
        public LayersSource createFromParcel(Parcel source) {
            return new LayersSource(source);
        }

        @Override
        public LayersSource[] newArray(int size) {
            return new LayersSource[size];
        }
    };
}
