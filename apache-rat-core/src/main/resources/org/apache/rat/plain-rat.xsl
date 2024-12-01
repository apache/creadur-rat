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
    <xsl:variable name='newline'>
        <xsl:text>&#xa;</xsl:text>
    </xsl:variable>
    <xsl:variable name="sectionPartition">
        <xsl:text>*****************************************************</xsl:text>
    </xsl:variable>
    <xsl:variable name="subsectionPartition">
        <xsl:text>-----------------------------------------------------</xsl:text>
    </xsl:variable>

    <xsl:output method='text'/>
    <xsl:template match='/'>
        <xsl:call-template name="section">
            <xsl:with-param name="title">Summary</xsl:with-param>
        </xsl:call-template>
        <xsl:value-of select='concat("Generated at: ", rat-report/@timestamp, $newline, "    by ",
    rat-report/version/@product, " ", rat-report/version/@version, " (", rat-report/version/@vendor, ")")'/>

        <xsl:call-template name="subsection">
            <xsl:with-param name="title">Counters</xsl:with-param>
        </xsl:call-template>

        <xsl:text>    (Entries starting with '!' exceed the minimum or maximum values)</xsl:text>
        <xsl:value-of select='$newline'/>
        <xsl:for-each select='descendant::statistic'>
            <xsl:call-template name="statistic">
                <xsl:with-param name="name"><xsl:value-of select="@name"/></xsl:with-param>
                <xsl:with-param name="count"><xsl:value-of select="@count"/></xsl:with-param>
                <xsl:with-param name="description"><xsl:value-of select="@description"/></xsl:with-param>
                <xsl:with-param name="leadin"><xsl:choose>
                    <xsl:when test='@approval="false"'><xsl:text>! </xsl:text></xsl:when>
                    <xsl:otherwise><xsl:text>  </xsl:text></xsl:otherwise>
                </xsl:choose></xsl:with-param>
            </xsl:call-template>
        </xsl:for-each>

        <xsl:call-template name="subsection">
            <xsl:with-param name="title">Licenses detected</xsl:with-param>
        </xsl:call-template>

        <xsl:for-each select='descendant::licenseName'>
            <xsl:value-of select='concat($newline, @name, ": ", @count, " ")'/>
        </xsl:for-each>

        <xsl:call-template name="subsection">
            <xsl:with-param name="title">License Categories detected</xsl:with-param>
        </xsl:call-template>

        <xsl:for-each select='descendant::licenseCategory'>
            <xsl:value-of select='concat($newline, @name, ": ", @count, " ")'/>
        </xsl:for-each>

        <xsl:call-template name="subsection">
            <xsl:with-param name="title">Document Types detected</xsl:with-param>
        </xsl:call-template>

        <xsl:for-each select='descendant::documentType'>
            <xsl:value-of select='concat($newline, @name, ": ", @count, " ")'/>
        </xsl:for-each>

        <xsl:if test="descendant::resource[license/@approval='false']">
            <xsl:value-of select="concat($newline, $newline)" />
            <xsl:call-template name="section">
                <xsl:with-param name="title">Files with unapproved licenses</xsl:with-param>
            </xsl:call-template>

            <xsl:for-each select='descendant::resource[license/@approval="false"]'>
                <xsl:value-of select='concat($newline, "  ", @name)'/>
            </xsl:for-each>

        </xsl:if>

        <xsl:if test="descendant::resource[@type='ARCHIVE']">
            <xsl:value-of select="concat($newline, $newline)" />
            <xsl:call-template name="section">
                <xsl:with-param name="title">Archives</xsl:with-param>
            </xsl:call-template>

            <xsl:for-each select='descendant::resource[@type="ARCHIVE"]'>
                <xsl:value-of select='concat($newline, " ", @name)'/>
            </xsl:for-each>
        </xsl:if>
        <xsl:value-of select="concat($newline, $newline)" />
        <xsl:call-template name="section">
            <xsl:with-param name="title">Detail</xsl:with-param>
        </xsl:call-template>
        <xsl:text>
  The line following documents with unapproved licenses will start with a '!'
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
            <xsl:call-template name="document">
                <xsl:with-param name="name"><xsl:value-of select='@name'/></xsl:with-param>
                <xsl:with-param name="mediaType"><xsl:value-of select='@mediaType'/></xsl:with-param>
                <xsl:with-param name="encoding"><xsl:value-of select='@encoding'/></xsl:with-param>
                <xsl:with-param name="type"><xsl:value-of select="substring(@type, 1, 1)"/></xsl:with-param>
                <xsl:with-param name="leadin"><xsl:choose>
                    <xsl:when test='license/@approval="false"'>
                        <xsl:text> !</xsl:text>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:text>  </xsl:text>
                    </xsl:otherwise>
                </xsl:choose></xsl:with-param>
            </xsl:call-template>
            <xsl:for-each select='descendant::license'>
                <xsl:call-template name="license">
                    <xsl:with-param name="name"><xsl:value-of select='@name'/></xsl:with-param>
                    <xsl:with-param name="id"><xsl:value-of select='@id'/></xsl:with-param>
                    <xsl:with-param name="family"><xsl:value-of select='@family'/></xsl:with-param>
                    <xsl:with-param name="approval"><xsl:value-of select='@approval'/></xsl:with-param>
                </xsl:call-template>
            </xsl:for-each>
            <xsl:value-of select='concat($newline, $newline)' />
        </xsl:for-each>
        <xsl:value-of select='$newline'/>
    </xsl:template>

    <xsl:template name="section">
        <xsl:param name="title"/>
        <xsl:value-of
                select='concat($sectionPartition, $newline, $title, $newline, $sectionPartition, $newline)'/>
    </xsl:template>

    <xsl:template name="subsection">
        <xsl:param name="title"/>
        <xsl:value-of
                select='concat($newline, $newline, $subsectionPartition, $newline, $title, $newline, $subsectionPartition, $newline)'/>
    </xsl:template>

    <xsl:template name="statistic">
        <xsl:param name="name" />
        <xsl:param name="count" />
        <xsl:param name="description" />
        <xsl:param name="leadin"/>

        <xsl:value-of select='concat($leadin, substring(concat($name, ":                  "), 1, 20),
            $count, "    ", $description, $newline)'/>
    </xsl:template>

    <xsl:template name="document">
        <xsl:param name="name"/>
        <xsl:param name="type"/>
        <xsl:param name="mediaType"/>
        <xsl:param name="encoding" />
        <xsl:param name="leadin"/>

        <xsl:value-of select='concat($name, $newline, $leadin, $type, "         ", $mediaType, "    ",$encoding)'/>
    </xsl:template>

    <xsl:template name="license">
        <xsl:param name="family"/>
        <xsl:param name="id"/>
        <xsl:param name="name"/>
        <xsl:param name="approval"/>

        <xsl:value-of select='concat($newline, "    ", substring(concat($family, "     "), 1, 5),
        "    ", substring(concat($id, "          "), 1,10), "    ", $name)'/>
        <xsl:if test="$approval='false'"> (Unapproved)</xsl:if>
    </xsl:template>
</xsl:stylesheet>
