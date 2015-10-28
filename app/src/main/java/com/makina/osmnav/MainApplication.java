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

package com.makina.osmnav;

import android.app.Application;
import android.support.annotation.Nullable;

import com.makina.osmnav.model.LayersSettings;
import com.makina.osmnav.util.LayersSettingsUtils;

import java.lang.ref.WeakReference;

/**
 * Base class to maintain global application state and prepare the application context.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class MainApplication
        extends Application {

    private static WeakReference<MainApplication> sInstance;

    // TODO: remove it from MainApplication
    private LayersSettings mLayersSettings;

    @Override
    public void onCreate() {
        super.onCreate();

        sInstance = new WeakReference<>(this);

        // TODO: use Loader instead
        mLayersSettings = LayersSettingsUtils.loadLayersSettingsFromAssets(this,
                                                                           getString(R.string.layers_settings));
    }

    @Nullable
    public static MainApplication getInstance() {
        return sInstance == null ? null : sInstance.get();
    }

    // TODO: remove it from MainApplication
    @Nullable
    public LayersSettings getLayersSettings() {
        return mLayersSettings;
    }
}
