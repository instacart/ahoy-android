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
package com.github.instacart.ahoy.utils;

import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.util.ArrayMap;

import com.github.instacart.ahoy.Visit;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class UtmUtil {

    public static final List<String> UTM_PARAMS = Arrays.asList(
            Visit.UTM_CAMPAIGN,
            Visit.UTM_CONTENT,
            Visit.UTM_MEDIUM,
            Visit.UTM_SOURCE,
            Visit.UTM_TERM
    );

    private UtmUtil() {
    }

    @Nullable public static Map<String, String> utmParams(@Nullable  Map<String, Object> params) {
        if (params == null) {
            return null;
        }
        Map<String, String> map = new ArrayMap<>();
        for (String key : UTM_PARAMS) {
            if (!params.containsKey(key)) {
                continue;
            }
            Object value = params.get(key);
            if (value != null && value instanceof CharSequence) {
                map.put(key, value.toString());
            }
        }
        return map;
    }

    public static Uri utmUri(Map<String, Object> params) {
        if (params == null) {
            return null;
        }
        Uri.Builder builder = new Uri.Builder();
        Set<Entry<String, String>> utmParams = UtmUtil.utmParams(params).entrySet();
        for (Map.Entry<String, String> entry : utmParams) {
            builder = builder.appendQueryParameter(entry.getKey(), Uri.encode(entry.getValue()));
        }
        return builder.build();
    }
}
