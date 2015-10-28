package com.makina.osmnav.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import org.osmdroid.util.BoundingBoxE6;

/**
 * Describes layers settings about {@link LayersSource}.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class LayersSettings
        implements Parcelable {

    @NonNull
    public final String name;

    @NonNull
    public final BoundingBoxE6 boundingBoxE6;

    @NonNull
    public final LayersSource layersSource;

    public LayersSettings(@NonNull final String name,
                          @NonNull final BoundingBoxE6 boundingBoxE6,
                          @NonNull final LayersSource layersSource) {
        this.name = name;
        this.boundingBoxE6 = boundingBoxE6;
        this.layersSource = layersSource;
    }

    protected LayersSettings(Parcel source) {
        name = source.readString();
        boundingBoxE6 = source.readParcelable(BoundingBoxE6.class.getClassLoader());
        layersSource = source.readParcelable(LayersSource.class.getClassLoader());
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
        dest.writeParcelable(layersSource,
                             flags);
    }

    public static final Parcelable.Creator<LayersSettings> CREATOR = new Parcelable.Creator<LayersSettings>() {
        @Override
        public LayersSettings createFromParcel(Parcel source) {
            return new LayersSettings(source);
        }

        @Override
        public LayersSettings[] newArray(int size) {
            return new LayersSettings[size];
        }
    };
}
