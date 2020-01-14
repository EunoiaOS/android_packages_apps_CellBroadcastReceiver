/**
 * Copyright (C) 2016 The Android Open Source Project
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.cellbroadcastreceiver.unit;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.res.Resources;
import android.media.AudioManager;
import android.telephony.CarrierConfigManager;
import android.telephony.SubscriptionManager;
import android.test.ServiceTestCase;

import com.android.internal.telephony.ISub;

import org.junit.After;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public abstract class CellBroadcastServiceTestCase<T extends Service> extends ServiceTestCase<T> {

    @Mock
    protected CarrierConfigManager mMockedCarrierConfigManager;
    @Mock
    Resources mResources;
    @Mock
    protected ISub.Stub mSubService;
    @Mock
    protected AudioManager mMockedAudioManager;

    MockedServiceManager mMockedServiceManager;

    Intent mServiceIntentToVerify;

    Intent mActivityIntentToVerify;

    CellBroadcastServiceTestCase(Class<T> serviceClass) {
        super(serviceClass);
    }

    protected static void waitForMs(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
        }
    }

    private class TestContextWrapper extends ContextWrapper {

        private final String TAG = TestContextWrapper.class.getSimpleName();

        public TestContextWrapper(Context base) {
            super(base);
        }

        @Override
        public ComponentName startService(Intent service) {
            mServiceIntentToVerify = service;
            return null;
        }

        @Override
        public Resources getResources() {
            return mResources;
        }

        @Override
        public void startActivity(Intent intent) {
            mActivityIntentToVerify = intent;
        }

        @Override
        public Context getApplicationContext() {
            return this;
        }

        @Override
        public Object getSystemService(String name) {
            switch (name) {
                case Context.CARRIER_CONFIG_SERVICE:
                    return mMockedCarrierConfigManager;
                case Context.AUDIO_SERVICE:
                    return mMockedAudioManager;
            }
            return super.getSystemService(name);
        }
    }

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        // A hack to return mResources from static method
        // CellBroadcastSettings.getResources(context).
        doReturn(mSubService).when(mSubService).queryLocalInterface(anyString());
        doReturn(SubscriptionManager.INVALID_SUBSCRIPTION_ID).when(mSubService).getDefaultSubId();
        doReturn(SubscriptionManager.INVALID_SUBSCRIPTION_ID).when(mSubService).getDefaultSmsSubId();

        doReturn(new String[]{""}).when(mResources).getStringArray(anyInt());

        mMockedServiceManager = new MockedServiceManager();
        mMockedServiceManager.replaceService("isub", mSubService);
        mContext = new TestContextWrapper(getContext());
        setContext(mContext);
    }

    @After
    public void tearDown() throws Exception {
        mMockedServiceManager.restoreAllServices();
    }

    void putResources(int id, String[] values) {
        doReturn(values).when(mResources).getStringArray(eq(id));
    }
}
