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
package com.github.instacart.ahoy.tests;

import com.github.instacart.ahoy.Ahoy;
import com.github.instacart.ahoy.LifecycleCallbackWrapper;
import com.github.instacart.ahoy.Storage;
import com.github.instacart.ahoy.delegate.AhoyDelegate;

import org.junit.Before;
import org.junit.Test;

import timber.log.Timber;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class VisitorTokenTest {

    private Ahoy ahoy;
    private AhoyDelegate delegate;
    private Storage storage;
    private LifecycleCallbackWrapper wrapper    ;

    @Before public void setupAhoy() {
        ahoy = new Ahoy();
        delegate = mock(AhoyDelegate.class);
        storage = mock(Storage.class);
        wrapper = mock(LifecycleCallbackWrapper.class);

        Timber.uprootAll();
    }

    @Test public void testStoredToken() throws Exception {
        when(storage.readVisitorToken(nullable(String.class))).thenReturn("123");
        ahoy.init(storage, wrapper, delegate, true);
        assertEquals("123", ahoy.visitorToken());
    }

    @Test public void testNewToken() throws Exception {
        when(storage.readVisitorToken(nullable(String.class))).thenReturn(null);
        when(delegate.newVisitorToken()).thenReturn("1234");
        ahoy.init(storage, wrapper, delegate, true);
        assertEquals("1234", ahoy.visitorToken());
    }
}