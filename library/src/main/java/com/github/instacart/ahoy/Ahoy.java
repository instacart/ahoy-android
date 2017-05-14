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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.github.instacart.ahoy.LifecycleCallbackWrapper.Listener;
import com.github.instacart.ahoy.delegate.AhoyDelegate;
import com.github.instacart.ahoy.delegate.VisitParams;
import com.github.instacart.ahoy.utils.TypeUtil;
import com.google.auto.value.AutoValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Scheduler;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

public class Ahoy {

    private static final String TAG = "ahoy";

    /**
     * Delay applied if a AhoyDelegate newVisit or saveUtms request fails.
     */
    private static final long DEFAULT_REQUEST_RETRY_DELAY = 1000;

    private CompositeSubscription scheduledSubscriptions = new CompositeSubscription();
    private CompositeSubscription updatesSubscription = new CompositeSubscription();

    private AhoyDelegate delegate;
    private final Scheduler singleThreadScheduler = Schedulers.from(Executors.newSingleThreadExecutor());
    private boolean shutdown;
    private Storage storage;
    private Visit visit;
    private List<VisitListener> visitListeners = new ArrayList<>();
    private String visitorToken;

    private ArrayList<Request> updateQueue = new ArrayList<>();
    private volatile boolean updateLock = false;

    public interface VisitListener {
        void onVisitUpdated(Visit visit);
    }

    private interface Request { }

    @AutoValue
    public static abstract class NewVisitRequest implements Request {

        public static Request create(VisitParams visitParams) {
            return new AutoValue_Ahoy_NewVisitRequest(visitParams);
        }

        public abstract VisitParams getVisitParams();
    }

    @AutoValue
    public static abstract class SaveExtrasRequest implements Request {

        public static Request create(Map<String, Object> extras) {
            return new AutoValue_Ahoy_SaveExtrasRequest(extras);
        }

        public abstract Map<String, Object> getExtras();
    }

    public Ahoy() {
    }

    public void init(Storage storage, LifecycleCallbackWrapper wrapper, AhoyDelegate ahoyDelegate, final boolean
            autoStart) {
        this.delegate = ahoyDelegate;
        this.storage = storage;

        visit = storage.readVisit(Visit.empty());
        visitorToken = storage.readVisitorToken(null);

        if (TypeUtil.isEmpty(visitorToken)) {
            visitorToken = delegate.newVisitorToken();
            storage.saveVisitorToken(visitorToken);
        }

        wrapper.setListener(new Listener() {
            @Override public void onActivityCreated() {
                if (!autoStart || shutdown) {
                    return;
                }
                scheduleUpdate(System.currentTimeMillis());
            }

            @Override public void onActivityStarted() {
                if (!autoStart || shutdown) {
                    return;
                }
                scheduleUpdate(System.currentTimeMillis());
            }

            @Override public void onLastOnStop() {
                updatesSubscription.clear();
                scheduledSubscriptions.clear();
                updateLock = false;
            }
        });

    }

    private void scheduleUpdate(long timestamp) {
        scheduledSubscriptions.clear();
        final long delay = Math.max(timestamp - System.currentTimeMillis(), 0);
        Timber.tag(TAG).d(String.format("schedule update with delay %d at %d", delay, System.currentTimeMillis()));
        scheduledSubscriptions.add(
                Observable.timer(delay, TimeUnit.MILLISECONDS)
                        .observeOn(singleThreadScheduler)
                        .subscribe(new Action1<Long>() {
                            @Override public void call(Long aLong) {
                                Timber.tag(TAG).d(String.format("update at %d", System.currentTimeMillis()));
                                if (!visit.isValid()) {
                                    enqueueExpiredVisitUpdate();
                                }
                                processQueue();
                            }
                        }));
    }

    private void enqueueExpiredVisitUpdate() {
        synchronized (updateQueue) {
            for (Request request : updateQueue) {
                if (request instanceof NewVisitRequest) {
                    return;
                }
            }
            updateQueue.add(0, NewVisitRequest.create(VisitParams.create(visitorToken, null,
                    null)));
        }
    }

