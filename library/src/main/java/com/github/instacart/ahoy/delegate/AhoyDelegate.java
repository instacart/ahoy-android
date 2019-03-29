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
package com.github.instacart.ahoy.delegate;

import androidx.annotation.NonNull;

import com.github.instacart.ahoy.Visit;

public interface AhoyDelegate {

    interface AhoyCallback {
        void onSuccess(@NonNull Visit visit);
        void onFailure(Throwable throwable);
    }

    /***
     * New visitor token generator.
     *
     * @return Unique string identifying current client.
     */
    String newVisitorToken();

    /**
     * Start a new visit with extra parameters.
     *
     * @param params Visit information & extra parameters (such as utm parameters, see {@link Visit}).
     * @param callback AhoyCallback reporting success or failure & returning new visit.
     */
    void saveVisit(VisitParams params, AhoyCallback callback);

    /**
     * Save extra parameters. Visit may or may not be started.
     *
     * @param params Visit information & extra parameters (such as utm parameters, see {@link Visit}).
     * @param callback AhoyCallback reporting success or failure & returning updated or new visit.
     */
    void saveExtras(VisitParams params, AhoyCallback callback);
}

