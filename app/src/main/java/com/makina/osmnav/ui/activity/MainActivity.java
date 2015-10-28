package com.makina.osmnav.ui.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.makina.osmnav.MainApplication;
import com.makina.osmnav.R;
import com.makina.osmnav.ui.fragment.MapFragment;

/**
 * Main {@code Activity}.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 * @see MapFragment
 */
public class MainActivity
        extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_single);

        if (savedInstanceState == null) {
            final MainApplication mainApplication = MainApplication.getInstance();

            if (mainApplication != null) {
                getSupportFragmentManager().beginTransaction()
                                           .replace(R.id.container,
                                                    MapFragment.newInstance(mainApplication.getLayersSettings()))
                                           .commit();
            }
        }
    }
}
