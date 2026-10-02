package com.jm.trackwallet.ui.home.fragment.home;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.PieChart;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.jm.trackwallet.R;

public class HomeFragment extends Fragment {
    private PieChart chart;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;

    public HomeFragment() {
        // Required empty public constructor
    }


    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        inizializza_tablayout(view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


    }

    public void inizializza_tablayout(View view) {
        tabLayout = view.findViewById(R.id.tabLayout);
        viewPager = view.findViewById(R.id.viewPager);

        tabLayout.setTabRippleColor(ColorStateList.valueOf(Color.TRANSPARENT));


        // Setting up the ViewPager with a FragmentStateAdapter
        FragmentStateAdapter fragmentStateAdapter = new FragmentStateAdapter(getChildFragmentManager(), getLifecycle()) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                // Returning different fragments for different tabs
                switch (position) {
                    case 0:
                        return new TotaleEntrateUsciteFragment(); // Crea il tuo primo fragment
                    case 1:
                        return new SpeseHomeFragment(); // Crea il tuo primo fragment
                    case 2:
                        return new EntrateHomeFragment(); // Crea il tuo secondo fragment
                    default:
                        return new SpeseHomeFragment();
                }
            }

            @Override
            public int getItemCount() {
                return 3; // Il numero di tabs
            }
        };

        viewPager.setAdapter(fragmentStateAdapter);

        // Set up TabLayout with ViewPager2
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText(R.string.totale);
                    break;
                case 1:
                    tab.setText(R.string.spese);
                    break;
                case 2:
                    tab.setText(R.string.entrate);
                    break;
            }
        }).attach();
    }
}