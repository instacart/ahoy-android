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
package com.github.instacart.ahoy.delegate.retrofit2;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableTransformer;
import io.reactivex.functions.Function;
import retrofit2.HttpException;

class RxBackoff {

    private static final int DEFAULT_RECONNECT_ATTEMPTS = 3;

    private static class RetryFunction implements Function<Throwable, Observable<Long>> {

        private final int mAttempts;
        private int mRetryCounter = 0;

        public RetryFunction(int attempts) {
            mAttempts = attempts;
        }

        @Override public Observable<Long> apply(final Throwable throwable) {

            if (!isRetryNeeded(throwable)) {
                return Observable.error(throwable);
            }

            if (mRetryCounter++ >= mAttempts) {
                return Observable.error(throwable);
            }

            return Observable.timer(getDelayTime(mRetryCounter - 1), TimeUnit.MILLISECONDS);
        }

        private int getDelayTime(int currentRetry) {
            // exponential back-off
            return (int) (Math.pow(2, currentRetry) * 100);
        }

        private boolean isRetryNeeded(Throwable throwable) {

            if (!(throwable instanceof HttpException)) {
                return false;
            }

            int status = ((HttpException) throwable).code();
            return isStatusWorthRetry(status);
        }

        private boolean isStatusWorthRetry(int status) {
            return status == 0 || status == 500 || status == 501 || status == 503 || status == 504;
        }
    }

    private RxBackoff() {
    }

    public static <T> ObservableTransformer<T, T> backoff() {
        return backoff(DEFAULT_RECONNECT_ATTEMPTS - 1);
    }

    /**
     * Source: https://gist.github.com/sddamico/c45d7cdabc41e663bea1
     *
     * @param retryAttempts The max number of attempts to retry this task or -1 to try MAX_INT times,
     */
    public static <T> ObservableTransformer<T, T> backoff(final int retryAttempts) {
        return observable -> observable.retryWhen(retryFunc(retryAttempts));
    }

    /**
     * Source: https://gist.github.com/sddamico/c45d7cdabc41e663bea1
     */
    private static Function<? super Observable<? extends Throwable>, ? extends Observable<?>> retryFunc(
            final int attempts) {

        // zip our number of retries to the incoming errors so that we only produce retries
        // when there's been an error
        return observable -> observable.switchMap(new RetryFunction(attempts));
    }
}
