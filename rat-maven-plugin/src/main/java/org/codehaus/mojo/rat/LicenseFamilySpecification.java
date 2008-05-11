package org.codehaus.mojo.rat;

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

/**
 * Used to specify additional license familys. A license family is basically a class, which implements
 * {@link rat.license.ILicenseFamily}.
 */
public class LicenseFamilySpecification
{
    /**
     * The license familys class name.
     */
    private String className;

    /**
     * Returns the license familys class name.
     * 
     * @return Class name of the license family.
     */
    public String getClassName()
    {
        return className;
    }

    /**
     * Sets the license familys class name. Required.
     * 
     * @param pClassName
     *            Class name of the license family.
     */
    public void setClassName( String pClassName )
    {
        className = pClassName;
    }
}
