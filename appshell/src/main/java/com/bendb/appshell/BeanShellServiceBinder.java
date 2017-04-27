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

import java.util.Map;

public interface BeanShellServiceBinder {
    boolean isStarted();

    /**
     * Gets the external IP address of this device, if any.
     */

    String address();

    /**
     * Makes the given {@code value} available in shells, in the global namespace,
     * under the given {@code name}.
     *
     * Note that this does not affect shells that have already been initialized -
     * only new connections will see this value.
     *
     * @param name the name of the value to register
     * @param value the value to make available to shells.
     */
    void registerGlobal(String name, Object value);
}
