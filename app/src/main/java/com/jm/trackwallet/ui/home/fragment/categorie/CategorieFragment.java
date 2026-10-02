package com.jm.trackwallet.ui.home.fragment.categorie;

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

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.jm.trackwallet.R;


public class CategorieFragment extends Fragment {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;

    public CategorieFragment() {
        // Required empty public constructor
    }

    public static CategorieFragment newInstance(String param1, String param2) {
        CategorieFragment fragment = new CategorieFragment();
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
        View view= inflater.inflate(R.layout.fragment_categorie, container, false);
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
        viewPager.setOffscreenPageLimit(3);
        // Setting up the ViewPager with a FragmentStateAdapter
        FragmentStateAdapter fragmentStateAdapter = new FragmentStateAdapter(getChildFragmentManager(), getLifecycle()) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                // Returning different fragments for different tabs
                switch (position) {
                    case 0:
                        return new SpeseCategorieFragment();
                    case 1:
                        return new EntrateCategorieFragment();
                    case 2:
                        return new ContiFragment();
                    default:
                        return new SpeseCategorieFragment();
                }
            }

            @Override
            public int getItemCount() {
                return 3;
            }
        };

        viewPager.setAdapter(fragmentStateAdapter);

        // Set up TabLayout with ViewPager2
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText(R.string.spese);
                    break;
                case 1:
                    tab.setText(R.string.entrate);
                    break;
                case 2:
                    tab.setText(R.string.conti);
                    break;
            }
        }).attach();
    }
}