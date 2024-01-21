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
package org.apache.rat.inspector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.rat.license.ILicense;

public abstract class AbstractInspector implements Inspector {
    private final Type type;
    private final String name;

    public AbstractInspector(Type type, String name) {
        this.type = type;
        this.name = name;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public String getCommonName() {
        return name;
    }

    public static Inspector license(ILicense license, Inspector matcher) {
        return new AbstractInspector(Inspector.Type.License, "license") {

            @Override
            public Collection<Inspector> getChildren() {
                List<Inspector> result = new ArrayList<>();
                if (!license.getName().equals(license.getLicenseFamily().getFamilyName())) {
                    result.add(parameter("name", license.getName()));
                }
                if (!license.getId().equals(license.getLicenseFamily().getFamilyCategory().trim())) {
                    result.add(parameter("id", license.getId()));
                }
                result.add(parameter("family", license.getLicenseFamily().getFamilyCategory().trim()));
                result.add(parameter("notes", license.getNotes()));
                if (matcher != null) {
                    result.add(matcher);
                }
                return result;
            }
        };
    }

    public static Inspector parameter(String name, String value) {
        return new Inspector() {
            @Override
            public Inspector.Type getType() {
                return Inspector.Type.Parameter;
            }

            @Override
            public String getCommonName() {
                return name;
            }

            @Override
            public String getParamValue() {
                return value == null ? "" : value;
            }
        };
    }

    public static Inspector text(String value) {
        return new Inspector() {
            @Override
            public Inspector.Type getType() {
                return Inspector.Type.Text;
            }

            @Override
            public String getCommonName() {
                return "";
            }

            @Override
            public String getParamValue() {
                return value == null ? "" : value;
            }
        };
    }

    public static Inspector matcher(String name, String id, Collection<Inspector> children) {
        return new AbstractInspector(Type.Matcher, name) {
            @Override
            public Collection<Inspector> getChildren() {
                List<Inspector> result = new ArrayList<>();
                result.add(parameter("id", id));
                if (children != null) {
                    result.addAll(children);
                }
                return result;
            }
        };
    }

}
