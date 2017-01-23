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

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.VisibleForTesting;

import com.github.instacart.ahoy.utils.ActivityLifecycleCallbacksStub;

@VisibleForTesting
public class LifecycleCallbackWrapper extends ActivityLifecycleCallbacksStub {

    private Listener mListener;

    public interface Listener {

        void onActivityCreated();
        void onActivityStarted();
        void onLastOnStop();
    }

    public LifecycleCallbackWrapper() {
    }

    private int visibleActivitiesCounter = 0;

    @Override public void onActivityCreated(Activity activity, Bundle bundle) {
        super.onActivityCreated(activity, bundle);
        mListener.onActivityCreated();
    }

    @Override public void onActivityStarted(Activity activity) {
        visibleActivitiesCounter++;
        mListener.onActivityStarted();
    }

    @Override public void onActivityStopped(Activity activity) {
        visibleActivitiesCounter--;

        if (visibleActivitiesCounter == 0) {
            mListener.onLastOnStop();
        }
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }
}