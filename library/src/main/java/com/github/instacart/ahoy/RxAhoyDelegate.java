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

import androidx.annotation.NonNull;

import com.github.instacart.ahoy.delegate.AhoyDelegate;
import com.github.instacart.ahoy.delegate.AhoyDelegate.AhoyCallback;
import com.github.instacart.ahoy.delegate.VisitParams;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;


public class RxAhoyDelegate {

    private RxAhoyDelegate() {
    }

    public static Flowable<Visit> createSaveExtrasStream(final AhoyDelegate delegate, final VisitParams params) {

        return Flowable.create(emitter -> delegate.saveExtras(params, new AhoyCallback() {
            @Override public void onSuccess(@NonNull Visit visit) {
                emitter.onNext(visit);
                emitter.onComplete();
            }

            @Override public void onFailure(Throwable throwable) {
                emitter.tryOnError(throwable);
            }
        }), BackpressureStrategy.LATEST);
    }

    public static Flowable<Visit> createNewVisitStream(final AhoyDelegate delegate, final VisitParams params) {

        return Flowable.create(emitter -> delegate.saveVisit(params, new AhoyCallback() {
            @Override public void onSuccess(@NonNull Visit visit) {
                emitter.onNext(visit);
                emitter.onComplete();
            }

            @Override public void onFailure(Throwable throwable) {
                emitter.tryOnError(throwable);
            }
        }), BackpressureStrategy.LATEST);
    }
}
