package br.com.wjd.adapters;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import br.com.wjd.fragments.Config;

public class MyFragmentPagerAdapter extends FragmentPagerAdapter {

    private String Title;

    public MyFragmentPagerAdapter(FragmentManager fm, String Title) {
        super(fm);
        this.Title = Title;
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                return new Config();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 1;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return this.Title;
    }
}