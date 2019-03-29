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

import android.net.Uri;
import androidx.collection.ArrayMap;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.instacart.ahoy.Visit;
import com.github.instacart.ahoy.delegate.AhoyDelegate;
import com.github.instacart.ahoy.delegate.DeviceInfo;
import com.github.instacart.ahoy.delegate.VisitParams;
import com.github.instacart.ahoy.utils.TypeUtil;
import com.github.instacart.ahoy.utils.UtmUtil;
import com.google.auto.value.AutoValue;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request.Builder;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.logging.HttpLoggingInterceptor.Level;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;

public class Retrofit2Delegate implements AhoyDelegate {

    private static final long DEFAULT_VISIT_DURATION = 30 * 60 * 1000;

    private final ApiRetrofit2 api;
    private final DeviceInfo deviceInfo;
    private long visitDuration = DEFAULT_VISIT_DURATION;

    public interface ApiRetrofit2 {

        @POST("/ahoy/visits")
        Observable<VisitResponse> registerVisit(@Body Map<String, Object> body);
    }

    @AutoValue
    public static abstract class VisitResponse {

        @JsonCreator
        public static VisitResponse create(@JsonProperty("visit_id") String visitId) {
            return new AutoValue_Retrofit2Delegate_VisitResponse(visitId);
        }

        public abstract String visitId();
    }

    public static Retrofit2Delegate factory(String baseUrl, long visitDuration, final DeviceInfo deviceInfo,
            boolean loggingEnabled) {

        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(loggingEnabled ? Level.BODY : Level.NONE);

        Interceptor userAgentInterceptor = chain -> {
            Builder builder = chain.request().newBuilder();
            builder.header("User-Agent", deviceInfo.getUserAgent());
            return chain.proceed(builder.build());
        };

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .addInterceptor(userAgentInterceptor)
                .connectTimeout(100, TimeUnit.SECONDS)
                .readTimeout(150, TimeUnit.SECONDS)
                .writeTimeout(100, TimeUnit.SECONDS)
                .build();

        return new Retrofit2Delegate(baseUrl, okHttpClient, visitDuration, deviceInfo);
    }

    public Retrofit2Delegate(String baseUrl, OkHttpClient okHttpClient, long visitDuration,
            final DeviceInfo deviceInfo) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        Retrofit retrofit = new Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(JacksonConverterFactory.create(mapper))
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .build();

        this.api = retrofit.create(ApiRetrofit2.class);
        this.deviceInfo = deviceInfo;
        this.visitDuration = visitDuration;
    }

    private void makeRequest(String visitToken, final VisitParams visitParams, final AhoyCallback callback) {
        Map<String, Object> request = new ArrayMap<>();
        request.put(Visit.OS, deviceInfo.getOs());
        request.put(Visit.USER_AGENT, deviceInfo.getUserAgent());
        request.put(Visit.SCREEN_HEIGHT, deviceInfo.getScreenHeightDp());
        request.put(Visit.SCREEN_WIDTH, deviceInfo.getScreenWidthDp());
        request.put(Visit.VISITOR_TOKEN, visitParams.visitorToken());
        request.putAll(TypeUtil.ifNull(visitParams.extraParams(), Collections.emptyMap()));

        Uri landingParams = UtmUtil.utmUri(visitParams.extraParams());
        if (landingParams != null && !TypeUtil.isEmpty(landingParams.toString())) {
            request.put(Visit.LANDING_PAGE, landingParams.toString());
        }

        if (!TypeUtil.isEmpty(visitToken)) {
            request.put(Visit.VISIT_TOKEN, visitToken);
        }

        api.registerVisit(request)
                .compose(RxBackoff.backoff())
                .subscribeOn(Schedulers.io())
                .subscribe(visitResponse -> {
                    long expiresAt = System.currentTimeMillis() + visitDuration;
                    Map<String, Object> extraParams = TypeUtil.ifNull(visitParams.extraParams(), Collections.<String, Object>emptyMap());
                    callback.onSuccess(Visit.create(visitResponse.visitId(), extraParams, expiresAt));
                }, callback::onFailure);
    }

    @Override public String newVisitorToken() {
        return UUID.randomUUID().toString();
    }

    @Override public void saveVisit(VisitParams visitParams, AhoyCallback callback) {
        final String visitorToken = visitParams.visitorToken();
        if (TypeUtil.isEmpty(visitorToken)) {
            throw new IllegalArgumentException("Please provide visitor token");
        }

        String visitToken = UUID.randomUUID().toString();
        makeRequest(visitToken, visitParams, callback);
    }

    @Override public void saveExtras(VisitParams visitParams, AhoyCallback callback) {
        if (TypeUtil.isEmpty(visitParams.visitorToken())) {
            throw new IllegalArgumentException("Please provide visit & visitor token");
        }
        makeRequest(null, visitParams, callback);
    }
}
