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
import com.github.instacart.ahoy.delegate.VisitParams;

import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import timber.log.Timber;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class NewVisitTest {

    private Ahoy ahoy;
    private AhoyDelegate delegate;
    private Storage storage;
    private LifecycleCallbackWrapper wrapper;
    private String visitorToken;

    @Before public void setupAhoy() {
        ahoy = new Ahoy();
        delegate = mock(AhoyDelegate.class);
        storage = mock(Storage.class);
        wrapper = new LifecycleCallbackWrapper();
        visitorToken = UUID.randomUUID().toString();
        when(storage.readVisitorToken(nullable(String.class))).thenReturn(visitorToken);
        when(storage.readVisit(nullable(Visit.class))).thenReturn(Visit.empty());

        Timber.uprootAll();
    }

    @Test public void requestNewVisitIfNoVisitStored() throws Exception {

        final VisitParams visitParams = VisitParams.create(visitorToken, null, null);

        final CountDownLatch latch = new CountDownLatch(1);
        delegate = new AhoyDelegate() {
            @Override public String newVisitorToken() {
                fail();
                return null;
            }

            @Override public void saveVisit(VisitParams params, AhoyCallback callback) {
                latch.countDown();
                assertEquals(visitParams, params);
            }

            @Override public void saveExtras(VisitParams params, AhoyCallback callback) {
                fail();
            }
        };
        ahoy.init(storage, wrapper, delegate, true);
        wrapper.onActivityCreated(null, null);
        assertTrue(latch.await(2000, TimeUnit.MILLISECONDS));
    }

    @Test public void requestNewVisitIfVisitExpired() throws Exception {
        Visit visit = Visit.empty().expire();
        when(storage.readVisit(nullable(Visit.class))).thenReturn(visit);
        final VisitParams visitParams = VisitParams.create(visitorToken, null, null);
        final CountDownLatch latch = new CountDownLatch(1);
        delegate = new AhoyDelegate() {
            @Override public String newVisitorToken() {
                fail();
                return null;
            }

            @Override public void saveVisit(VisitParams params, AhoyCallback callback) {
                latch.countDown();
                assertEquals(visitParams, params);
            }

            @Override public void saveExtras(VisitParams params, AhoyCallback callback) {
                fail();
            }
        };
        ahoy.init(storage, wrapper, delegate, true);
        wrapper.onActivityCreated(null, null);
        assertTrue(latch.await(2000, TimeUnit.MILLISECONDS));
    }

    @Test public void testStoringVisit() throws Exception {
        final VisitParams visitParams = VisitParams.create(visitorToken, null, null);

        final Visit visit = Visit.create(
                UUID.randomUUID().toString(),
                Collections.emptyMap(),
                System.currentTimeMillis() + 3600);

        delegate = new AhoyDelegate() {
            @Override public String newVisitorToken() {
                fail();
                return null;
            }

            @Override public void saveVisit(VisitParams params, AhoyCallback callback) {
                callback.onSuccess(visit);
                assertEquals(visitParams, params);
            }

            @Override public void saveExtras(VisitParams params, AhoyCallback callback) {
                fail();
            }
        };
        final CountDownLatch latch = new CountDownLatch(1);
        ahoy.init(storage, wrapper, delegate, true);
        ahoy.addVisitListener(ignored -> latch.countDown());
        wrapper.onActivityCreated(null, null);
        assertTrue(latch.await(2000, TimeUnit.MILLISECONDS));
        verify(storage, timeout(1000)).saveVisit(visit);
    }
}