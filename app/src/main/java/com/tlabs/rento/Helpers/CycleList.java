package com.tlabs.rento.Helpers;

public class CycleList{
        private final String mCycleName;
        private final String mCycleImageId;
        private final String mCycleAvailability;
        private final String mCycleNote;
        private final String mPhone;
        private final String mLat;
        private final String mLon;
        private final String mRenterUid;
        public CycleList(String cycleName, String cycleAvailability, String cycleImageURL, String Note, String Phone,
                     String Lat, String Lon,String renterUid)
        {
            mCycleImageId=cycleImageURL;
            mCycleName =cycleName;
            mCycleAvailability=cycleAvailability;
            mCycleNote=Note;
            mPhone=Phone;
            mLat=Lat;
            mLon=Lon;
            mRenterUid=renterUid;
        }
        public String getCycleName()
        {
            return mCycleName;
        }
        public String getCycleImageURL()
        {
            return mCycleImageId;
        }
        public String getCycleAvailability(){return mCycleAvailability;}
        public String getCycleNote() {
            return mCycleNote;
        }
        public String getPhone() {
            return mPhone;
        }
        public String getLat() {
            return mLat;
        }
        public String getLon() {
            return mLon;
        }
        public String getRenterUid() {
            return mRenterUid;
        }
}
