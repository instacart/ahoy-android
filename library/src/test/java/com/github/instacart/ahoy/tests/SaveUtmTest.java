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

import androidx.collection.ArrayMap;

import com.github.instacart.ahoy.Ahoy;
import com.github.instacart.ahoy.LifecycleCallbackWrapper;
import com.github.instacart.ahoy.Storage;
import com.github.instacart.ahoy.Visit;
import com.github.instacart.ahoy.delegate.AhoyDelegate;
import com.github.instacart.ahoy.delegate.VisitParams;

import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SaveUtmTest {

    private Ahoy ahoy;
    private AhoyDelegate delegate;
    private Storage storage;
    private LifecycleCallbackWrapper wrapper;
    private Visit visit;
    private String visitorToken;

    @Before public void setupAhoy() {
        ahoy = new Ahoy();
        delegate = mock(AhoyDelegate.class);
        storage = mock(Storage.class);
        wrapper = new LifecycleCallbackWrapper();
        visitorToken = UUID.randomUUID().toString();

        long tomorrow = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1);
        visit = Visit.create(visitorToken, Collections.emptyMap(), tomorrow);

        when(storage.readVisitorToken(nullable(String.class))).thenReturn(visitorToken);
        when(storage.readVisit(nullable(Visit.class))).thenReturn(visit);
    }

    @Test public void testStoringVisit() throws Exception {

        Map<String, Object> params = new ArrayMap<>();
        params.put(Visit.UTM_CAMPAIGN, "campaign");
        params.put(Visit.UTM_CONTENT, "content");
        params.put(Visit.UTM_MEDIUM, "medium");
        params.put(Visit.UTM_SOURCE, "source");
        params.put(Visit.UTM_TERM, "term");

        final VisitParams visitParams = VisitParams.create(visitorToken, visit, params);

        final Visit visit = Visit.create(
                UUID.randomUUID().toString(),
                params,
                System.currentTimeMillis() + 36000);

        delegate = new AhoyDelegate() {
            @Override public String newVisitorToken() {
                fail();
                return null;
            }

            @Override public void saveVisit(VisitParams params, AhoyCallback callback) {
                fail();
            }

            @Override public void saveExtras(VisitParams params, AhoyCallback callback) {
                callback.onSuccess(visit);
                assertEquals(visitParams, params);
            }
        };
        final CountDownLatch latch = new CountDownLatch(1);
        ahoy.init(storage, wrapper, delegate, true);
        ahoy.addVisitListener(ignored -> latch.countDown());
        wrapper.onActivityCreated(null, null);

        ahoy.saveExtras(params);

        assertTrue(latch.await(2000, TimeUnit.MILLISECONDS));
        verify(storage).saveVisit(visit);
        assertEquals(ahoy.visit(), visit);
    }

    @Test public void testStoringWithNoVisit() throws Exception {

        final Map<String, Object> utmParams = new ArrayMap<>();
        utmParams.put(Visit.UTM_CAMPAIGN, "campaign");
        utmParams.put(Visit.UTM_CONTENT, "content");
        utmParams.put(Visit.UTM_MEDIUM, "medium");
        utmParams.put(Visit.UTM_SOURCE, "source");
        utmParams.put(Visit.UTM_TERM, "term");

        when(storage.readVisit(nullable(Visit.class))).thenReturn(Visit.empty());

        final Visit updatedVisit = visit.withUpdatedExtraParams(utmParams);
        delegate = new AhoyDelegate() {
            @Override public String newVisitorToken() {
                fail();
                return null;
            }

            @Override public void saveVisit(VisitParams params, AhoyCallback callback) {
                assertNull(params.visit());
                callback.onSuccess(visit);
            }

            @Override public void saveExtras(VisitParams params, AhoyCallback callback) {
                assertEquals(VisitParams.create(visitorToken, visit, utmParams), params);
                callback.onSuccess(updatedVisit);
            }
        };
        final CountDownLatch latch = new CountDownLatch(2);
        ahoy.init(storage, wrapper, delegate, true);
        ahoy.addVisitListener(ignored -> latch.countDown());
        wrapper.onActivityCreated(null, null);
        ahoy.saveExtras(utmParams);

        assertTrue(latch.await(2000, TimeUnit.MILLISECONDS));
        verify(storage).saveVisit(visit);
        verify(storage).saveVisit(updatedVisit);
        assertEquals(ahoy.visit(), updatedVisit);
    }
}
