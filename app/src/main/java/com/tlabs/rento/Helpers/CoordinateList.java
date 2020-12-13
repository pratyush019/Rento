package com.tlabs.rento.Helpers;

import android.os.Parcel;
import android.os.Parcelable;

public class CoordinateList implements Parcelable {
    private final double mLatitude;
    private final double mLongitude;
    private final String mBrand;
    private final String mAvailable;



    protected CoordinateList(Parcel in) {
        mLatitude = in.readDouble();
        mLongitude = in.readDouble();
        mBrand=in.readString();
        mAvailable=in.readString();
    }

    public static final Creator<CoordinateList> CREATOR = new Creator<CoordinateList>() {
        @Override
        public CoordinateList createFromParcel(Parcel in) {
            return new CoordinateList(in);
        }

        @Override
        public CoordinateList[] newArray(int size) {
            return new CoordinateList[size];
        }
    };

    public double getmLatitude() {
        return mLatitude;
    }

    public double getmLongitude() {
        return mLongitude;
    }

    public String getmBrand() {
        return mBrand;
    }

    public String getmAvailable() {
        return mAvailable;
    }

    public CoordinateList(double latitude, double longitude,String brand,String available)
    {
        mLatitude=latitude;
        mLongitude =longitude;
        mBrand=brand;
        mAvailable=available;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(mLatitude);
        dest.writeDouble(mLongitude);
        dest.writeString(mBrand);
        dest.writeString(mAvailable);

    }
}

