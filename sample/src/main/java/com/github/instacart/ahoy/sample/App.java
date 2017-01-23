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
package com.github.instacart.ahoy.sample;

import android.app.Application;

import com.github.instacart.ahoy.AhoySingleton;
import com.github.instacart.ahoy.delegate.DeviceInfo;
import com.github.instacart.ahoy.delegate.retrofit2.Retrofit2Delegate;
import com.github.instacart.ahoy.utils.SimpleDeviceInfo;

public class App extends Application {

    @Override public void onCreate() {
        super.onCreate();
        DeviceInfo deviceInfo = new SimpleDeviceInfo();
        String demoUrl = "https://murmuring-ocean-69755.herokuapp.com/";
        AhoySingleton.init(this, Retrofit2Delegate.factory(demoUrl, 60 * 1000, deviceInfo, true), true);
    }
}
