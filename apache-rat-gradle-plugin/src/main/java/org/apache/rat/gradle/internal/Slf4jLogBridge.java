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
package org.apache.rat.gradle.internal;

import org.apache.rat.utils.Log;
import org.slf4j.Logger;

/** RAT {@link Log} implementation backed by an SLF4J {@link Logger}. */
public class Slf4jLogBridge implements Log {
  /** The SLF4J logger to delegate to. */
  private final Logger logger;

  public Slf4jLogBridge(final Logger logger) {
    this.logger = logger;
  }

  @Override
  public Level getLevel() {
    // We don't know the "minimum" log level for an SLF4J logger, so we just return DEBUG.
    return Level.DEBUG;
  }

  @Override
  public boolean isEnabled(final Level level) {
    switch (level) {
      case DEBUG:
        return logger.isDebugEnabled();
      case INFO:
        return logger.isInfoEnabled();
      case WARN:
        return logger.isWarnEnabled();
      case ERROR:
        return logger.isErrorEnabled();
      default:
        throw new IllegalArgumentException("Unknown log level: " + level);
    }
  }

  @Override
  public void log(final Level level, final String message) {
    switch (level) {
      case DEBUG:
        logger.debug(message);
        break;
      case INFO:
        logger.info(message);
        break;
      case WARN:
        logger.warn(message);
        break;
      case ERROR:
        logger.error(message);
        break;
      default:
        throw new IllegalArgumentException("Unknown log level: " + level);
    }
  }
}
