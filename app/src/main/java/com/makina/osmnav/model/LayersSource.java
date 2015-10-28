package com.makina.osmnav.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import org.osmdroid.util.BoundingBoxE6;

/**
 * Describes layers source.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class LayersSource
        implements Parcelable {

    public String name;
    public BoundingBoxE6 boundingBoxE6;
    public LayerSource layerSource;

    public LayersSource(@NonNull final String name,
                        @NonNull final BoundingBoxE6 boundingBoxE6,
                        @NonNull final LayerSource layerSource) {
        this.name = name;
        this.boundingBoxE6 = boundingBoxE6;
        this.layerSource = layerSource;
    }

    protected LayersSource(Parcel source) {
        name = source.readString();
        boundingBoxE6 = source.readParcelable(BoundingBoxE6.class.getClassLoader());
        layerSource = source.readParcelable(LayerSource.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest,
                              int flags) {
        dest.writeString(name);
        dest.writeParcelable(boundingBoxE6,
                             flags);
        dest.writeParcelable(layerSource,
                             flags);
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
