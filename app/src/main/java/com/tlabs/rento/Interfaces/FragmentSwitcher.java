package com.tlabs.rento.Interfaces;

//This fragment is responsible for switching view on main login screen since
//Can be used where simple view changing is involved without transfer of data
public interface FragmentSwitcher {
    void changeToFragment(int position);
    void lockViewPager(boolean value);
}