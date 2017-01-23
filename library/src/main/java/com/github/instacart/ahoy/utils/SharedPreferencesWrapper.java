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

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SharedPreferencesWrapper {

    private final Gson gson;
    private final SharedPreferences mSharedPreferences;

    public SharedPreferencesWrapper(Context context, String fileName) {
        mSharedPreferences = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        gson = new GsonBuilder().create();
    }

    public void delete(String key) {
        mSharedPreferences.edit().remove(key).apply();
    }

    public void putBoolean(String key, boolean value) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public void putString(String key, String value, boolean commit) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(key, value);
        if (commit) {
            editor.commit();
        } else {
            editor.apply();
        }
    }

    public void putString(String key, String value) {
        putString(key, value, false);
    }

    public void putStringSet(String key, Set<String> value) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putStringSet(key, value);
        editor.apply();
    }

    public Set<String> getStringSet(String key, Set<String> defaultValue) {
        return mSharedPreferences.getStringSet(key, defaultValue);
    }

    public void putInteger(String key, Integer value) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public Integer getInteger(String key, Integer defaultValue) {
        return mSharedPreferences.getInt(key, defaultValue);
    }

    public long getLong(String key, long defaultValue) {
        return mSharedPreferences.getLong(key, defaultValue);
    }

    public void putLong(String key, long timestamp) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putLong(key, timestamp);
        editor.apply();
    }

    public String getString(String key, String defaultValue) {
        return mSharedPreferences.getString(key, defaultValue);
    }

    public void putStringMap(String key, @Nullable Map<String, Object> map) {
        if (map == null) {
            Editor editor = mSharedPreferences.edit();
            editor.remove(key);
            editor.apply();
            return;
        }

        String json = gson.toJson(map);
        putString(key, json);
    }

    public Map<String, Object> getStringMap(String key, Map<String, Object> defaultValue) {
        String json = getString(key, null);
        if (TypeUtil.isEmpty(json)) {
            return defaultValue;
        }

        return TypeUtil.ifNull(gson.fromJson(json, HashMap.class), defaultValue);
    }

    public void clear() {
        SharedPreferences.Editor edit = mSharedPreferences.edit();
        edit.clear();
        edit.commit();
    }
}