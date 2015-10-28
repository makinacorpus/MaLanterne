package com.makina.osmnav.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Describes a layer source and its indoor levels.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class LayerSource
        implements Parcelable {

    @NonNull
    public final String source;

    @Nullable
    public final Double level;

    public LayerSource(@NonNull final String source) {
        this(source,
             null);
    }

    public LayerSource(@NonNull final String source,
                       @Nullable final Double level) {
        this.source = source;
        this.level = level;
    }

    protected LayerSource(Parcel source) {
        this.source = source.readString();
        this.level = (Double) source.readSerializable();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest,
                              int flags) {
        dest.writeString(source);
        dest.writeSerializable(level);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof LayerSource)) {
            return false;
        }

        LayerSource that = (LayerSource) o;

        // noinspection SimplifiableIfStatement
        if (!source.equals(that.source)) {
            return false;
        }

        return !(level != null ? !level.equals(that.level) : that.level != null);
    }

    @Override
    public int hashCode() {
        int result = source.hashCode();
        result = 31 * result + (level != null ? level.hashCode() : 0);

        return result;
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
