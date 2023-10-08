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
package org.apache.rat.document.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.IOUtils;

public class DocumentImplUtils {

    public final static String toName(File file) {
        String path = file.getPath();
        return path.replace('\\', '/');
    }
    
    public static final boolean isZipStream(InputStream stream) {
        ZipInputStream zip = new ZipInputStream(stream);
        try {
            zip.getNextEntry();
            return true;
        } catch (IOException e) {
            return false;
        } finally {
            IOUtils.closeQuietly(zip);
        }
    }

    public static final boolean isZip(File file) {
        try {
            return isZipStream(new FileInputStream(file));
        } catch (IOException e) {
            return false;
        }
    }

}
