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
package org.apache.rat.annotation;

import java.io.File;


/**
 * Add an Apache Licence V2 licence header to a 
 * document. This appender does not check for the
 * existence of an existing licence header, it is assumed that either a second
 * licence header is intentional or that there is no licence header present
 * already. 
 * 
 */
public class ApacheV2LicenceAppender extends AbstractLicenceAppender {

  private String copyright;

  /**
   * Create a licence appender with the standard ASF licence header.
   */
  public ApacheV2LicenceAppender() {
    super();
  }

  /**
   * Create a licence appender with the given copyright line. This should be of
   * the form "Copyright 2008 Foo"
   * 
   * @param copyright
   */
  public ApacheV2LicenceAppender(String copyright) {
    super();
    this.copyright = copyright;
  }

  @Override
  public String getLicenceHeader(File document) {
    int type = getType(document);
    StringBuilder sb = new StringBuilder();
    if (copyright == null) {
      sb.append(getFirstLine(type));
      sb.append(getLine(type, ""));
      sb.append(getLine(type, "Licensed to the Apache Software Foundation (ASF) under one"));
      sb.append(getLine(type, "or more contributor license agreements.  See the NOTICE file"));
      sb.append(getLine(type, "distributed with this work for additional information"));
      sb.append(getLine(type, "regarding copyright ownership.  The ASF licenses this file"));
      sb.append(getLine(type, "to you under the Apache License, Version 2.0 (the"));
      sb.append(getLine(type, "\"License\"); you may not use this file except in compliance"));
      sb.append(getLine(type, "with the License.  You may obtain a copy of the License at"));
      sb.append(getLine(type, ""));
      sb.append(getLine(type, "  http://www.apache.org/licenses/LICENSE-2.0"));
      sb.append(getLine(type, ""));
      sb.append(getLine(type, "Unless required by applicable law or agreed to in writing,"));
      sb.append(getLine(type, "software distributed under the License is distributed on an"));
      sb.append(getLine(type, "\"AS IS\" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY"));
      sb.append(getLine(type, "KIND, either express or implied.  See the License for the"));
      sb.append(getLine(type, "specific language governing permissions and limitations"));
      sb.append(getLine(type, "under the License."));
      sb.append(getLine(type, ""));
      sb.append(getLastLine(type));
    } else {
      sb.append(getFirstLine(type));
      sb.append(getLine(type, ""));
      sb.append(getLine(type, copyright));
      sb.append(getLine(type, ""));
      sb.append(getLine(type, "Licensed under the Apache License, Version 2.0 (the \"License\");"));
      sb.append(getLine(type, "you may not use this file except in compliance with the License."));
      sb.append(getLine(type, "You may obtain a copy of the License at"));
      sb.append(getLine(type, ""));
      sb.append(getLine(type, "  http://www.apache.org/licenses/LICENSE-2.0"));
      sb.append(getLine(type, ""));
      sb.append(getLine(type, "Unless required by applicable law or agreed to in writing,"));
      sb.append(getLine(type, "software distributed under the License is distributed on an"));
      sb.append(getLine(type, "\"AS IS\" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY"));
      sb.append(getLine(type, "KIND, either express or implied.  See the License for the"));
      sb.append(getLine(type, "specific language governing permissions and limitations"));
      sb.append(getLine(type, "under the License."));
      sb.append(getLine(type, ""));
      sb.append(getLastLine(type));
    }
    return sb.toString();
  }
  
  

}
