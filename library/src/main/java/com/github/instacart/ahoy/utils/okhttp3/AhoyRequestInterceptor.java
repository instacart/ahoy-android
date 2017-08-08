package com.github.instacart.ahoy.utils.okhttp3;

import android.support.annotation.NonNull;

import com.github.instacart.ahoy.Ahoy;
import com.github.instacart.ahoy.Visit;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Request.Builder;
import okhttp3.Response;

public class AhoyRequestInterceptor implements Interceptor {

    private static final String HEADER_VISIT = "Ahoy-Visit";
    private static final String HEADER_VISITOR = "Ahoy-Visitor";

    private Ahoy ahoy;

    @Override public Response intercept(@NonNull Chain chain) throws IOException {
        if (ahoy == null) {
            return chain.proceed(chain.request());
        }

        Request request = chain.request();
        Builder builder = request.newBuilder();
        if (!Visit.empty().equals(ahoy.visit())) {
            builder.header(HEADER_VISIT, ahoy.visit().visitToken());
        }
        builder.header(HEADER_VISITOR, ahoy.visitorToken());
        return chain.proceed(builder.build());
    }

    public void setAhoy(Ahoy ahoy) {
        this.ahoy = ahoy;
    }
}