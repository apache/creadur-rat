/*
 * Licensed to the Apache Software Foundation (ASF) under one   *
 * or more contributor license agreements.  See the NOTICE file *
 * distributed with this work for additional information        *
 * regarding copyright ownership.  The ASF licenses this file   *
 * to you under the Apache License, Version 2.0 (the            *
 * "License"); you may not use this file except in compliance   *
 * with the License.  You may obtain a copy of the License at   *
 *                                                              *
 *   http://www.apache.org/licenses/LICENSE-2.0                 *
 *                                                              *
 * Unless required by applicable law or agreed to in writing,   *
 * software distributed under the License is distributed on an  *
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY       *
 * KIND, either express or implied.  See the License for the    *
 * specific language governing permissions and limitations      *
 * under the License.                                           *
 */ 
package org.apache.rat.document.impl.zip;

import java.util.zip.ZipEntry;

final class ZipUtils {

    public static String getStem(final ZipEntry entry) {
        final String name = entry.getName();
        final int lastIndexOfForwardSlash = name.lastIndexOf('/');
        final int lastIndexOfBackSlash = name.lastIndexOf('\\');
        final int index = Math.max(lastIndexOfBackSlash, lastIndexOfForwardSlash);
        String result = "";
        if (index >= 0) {
            result = name.substring(0, index);
        }
        return result;
    }
    
    public static String getName(final ZipEntry entry) {
        String name = entry.getName();
        if (name.endsWith("/") || name.endsWith("\\")) {
            name = name.substring(0, name.length() - 1);
        }
        final int lastIndexOfForwardSlash = name.lastIndexOf('/');
        final int lastIndexOfBackSlash = name.lastIndexOf('\\');
        final int index = Math.max(lastIndexOfBackSlash, lastIndexOfForwardSlash);
        String result = name;
        if (index >= 0) {
            final int length = name.length();
            result = name.substring(index + 1, length);
        }
        return result;
    }
    
    public static String getUrl(final ZipEntry entry) {
        return "zip:" + entry.getName();
    }
    
    public static boolean isTopLevel(final ZipEntry entry) {
        final String name = entry.getName();
        final int lastPosition = name.length() - 1;
        final int indexOfForwardSlash = name.indexOf('/');
        final int indexOfBackSlash = name.indexOf('\\');
        final boolean result = (indexOfForwardSlash < 0 || indexOfForwardSlash == lastPosition) 
                        && (indexOfBackSlash < 0 || indexOfBackSlash == lastPosition);
        return result;
    }
}
