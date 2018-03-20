package com.github.instacart.ahoy.tests.util;

import android.os.Parcel;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MockParcel {

    public static Parcel obtain() {
        return new MockParcel().getMockedParcel();
    }

    private final Parcel mockedParcel = mock(Parcel.class);
    private final List<Object> objects = new ArrayList<>();
    private int position = 0;

    public Parcel getMockedParcel() {
        return mockedParcel;
    }

    public MockParcel() {
        setupMock();
    }

    private void setupMock() {
        setupWrites();
        setupReads();
        setupOthers();
    }

    private void setupWrites() {
        Answer<Void> writeAnswer = new WriteAnswer();
        doAnswer(writeAnswer).when(mockedParcel).writeInt(anyInt());
        doAnswer(writeAnswer).when(mockedParcel).writeString(anyString());
        doAnswer(writeAnswer).when(mockedParcel).writeValue(any());
    }

    private void setupReads() {
        when(mockedParcel.readInt()).thenAnswer(new ReadAnswer<Integer>());
        when(mockedParcel.readString()).thenAnswer(new ReadAnswer<String>());
        when(mockedParcel.readValue(Object.class.getClassLoader())).thenAnswer(new ReadAnswer<>());
    }

    private void setupOthers() {
        doAnswer(invocation -> objects.size()).when(mockedParcel).dataSize();
    }

    private class WriteAnswer implements Answer<Void> {
        @Override
        public Void answer(InvocationOnMock invocation) throws Throwable {
            objects.add(invocation.getArgument(0));
            return null;
        }
    }

    private class ReadAnswer<T> implements Answer<T> {
        @Override
        public T answer(InvocationOnMock invocation) throws Throwable {
            return (T) objects.get(position++);
        }
    }
}
