package com.github.instacart.ahoy.tests;

import android.os.Parcel;
import androidx.collection.ArrayMap;

import com.github.instacart.ahoy.tests.util.MockParcel;
import com.github.instacart.ahoy.utils.MapTypeAdapter;

import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;

public class MapTypeAdapterTest {

    @Test public void testConversion() {
        MapTypeAdapter adapter = new MapTypeAdapter();

        Map<String, Object> before = new ArrayMap<>();
        before.put("test1", 0);
        before.put("test2", "");
        before.put("test3", 25L);
        before.put("test4", new Object());

        Parcel parcel = MockParcel.obtain();
        adapter.toParcel(before, parcel);
        assertEquals(before, adapter.fromParcel(parcel));
    }
}
