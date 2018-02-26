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
package com.github.instacart.ahoy.tests;

import com.github.instacart.ahoy.Ahoy;
import com.github.instacart.ahoy.LifecycleCallbackWrapper;
import com.github.instacart.ahoy.Storage;
import com.github.instacart.ahoy.Visit;
import com.github.instacart.ahoy.delegate.AhoyDelegate;

import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.UUID;

import timber.log.Timber;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class StoredVisitTest {

    private Ahoy ahoy;
    private AhoyDelegate delegate;
    private Storage storage;
    private LifecycleCallbackWrapper wrapper;

    @Before public void setup() {
        ahoy = new Ahoy();
        delegate = mock(AhoyDelegate.class);
        storage = mock(Storage.class);
        wrapper = mock(LifecycleCallbackWrapper.class);

        Timber.uprootAll();
    }

    @Test public void testStoredVisit() throws Exception {
        String token = UUID.randomUUID().toString();
        long inAnHour = System.currentTimeMillis() + 3600;
        Visit visit = Visit.create(token, Collections.emptyMap(), inAnHour);

        when(storage.readVisit(any(Visit.class))).thenReturn(visit);
        ahoy.init(storage, wrapper, delegate, true);

        assertEquals(visit, ahoy.visit());
    }
}