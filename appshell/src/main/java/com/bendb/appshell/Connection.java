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

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Map;

import bsh.EvalError;
import bsh.Interpreter;
import bsh.Primitive;

final class Connection implements Closeable, Runnable {
    private final Socket client;
    private final Interpreter interpreter;
    private final Thread connectionThread;

    Connection(Socket client, Map<String, Object> globals) throws IOException {
        this.client = client;
        InputStream input = client.getInputStream();
        OutputStream output = client.getOutputStream();
        StreamConsoleInterface consoleInterface = new StreamConsoleInterface(input, output);

        interpreter = new Interpreter(consoleInterface);
        interpreter.setExitOnEOF(false);
        for (Map.Entry<String, Object> entry : globals.entrySet()) {
            try {
                registerGlobalValue(entry.getKey(), entry.getValue());
            } catch (EvalError evalError) {
                // hmm... what to do?
            }
        }

        connectionThread = new Thread(this);
        connectionThread.start();
    }

    private void registerGlobalValue(String name, Object value) throws EvalError {
        // If someone has given us boxed primitives, we need to unbox them
        // for bsh because it has its own special kind of box.
        if (value == null) {
            value = Primitive.NULL;
        } else if (Primitive.isWrapperType(value.getClass())) {
            value = new Primitive(value);
        }

        interpreter.set(name, value);
    }

    @Override
    public void run() {
        try {
            interpreter.run();
        } catch (Exception e) {
            try {
                close();
            } catch (IOException ignored) {
            }
        }
    }

    @Override
    public void close() throws IOException {
        try {
            client.close();
        } catch (IOException e) {
            // oh well
        }
    }
}
