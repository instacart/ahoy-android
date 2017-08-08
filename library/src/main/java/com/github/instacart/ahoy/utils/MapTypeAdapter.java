package com.github.instacart.ahoy.utils;

import android.os.Parcel;
import android.support.v4.util.ArrayMap;

import com.ryanharter.auto.value.parcel.TypeAdapter;

import java.util.Map;

public class MapTypeAdapter implements TypeAdapter<Map<String, Object>> {
    private static final ClassLoader CLASS_LOADER = Object.class.getClassLoader();

    @Override
    public Map<String, Object> fromParcel(Parcel parcel) {
        int size = parcel.readInt();
        Map<String, Object> map = new ArrayMap<>(size);

        for (int i = 0; i < size; i++) {
            map.put(parcel.readString(), parcel.readValue(CLASS_LOADER));
        }
        return map;
    }

    @Override
    public void toParcel(Map<String, Object> map, Parcel parcel) {
        parcel.writeInt(map.size());

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            parcel.writeString(entry.getKey());
            parcel.writeValue(entry.getValue());
        }
    }
}
