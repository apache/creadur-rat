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
package org.apache.rat.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.JavaBasePlugin

class RatPlugin implements Plugin<Project> {

    void apply( Project project ) {
        configureDependencies( project )
        Task ratTask = project.task(
            'rat',
            type: RatTask,
            group: 'Apache Creadur',
            description: 'Runs Apache Rat checks'
        )
        if( project.plugins.hasPlugin( JavaBasePlugin ) ) {
            project.tasks[JavaBasePlugin.CHECK_TASK_NAME].dependsOn ratTask
        }
    }

    void configureDependencies( Project project ) {
        project.configurations {
            rat
        }
        project.repositories {
            jcenter()
            // maven { url 'https://repository.apache.org/snapshots' }
        }
        project.dependencies {
            rat 'org.apache.rat:apache-rat-tasks:0.11'
            // rat 'org.apache.rat:apache-rat-tasks:0.12-SNAPSHOT'
        }
    }
}
