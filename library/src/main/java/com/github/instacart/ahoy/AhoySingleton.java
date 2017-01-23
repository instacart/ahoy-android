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
package com.github.instacart.ahoy;

import android.app.Application;
import android.support.annotation.Nullable;

import com.github.instacart.ahoy.Ahoy.VisitListener;
import com.github.instacart.ahoy.delegate.AhoyDelegate;

import java.util.Map;

import rx.Observable;

public class AhoySingleton {

    private static Ahoy sInstance;

    public static void init(Application application, AhoyDelegate delegate, boolean autoStart) {
        sInstance = new Ahoy();
        LifecycleCallbackWrapper wrapper = new LifecycleCallbackWrapper();
        application.registerActivityLifecycleCallbacks(wrapper);
        sInstance.init(new Storage(application), wrapper, delegate, autoStart);
    }

    @Nullable public static Visit visit() {
        return sInstance.visit();
    }

    public static String visitorToken() {
        return sInstance.visitorToken();
    }

    public static void addVisitListener(VisitListener listener) {
        sInstance.addVisitListener(listener);
    }

    public static void removeVisitListener(VisitListener listener) {
        sInstance.removeVisitListener(listener);
    }

    public static Observable<Visit> visitStream() {
        return RxAhoy.visitStream(sInstance);
    }

    public static void newVisit(Map<String, Object> extraParams) {
        sInstance.newVisit(extraParams);
    }

    public static void saveExtras(Map<String, Object> extraParams) {
        sInstance.saveExtras(extraParams);
    }
}