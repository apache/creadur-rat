package org.apache.rat.mp.util;

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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

public final class ConfigurationHelper {

    private ConfigurationHelper() {
        // prevent instantiation
    }

    public static <T> T newInstance(final Class<T> clazz, final String className)
            throws MojoExecutionException, MojoFailureException {
        try {
            final ClassLoader cl = Thread.currentThread()
                    .getContextClassLoader();
            @SuppressWarnings("unchecked") // incorrect cast will be caught below
            final T o = (T) cl.loadClass(className).newInstance();

            if (!clazz.isAssignableFrom(o.getClass())) {
                throw new MojoFailureException("The class "
                        + o.getClass().getName() + " does not implement "
                        + clazz.getName());
            }
            return o;
        } catch (final InstantiationException e) {
            throw new MojoExecutionException("Failed to instantiate class "
                    + className + ": " + e.getMessage(), e);
        } catch (final ClassCastException e) {
            throw new MojoExecutionException("The class " + className
                    + " is not implementing " + clazz.getName() + ": "
                    + e.getMessage(), e);
        } catch (final IllegalAccessException e) {
            throw new MojoExecutionException("Illegal access to class "
                    + className + ": " + e.getMessage(), e);
        } catch (final ClassNotFoundException e) {
            throw new MojoExecutionException("Class " + className
                    + " not found: " + e.getMessage(), e);
        }
    }

}
