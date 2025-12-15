/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.udf.entity;

import java.util.Date;

// taken from https://github.com/apache/linkis/blob/master/linkis-public-enhancements/linkis-pes-common/src/main/java/org/apache/linkis/udf/entity/UDFVersion.java
public class ChineseCommentsJava {
  private Long id;
  private Long udfId;
  private String path; // 仅存储用户上一次上传的路径 作提示用
  private String bmlResourceId;
  private String bmlResourceVersion;
  private Boolean isPublished; // 共享udf被使用的是已发布的最新版本
  private String registerFormat;
  private String useFormat;
  private String description;
  private Date createTime;

  /** Constructors and method taken away to only parse above comments but no meaningful Java class :) */
  private String md5;

  public void setCreateTime(Date createTime) {
    this.createTime = createTime;
  }
}

