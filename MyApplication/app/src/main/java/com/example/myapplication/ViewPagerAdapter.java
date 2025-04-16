package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.myapplication.fragments.CabsFragment;
import com.example.myapplication.fragments.FoodFragment;
import com.example.myapplication.fragments.GroceryFragment;

public class ViewPagerAdapter extends FragmentStateAdapter {

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new FoodFragment();
            case 1:
                return new GroceryFragment();
            case 2:
                return new CabsFragment();
            default:
                return new FoodFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3; // Three tabs: Food, Grocery, and Cabs
    }
}

