/*
 * Copyright (c) 2015, Makina Corpus
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 *  Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.makina.osmnav.ui.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.OnNavigationListener;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.makina.osmnav.R;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Switch level using navigation mode as list.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
@SuppressWarnings("deprecation")
public class LevelsFilterNavigationListView
        implements OnNavigationListener {

    private static final String TAG = LevelsFilterNavigationListView.class.getName();

    private LevelAdapter mLevelAdapter;
    private ActionBar mActionBar;
    private Set<Double> mLevels = new TreeSet<>(new Comparator<Double>() {
        @Override
        public int compare(Double lhs,
                           Double rhs) {
            return Double.compare(rhs,
                                  lhs);
        }
    });

    private Double mDefaultLevel;
    private LevelsFilterViewCallback mLevelsFilterViewCallback;

    public LevelsFilterNavigationListView(ActionBar pActionBar) {
        this.mLevelAdapter = new LevelAdapter(pActionBar.getThemedContext());
        this.mActionBar = pActionBar;
        this.mDefaultLevel = 0d;
    }

    @NonNull
    public Set<Double> getLevels() {
        if (mLevels == null) {
            return Collections.emptySet();
        }

        return mLevels;
    }

    public void setDefaultLevel(Double pDefaultLevel) {
        this.mDefaultLevel = pDefaultLevel;
    }

    public void setLevelsFilterViewCallback(@NonNull final LevelsFilterViewCallback pLevelsFilterViewCallback) {
        this.mLevelsFilterViewCallback = pLevelsFilterViewCallback;
    }

    public void setLevels(@NonNull final List<Double> levels) {
        mLevels.addAll(levels);
        mLevelAdapter.clear();

        for (Double level : mLevels) {
            mLevelAdapter.add(level);
        }

        mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        mActionBar.setListNavigationCallbacks(mLevelAdapter,
                                              this);

        if (mLevels.contains(mDefaultLevel)) {
            mActionBar.setSelectedNavigationItem(mLevelAdapter.getPosition(mDefaultLevel));
        }
    }

    public void show() {
        mActionBar.setNavigationMode(mLevelAdapter.isEmpty() ? ActionBar.NAVIGATION_MODE_STANDARD: ActionBar.NAVIGATION_MODE_LIST);
    }

    public void hide() {
        mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
    }

    @Override
    public boolean onNavigationItemSelected(int itemPosition,
                                            long itemId) {
        try {
            if (mLevelsFilterViewCallback != null) {
                mLevelsFilterViewCallback.onLevelSelected(mLevelAdapter.getItem(itemPosition));

                return true;
            }
        }
        catch (NumberFormatException nfe) {
            Log.w(TAG,
                  nfe.getMessage());
        }

        return false;
    }

    @Deprecated
    private class LevelAdapter
            extends ArrayAdapter<Double> {

        private final DecimalFormat levelFormat = new DecimalFormat("0.#");

        public LevelAdapter(Context context) {
            super(context,
                  R.layout.level_spinner_item);
            setDropDownViewResource(R.layout.level_spinner_dropdown_item);
        }

        @Override
        public View getView(int position,
                            View convertView,
                            ViewGroup parent) {
            final View view = super.getView(position,
                                            convertView,
                                            parent);

            if (view instanceof TextView) {
                ((TextView) view).setText(getContext().getString(R.string.action_select_level,
                                                                 levelFormat.format(getItem(position))));
            }

            return view;
        }

        @Override
        public View getDropDownView(int position,
                                    View convertView,
                                    ViewGroup parent) {
            final View view = super.getDropDownView(position,
                                                    convertView,
                                                    parent);

            if (view instanceof TextView) {
                ((TextView) view).setText(levelFormat.format(getItem(position)));
            }

            return view;
        }
    }

    /**
     * Callback interface for {@link LevelsFilterNavigationListView}.
     *
     * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
     */
    public interface LevelsFilterViewCallback {

        void onLevelSelected(final double level);
    }
}
