/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

${package}

import java.io.File;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/* DO NOT EDIT - GENERATED FILE */

/**
 * Generated class to provide Ant support for standard Rat command line options
 */
${class}
    private final List<String> args = new ArrayList<>();

${constructor}

    protected List<String> args() { return this.args; }

    protected void setArg(String key, String value) {
        int idx = args.indexOf(key);
        if (idx == -1) {
            addArg(key, value);
        } else {
            args.set(idx + 1, value);
        }
    }

    protected void addArg(String key, String value) {
        args.add(key);
        args.add(value);
    }

${methods}
}
