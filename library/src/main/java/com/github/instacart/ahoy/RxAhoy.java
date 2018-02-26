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

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;

public class RxAhoy {

    private RxAhoy() {
    }

    public static Flowable<Visit> visitStream(final Ahoy ahoy) {
        return Flowable.create(emitter -> {
            final VisitListener listener = emitter::onNext;
            ahoy.addVisitListener(listener);
            emitter.setCancellable(() -> ahoy.removeVisitListener(listener));
        }, BackpressureStrategy.LATEST);
    }
}