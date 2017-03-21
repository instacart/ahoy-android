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

import com.github.instacart.ahoy.Ahoy.VisitListener;

import rx.Emitter;
import rx.Emitter.BackpressureMode;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Cancellable;

public class RxAhoy {

    private RxAhoy() {
    }

    public static Observable<Visit> visitStream(final Ahoy ahoy) {
        return Observable.create(new Action1<Emitter<Visit>>() {
            @Override public void call(final Emitter<Visit> emitter) {
                final VisitListener listener = new VisitListener() {
                    @Override public void onVisitUpdated(Visit visit) {
                        emitter.onNext(visit);
                    }
                };

                ahoy.addVisitListener(listener);

                emitter.setCancellation(new Cancellable() {
                    @Override public void cancel() throws Exception {
                        ahoy.removeVisitListener(listener);
                    }
                });
            }
        }, BackpressureMode.LATEST);
    }
}