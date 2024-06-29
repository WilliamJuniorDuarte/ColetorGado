package br.com.wjd.fragments;

import android.app.ActivityManager;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import br.com.wjd.R;
import br.com.wjd.adapters.MyFragmentPagerAdapter;

public class Configuracao extends AppCompatActivity {

    public static final String PREF_NAME = "wjdPreferences";
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracao);

        //Menu Suspenso
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_configuracao);
        setSupportActionBar(toolbar);

        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setHomeAsUpIndicator(R.drawable.baseline_arrow_white_24);
        ab.setTitle(getString(R.string.config));

        //SharedPreferences
        final SharedPreferences spViaOnda = this.getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        final SharedPreferences.Editor ed = spViaOnda.edit();

        mViewPager = (ViewPager) findViewById(R.id.view_pager);

        mViewPager.setAdapter(new MyFragmentPagerAdapter(getSupportFragmentManager(), "Configurações"));

    }

    //Botão Menu
    @Override
    protected void onPause() {
        super.onPause();
        ActivityManager activityManager = (ActivityManager) getApplicationContext().getSystemService(Inicial.ACTIVITY_SERVICE);
        activityManager.moveTaskToFront(getTaskId(), 0);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}