    private void processQueue() {
        if (updateLock) {
            return;
        }
        updateLock = true;

        synchronized (updateQueue) {
            if (updateQueue.size() == 0) {
                updateLock = false;
                scheduleUpdate(visit.expiresAt());
                return;
            }

            final Request request = updateQueue.get(0);

            Observable<Visit> visitObservable;
            if (request instanceof NewVisitRequest) {
                NewVisitRequest newVisitRequest = (NewVisitRequest) request;
                visitObservable = RxAhoyDelegate.createNewVisitStream(delegate, newVisitRequest.getVisitParams());
            } else {
                SaveExtrasRequest saveExtrasRequest = (SaveExtrasRequest) request;
                VisitParams params = VisitParams.create(visitorToken, visit, saveExtrasRequest.getExtras());
                visitObservable = RxAhoyDelegate.createSaveExtrasStream(delegate, params);
            }

            updatesSubscription.add(
                    visitObservable
                            .subscribe(new Action1<Visit>() {
                                @Override public void call(Visit visit) {
                                    saveVisit(visit);
                                    synchronized (updateQueue) {
                                        updateQueue.remove(0);
                                    }
                                    updateLock = false;
                                    scheduleUpdate(0);
                                }
                            }, new Action1<Throwable>() {
                                @Override public void call(Throwable throwable) {
                                    throwable.printStackTrace();
                                    updateLock = false;
                                    Timber.tag(TAG).d("failed " + request);
                                    scheduleUpdate(System.currentTimeMillis() + DEFAULT_REQUEST_RETRY_DELAY);
                                }
                            }));
        }
    }


    private void saveVisit(Visit visit) {
        if (visit == null) {
            throw new IllegalArgumentException("visit can't be null");
        }

        Visit oldVisit = this.visit;
        this.visit = visit;
        Timber.tag(TAG).d("saving updated visit " + visit.toString());
        storage.saveVisit(visit);
        if (!oldVisit.equals(visit)) {
            fireVisitUpdatedEvent();
        }
    }

    private void fireVisitUpdatedEvent() {
        List<VisitListener> copy = new ArrayList<>(visitListeners);
        for (VisitListener listener : copy) {
            try {
                listener.onVisitUpdated(visit);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @NonNull public Visit visit() {
        return visit;
    }

    public String visitorToken() {
        return visitorToken;
    }

    public void addVisitListener(VisitListener listener) {
        visitListeners.add(listener);
    }

    public void removeVisitListener(VisitListener listener) {
        visitListeners.remove(listener);
    }

    /**
     * Save visit with the provided extra parameters. If a visit was already saved, new visit is started
     * <p>
     * New values for the same keys, override stored parameters.
     * <p>
     * the {@link AhoyDelegate}.
     *
     * @param extraParams Extra parameters passed to {@link AhoyDelegate}. Null will saved parameters.
     */
    public void newVisit(@Nullable Map<String, Object> extraParams) {
        if (shutdown) {
            throw new IllegalArgumentException("Ahoy has been shutdownAndClear");
        }
        visit = visit.expire();
        synchronized (updateQueue) {
            VisitParams visitParams = VisitParams.create(visitorToken, null, extraParams);
            updateQueue.add(NewVisitRequest.create(visitParams));
        }
        scheduleUpdate(System.currentTimeMillis());
    }

    /**
     * Ensure that current visit is not expired. Renew visit if necessary.
     * <p>
     * New values for the same keys, override stored parameters.
     * <p>
     * the {@link AhoyDelegate}.
     *
     */
    public void ensureFreshVisit() {
        if (shutdown) {
            throw new IllegalArgumentException("Ahoy has been shutdownAndClear");
        }
        scheduleUpdate(System.currentTimeMillis());
    }

    /**
     * Save extra visit parameters as part of save visit or save extras delegate call.
     * <p>
     * New values for the same keys, override stored parameters.
     * <p>
     * the {@link AhoyDelegate}.
     *
     * @param extraParams Extra parameters passed to {@link AhoyDelegate}. Null will saved parameters.
     */
    public void saveExtras(@Nullable Map<String, Object> extraParams) {
        if (shutdown) {
            throw new IllegalArgumentException("Ahoy has been shutdownAndClear");
        }
        synchronized (updateQueue) {
            updateQueue.add(SaveExtrasRequest.create(extraParams));
        }
        scheduleUpdate(System.currentTimeMillis());
    }

    /**
     * Stops periodic visit updates and removes saved visit id. Visitor token is preserved.
     */
    public void shutdownAndClear() {
        storage.clear();
        storage.saveVisitorToken(visitorToken);
        updatesSubscription.clear();
        scheduledSubscriptions.clear();
        shutdown = true;
    }
}