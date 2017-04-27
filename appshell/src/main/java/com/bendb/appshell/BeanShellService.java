/*
 * App Shell
 *
 * Copyright (c) Benjamin Bader
 *
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * THIS CODE IS PROVIDED ON AN  *AS IS* BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING
 * WITHOUT LIMITATION ANY IMPLIED WARRANTIES OR CONDITIONS OF TITLE,
 * FITNESS FOR A PARTICULAR PURPOSE, MERCHANTABLITY OR NON-INFRINGEMENT.
 *
 * See the Apache Version 2.0 License for specific language governing permissions and limitations under the License.
 */

package com.bendb.appshell;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import java.io.IOException;

public final class BeanShellService extends Service {
    Server server = new Server();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            server.start();
        } catch (IOException e) {
            // nope
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new Binder();
    }

    final class Binder extends android.os.Binder implements BeanShellServiceBinder {
        @Override
        public boolean isStarted() {
            return server.isStarted();
        }

        @Override
        public String address() {
            return server.getAddress();
        }

        @Override
        public void registerGlobal(String name, Object value) {
            server.registerGlobal(name, value);
        }
    }
}
