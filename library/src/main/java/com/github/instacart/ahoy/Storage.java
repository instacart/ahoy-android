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

import android.app.Application;

import com.github.instacart.ahoy.utils.SharedPreferencesWrapper;

import java.util.Collections;
import java.util.Map;

public class Storage {

    private static final String AHOY_PREFERENCES_FILE = "ahoy.prefs";

    private static final String VISIT_TOKEN = "visit token";
    private static final String VISIT_EXTRA_PARAMS = "visit extra params";
    private static final String VISITOR_TOKEN = "visitor token";
    private static final String VISIT_EXPIRATION = "visit expiration";

    private final SharedPreferencesWrapper sharedPreferences;

    public Storage(Application application) {
        sharedPreferences = new SharedPreferencesWrapper(application, AHOY_PREFERENCES_FILE);
    }

    public Visit readVisit(Visit defaultValue) {
        String visitToken = sharedPreferences.getString(VISIT_TOKEN, null);
        if (visitToken == null) {
            return defaultValue;
        }
        Map<String, Object> emptyMap = Collections.emptyMap();
        Map<String, Object> extraParams = sharedPreferences.getStringMap(VISIT_EXTRA_PARAMS, emptyMap);
        long visitTokenExpiration = sharedPreferences.getLong(VISIT_EXPIRATION, 0);
        return Visit.create(visitToken, extraParams, visitTokenExpiration);
    }

    public void saveVisit(Visit visit) {
        if (visit == null) {
            sharedPreferences.delete(VISIT_EXPIRATION);
            sharedPreferences.delete(VISIT_EXTRA_PARAMS);
            sharedPreferences.delete(VISIT_TOKEN);
            return;
        }

        sharedPreferences.putStringMap(VISIT_EXTRA_PARAMS, visit.extraParams());
        sharedPreferences.putLong(VISIT_EXPIRATION, visit.expiresAt());
        sharedPreferences.putString(VISIT_TOKEN, visit.visitToken());
    }

    public void saveVisitorToken(String visitorToken) {
        sharedPreferences.putString(VISITOR_TOKEN, visitorToken);
    }

    public String readVisitorToken(String defaultValue) {
        return sharedPreferences.getString(VISITOR_TOKEN, defaultValue);
    }

    public void clear() {
        sharedPreferences.clear();
    }
}
