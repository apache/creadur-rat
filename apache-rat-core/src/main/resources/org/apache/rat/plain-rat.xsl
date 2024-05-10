<?xml version='1.0' ?>
<!--
 Licensed to the Apache Software Foundation (ASF) under one   *
 or more contributor license agreements.  See the NOTICE file *
 distributed with this work for additional information        *
 regarding copyright ownership.  The ASF licenses this file   *
 to you under the Apache License, Version 2.0 (the            *
 "License"); you may not use this file except in compliance   *
 with the License.  You may obtain a copy of the License at   *
                                                              *
   http://www.apache.org/licenses/LICENSE-2.0                 *
                                                              *
 Unless required by applicable law or agreed to in writing,   *
 software distributed under the License is distributed on an  *
 "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY       *
 KIND, either express or implied.  See the License for the    *
 specific language governing permissions and limitations      *
 under the License.                                           *
-->
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method='text'/>
<xsl:template match='/'>
*****************************************************
Summary
-------
Generated at: <xsl:value-of select='rat-report/@timestamp'/>

Notes: <xsl:value-of select='count(descendant::resource[attribute::type="NOTICE"])'/>
Binaries: <xsl:value-of select='count(descendant::resource[attribute::type="BINARY"])'/>
Archives: <xsl:value-of select='count(descendant::resource[attribute::type="ARCHIVE"])'/>
Standards: <xsl:value-of select='count(descendant::resource[attribute::type="STANDARD"])'/>

Apache Licensed: <xsl:value-of select='count(descendant::license[attribute::family="AL   "])'/>
Generated Documents: <xsl:value-of select='count(descendant::license[attribute::family="GEN  "])'/>

JavaDocs are generated, thus a license header is optional.
Generated files do not require license headers.

<xsl:value-of select='count(descendant::license[attribute::family="?????"])'/> Unknown Licenses
<xsl:if test="descendant::resource[license/@approval='false']">
*****************************************************

Files with unapproved licenses:

<xsl:for-each select='descendant::resource[license/@approval="false"]'>
  <xsl:text>  </xsl:text>
  <xsl:value-of select='@name'/>
  <xsl:text>
</xsl:text>
</xsl:for-each>
*****************************************************
</xsl:if>
<xsl:if test="descendant::resource[@type='ARCHIVE']">
Archives:
<xsl:for-each select='descendant::resource[@type="ARCHIVE"]'>
  <xsl:text>  </xsl:text>
  <xsl:value-of select='@name'/>
  <xsl:text>
</xsl:text>
 </xsl:for-each>
</xsl:if>
<xsl:text>
*****************************************************
  Documents with unapproved licenses will start with a '!'
  The next character identifies the document type.
   
   char         type
    A       Archive file
    B       Binary file
    G       Generated file
    N       Notice file
    S       Standard file
    U       Unknown file.
  
</xsl:text>
 <xsl:for-each select='descendant::resource'>
  <xsl:choose>
     <xsl:when test='license/@approval="false"'>!</xsl:when>
     <xsl:otherwise><xsl:text> </xsl:text></xsl:otherwise>
   </xsl:choose>
   <xsl:value-of select="substring(@type,1,1)"/><xsl:text> </xsl:text>
   <xsl:value-of select='@name'/><xsl:text>
</xsl:text>
   <xsl:for-each select='descendant::license'>
       <xsl:text>    </xsl:text>
       <xsl:value-of select='substring(concat(@family, "     "),1,5)'/>
       <xsl:text>    </xsl:text>
       <xsl:value-of select='substring(concat(@id, "          "),1,10)'/>
       <xsl:text>    </xsl:text>
       <xsl:value-of select='@name'/>
       <xsl:if test="@approval='false'">
         <xsl:text> (Unapproved)</xsl:text>
       </xsl:if>
       <xsl:text>
</xsl:text>
   </xsl:for-each>
   <xsl:text>
</xsl:text>
 </xsl:for-each>
*****************************************************
<xsl:if test="descendant::resource[/license/@id='?????']">
 Printing headers for text files without a valid license header...
 <xsl:for-each select='descendant::resource[/license/@id="?????"]'>
=====================================================
== File: <xsl:value-of select='@name'/>
=====================================================
<xsl:value-of select='sample'/>
</xsl:for-each>
</xsl:if>
</xsl:template>
</xsl:stylesheet>