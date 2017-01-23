/*
 * Copyright (C) 2016 Maplebear Inc., d/b/a Instacart
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.instacart.ahoy.utils;

import android.content.res.Resources;
import android.os.Build;
import android.util.DisplayMetrics;

import com.github.instacart.ahoy.BuildConfig;
import com.github.instacart.ahoy.Visit;
import com.github.instacart.ahoy.delegate.DeviceInfo;

public class SimpleDeviceInfo implements DeviceInfo {

    public SimpleDeviceInfo() {
    }

    @Override public int getScreenWidthDp() {
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        return (int) (metrics.widthPixels / metrics.density);
    }

    @Override public int getScreenHeightDp() {
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        return (int) (metrics.heightPixels / metrics.density);
    }

    @Override public String getUserAgent() {
        String appVersion = BuildConfig.VERSION_NAME;
        String model = Build.MODEL;
        String osVersion = Build.VERSION.RELEASE;
        return String.format("Android-%s %s %s", osVersion, model, appVersion);
    }

    @Override public String getOs() {
        return Visit.OS_ANDROID;
    }
}
