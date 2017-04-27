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

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import bsh.ConsoleInterface;

final class StreamConsoleInterface implements ConsoleInterface {
    private final Reader reader;
    private final PrintStream writer;

    StreamConsoleInterface(InputStream input, OutputStream output) {
        try {
            reader = new InputStreamReader(input, "UTF-8");
            writer = new PrintStream(output, true, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError(e);
        }
    }

    @Override
    public Reader getIn() {
        return reader;
    }

    @Override
    public PrintStream getOut() {
        return writer;
    }

    @Override
    public PrintStream getErr() {
        return writer;
    }

    @Override
    public void println(Object o) {
        print(o);
        writer.println();
    }

    @Override
    public void print(Object o) {
        writer.print(o);
    }

    @Override
    public void error(Object o) {
        print(o);
    }
}
