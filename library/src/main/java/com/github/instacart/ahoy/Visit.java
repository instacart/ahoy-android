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

import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.util.ArrayMap;

import com.github.instacart.ahoy.utils.MapTypeAdapter;
import com.github.instacart.ahoy.utils.TypeUtil;
import com.google.auto.value.AutoValue;
import com.ryanharter.auto.value.parcel.ParcelAdapter;

import java.util.Collections;
import java.util.Map;

@AutoValue
public abstract class Visit implements Parcelable {

    public static final String LANDING_PAGE = "landing_page";
    public static final String OS = "os";
    public static final String OS_ANDROID = "Android";
    public static final String REFERRER = "referrer";
    public static final String SCREEN_WIDTH = "screen_width";
    public static final String SCREEN_HEIGHT = "screen_height";
    public static final String VISIT_TOKEN = "visit_token";
    public static final String VISITOR_TOKEN = "visitor_token";
    public static final String USER_AGENT = "user_agent";
    public static final String UTM_CAMPAIGN = "utm_campaign";
    public static final String UTM_CONTENT = "utm_content";
    public static final String UTM_MEDIUM = "utm_medium";
    public static final String UTM_SOURCE = "utm_source";
    public static final String UTM_TERM = "utm_term";

    public static Visit create(String visitToken, @NonNull Map<String, Object> extraParams, long expiresAt) {
        extraParams = TypeUtil.ifNull(extraParams, Collections.emptyMap());
        return new AutoValue_Visit(visitToken, Collections.unmodifiableMap(extraParams), expiresAt);
    }

    public abstract String visitToken();
    @ParcelAdapter(MapTypeAdapter.class) public abstract Map<String, Object> extraParams();
    public abstract long expiresAt();

    public static Visit empty() {
        return create("", Collections.emptyMap(), 0);
    }

    public Visit expire() {
        return Visit.create(visitToken(), extraParams(), System.currentTimeMillis());
    }

    public <T> T extra(String key) {
        return (T) extraParams().get(key);
    }

    public boolean isValid() {
        return System.currentTimeMillis() < expiresAt();
    }

    public Visit withUpdatedExtraParams(Map<String, Object> extraParams) {
        Map<String, Object> map = new ArrayMap<>();
        map.putAll(extraParams());
        map.putAll(extraParams);
        return Visit.create(visitToken(), map, expiresAt());
    }
}
