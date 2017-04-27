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

import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

final class Server {
    final AtomicBoolean running = new AtomicBoolean(false);
    final List<Connection> connections = new LinkedList<>();
    final Map<String, Object> shellGlobalValues = new HashMap<>();
    final Lock lock = new ReentrantLock();

    ServerSocket serverSocket;
    ServerThread serverThread;

    String address;

    boolean isStarted() {
        return running.get();
    }

    void start() throws IOException {
        if (running.compareAndSet(false, true)) {
            address = getDeviceAddress();

            serverSocket = new ServerSocket(9998);
            serverThread = new ServerThread();
            serverThread.start();
        }
    }

    void stop() throws IOException {
        if (running.compareAndSet(true, false)) {
            try {
                serverSocket.close();
            } catch (IOException ignored) {}

            serverSocket = null;
            serverThread = null;

            lock.lock();
            try {
                for (Connection connection : connections) {
                    try {
                        connection.close();
                    } catch (IOException ignored) {}
                }
                connections.clear();
            } finally {
                lock.unlock();
            }
        }
    }

    String getAddress() {
        return address;
    }

    void registerGlobal(String name, Object value) {
        lock.lock();
        try {
            if (value != null) {
                shellGlobalValues.put(name, value);
            } else {
                shellGlobalValues.remove(name);
            }
        } finally {
            lock.unlock();
        }
    }

    void handleConnection(Socket client) {
        Map<String, Object> globals;
        lock.lock();
        try {
            globals = new HashMap<>(shellGlobalValues);
        } finally {
            lock.unlock();
        }

        Connection connection;
        try {
            connection = new Connection(client, globals);
        } catch (IOException e) {
            try {
                client.close();
            } catch (IOException ignored) {
            }
            return;
        }

        lock.lock();
        try {
            connections.add(connection);
        } finally {
            lock.unlock();
        }
    }

    private String getDeviceAddress() throws SocketException {
        String v4 = null;
        String v6 = null;

        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface iface = interfaces.nextElement();
            Enumeration<InetAddress> addresses = iface.getInetAddresses();
            while (addresses.hasMoreElements()) {
                InetAddress address = addresses.nextElement();
                if (address.isLoopbackAddress()
                        || address.isLinkLocalAddress()) {
                    continue;
                }

                if (address instanceof Inet4Address) {
                    v4 = address.getHostAddress();
                } else if (address instanceof Inet6Address) {
                    v6 = address.getHostAddress();
                }
            }
        }

        if (v6 != null) {
            return v6;
        }

        if (v4 != null) {
            return v4;
        }

        return null;
    }

    final class ServerThread extends Thread {
        @Override
        public void run() {
            while (running.get()) {
                try {
                    serveNextClient();
                } catch (IOException e) {
                    // fail
                }
            }
        }

        void serveNextClient() throws IOException {
            Socket client;
            try {
                client = serverSocket.accept();
            } catch (IOException e) {
                // We've been closed
                return;
            }

            if (!running.get()) {
                client.close();
                return;
            }

            handleConnection(client);
        }
    }
}
