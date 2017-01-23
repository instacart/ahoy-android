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

import android.support.v4.util.ArrayMap;

import com.github.instacart.ahoy.Ahoy;
import com.github.instacart.ahoy.Ahoy.VisitListener;
import com.github.instacart.ahoy.LifecycleCallbackWrapper;
import com.github.instacart.ahoy.Storage;
import com.github.instacart.ahoy.Visit;
import com.github.instacart.ahoy.delegate.AhoyDelegate;
import com.github.instacart.ahoy.delegate.VisitParams;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FreshVisitTest {

    private Ahoy ahoy;
    private PassThroughDelegate delegate;
    private Storage storage;
    private LifecycleCallbackWrapper wrapper;
    private String visitorToken;

    private CompositeSubscription compositeSubscription = new CompositeSubscription();

    class NoOp<T> implements Action1<T> {
        @Override public void call(T t) {
        }
    }

    class PassThroughDelegate implements AhoyDelegate {

        private int nextTokenIndex = 0;
        private List<String> nextTokens = Arrays.asList("0", "1", "2", "3");

        private String getNextToken() {
            String token = nextTokens.get(nextTokenIndex++);
            return token;
        }

        @Override public String newVisitorToken() {
            return visitorToken;
        }

        @Override public void saveVisit(final VisitParams params, final AhoyCallback callback) {
            compositeSubscription.add(Observable
                    .fromCallable(new Callable<Object>() {
                        @Override public Object call() throws Exception {
                            callback.onSuccess(generateVisit(params));
                            return null;
                        }
                    })
            .delay(200, TimeUnit.MILLISECONDS)
            .subscribe(new NoOp<>()));
        }

        @Override public void saveExtras(final VisitParams params, final AhoyCallback callback) {
            compositeSubscription.add(Observable
                    .fromCallable(new Callable<Object>() {
                        @Override public Object call() throws Exception {
                            Visit visit = params.visit();
                            String visitToken = visit.visitToken();
                            callback.onSuccess(Visit.create(visitToken, params.extraParams(), visit.expiresAt()));
                            return null;
                        }
                    })
            .delay(200, TimeUnit.MILLISECONDS)
            .subscribe(new NoOp<>()));
        }

        Visit generateVisit(VisitParams params) {
            final long tomorrow = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1);
            return Visit.create(getNextToken(), params.extraParams(), tomorrow);
        }
    }

    class FailingListener implements VisitListener {
        @Override public void onVisitUpdated(Visit visit) {
            fail();
        }
    }

    class BlockingOneUpdateListener implements VisitListener {
        Ahoy ahoy;
        CountDownLatch latch;

        public BlockingOneUpdateListener(Ahoy ahoy, int countDowns) {
            this.ahoy = ahoy;
            ahoy.addVisitListener(this);
            latch = new CountDownLatch(countDowns);
        }

        @Override public void onVisitUpdated(Visit visit) {
            if (latch.getCount() - 1 == 0) {
                ahoy.removeVisitListener(this);
            }
            latch.countDown();
        }

        public boolean await(long timeOutMillis) throws InterruptedException {
            return latch.await(timeOutMillis, TimeUnit.MILLISECONDS);
        }
    }

    @Before public void setupAhoy() {
        ahoy = new Ahoy();
        delegate = new PassThroughDelegate();
        storage = mock(Storage.class);
        wrapper = new LifecycleCallbackWrapper();
        visitorToken = UUID.randomUUID().toString();
        when(storage.readVisitorToken(nullable(String.class))).thenReturn(visitorToken);

        Timber.uprootAll();
    }

    @After public void clearSubscription() {
        compositeSubscription.clear();
    }

    private Map<String, Object> createUtmParams(int salt) {
        Map<String, Object> utmParams = new ArrayMap<>();
        utmParams.put(Visit.UTM_CAMPAIGN, "campaign " + salt);
        utmParams.put(Visit.UTM_CONTENT, "content " + salt);
        utmParams.put(Visit.UTM_MEDIUM, "medium " + salt);
        utmParams.put(Visit.UTM_SOURCE, "source " + salt);
        utmParams.put(Visit.UTM_TERM, "term " + salt);
        return utmParams;
    }

    @Test public void testAutoStartDisabled() throws InterruptedException {
        when(storage.readVisit(nullable(Visit.class))).thenReturn(Visit.empty());

        VisitListener failingListener = new FailingListener();
        ahoy.addVisitListener(failingListener);
        ahoy.init(storage, wrapper, delegate, false);
        wrapper.onActivityCreated(null, null);

        // because we passed autoStart == false to ahoy.init, ahoy didn't renew the token
        assertEquals(ahoy.visit(), Visit.empty());
        ahoy.removeVisitListener(failingListener);
    }

    @Test public void renewingToken() throws InterruptedException {
        VisitParams initialParams = VisitParams.create(visitorToken, null, null);
        when(storage.readVisit(nullable(Visit.class))).thenReturn(delegate.generateVisit(initialParams));

        ahoy.init(storage, wrapper, delegate, false);
        wrapper.onActivityCreated(null, null);

        BlockingOneUpdateListener blockingListener = new BlockingOneUpdateListener(ahoy, 2);
        ahoy.newVisit(null);
        ahoy.saveExtras(createUtmParams(0));
        assertTrue(blockingListener.await(20000));
        assertEquals("1", ahoy.visit().visitToken());

        blockingListener = new BlockingOneUpdateListener(ahoy, 4);
        ahoy.saveExtras(createUtmParams(1));
        ahoy.saveExtras(createUtmParams(2));
        ahoy.newVisit(null);
        ahoy.saveExtras(createUtmParams(3));
        assertTrue(blockingListener.await(20000));
        assertEquals("2", ahoy.visit().visitToken());
    }
}
