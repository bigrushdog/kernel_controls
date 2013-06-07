
package com.brd.apps.kernelcontrols;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.app.ActionBar;
import android.app.Fragment;
import android.app.Activity;

import java.util.ArrayList;

public class Main extends Activity {
    ImageView mBackIndicator;
    ImageView mIcon;
    TextView mTitle;
    ActionBar mBar;

    ArrayList<String> mFragmentsTitleList = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            savedInstanceState.remove("android:fragments");
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mBar = getActionBar();
        mBar.setDisplayHomeAsUpEnabled(true);
        Fragment f = Performance.newInstance();
        showFragment(f);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Main.this.onBackPressed();
                break;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStack();
        } else {
            finish();
        }
    }

    private void showFragment(Fragment f) {
        getFragmentManager().beginTransaction()
                .add(R.id.container, f)
                .commit();
    }
}
