<xsl:transform xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">

    <!--***********************************************************
     *
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
     *
     ***********************************************************-->

    <!-- This style sheet converts any rat-report.xml file.  -->

    <xsl:output method='xml'/>

    <xsl:variable name='notapproved'>&#10071;</xsl:variable>
    <xsl:variable name="archive"><span style="font-size:x-large;">&#128476;</span></xsl:variable>
    <xsl:variable name="binary"><span style="padding-left:4px;">&#128288;</span></xsl:variable>
    <xsl:variable name="ignored"><span style="padding-left:4px;">&#128683;</span></xsl:variable>
    <xsl:variable name="notice"><span style="font-size:x-large;padding-left:4px;">&#8505;</span></xsl:variable>
    <xsl:variable name="standard"><span style="padding-left:4px;">&#9989;</span></xsl:variable>
    <xsl:variable name="unknown">&#10067;</xsl:variable>
    <xsl:variable name="directory"><span style="padding-left:4px;background-color: #00000087;">&#128194;</span></xsl:variable>

    <xsl:template match="/">
        <html>
            <head>
                <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
                <style type="text/css">
                    <xsl:comment>
                        body {margin-top: 0px;background-color: #F9F7ED;}

                        h1 {color:red;}
                        h2 {color:blue;}
                        h3 {color:blueviolet;}
                        h4 {color:green;}
                        div.section {padding-left:5em;}
                        div.subsection {padding-left:5em;}

                        /* Table Design */
                        table,tr,td {font-weight:bold;border:1px solid #000;}
                        table {min-width: 50%;}
                        caption {color:blue;text-align:left;}

                        .document-name {font-size:larger;}
                        .document-mediatype {float: left;}
                        .document-encoding {padding-left: 2em;}
                        .license-entry {padding-left: 1em;}
                        .license-id {padding-left: 2em;}
                        .license-family {padding-left: 1em;}
                    </xsl:comment>
                </style>
            </head>
            <body>
                <xsl:apply-templates />
            </body>
        </html>
    </xsl:template>

    <xsl:template match="rat-report">

        <h1>Rat Report</h1>

        <xsl:call-template name="summary" />


        <xsl:if test="descendant::resource[license/@approval='false']">
            <xsl:call-template name="unapproved-files" />
        </xsl:if>

        <xsl:if test="descendant::resource[@type='ARCHIVE']">
            <xsl:call-template name="archives" />
        </xsl:if>

        <h2>Detail</h2>

        <p>
            Documents with unapproved licenses will start with a <xsl:value-of select="$notapproved"/>
            The first character on the next line identifies the document type.
        </p>
        <table id="rat-reports symbols" cellspacing="0"
        summary="Symbols used in this RAT report">
        <caption>
            Symbols used in this RAT report
        </caption>
            <tr>
                <th>Symbol</th>
                <th>Type</th>
            </tr>
            <tr>
                <td><xsl:value-of select="$archive"/></td>
                <td>Archive file</td>
            </tr>
            <tr>
                <td><xsl:value-of select="$binary"/></td>
                <td>Binary file</td>
            </tr>
            <tr>
                <td><xsl:value-of select="$ignored"/></td>
                <td>Ignored file</td>
            </tr>
            <tr>
                <td><xsl:value-of select="$notice"/></td>
                <td>Notice file</td>
            </tr>
            <tr>
                <td><xsl:value-of select="$standard"/></td>
                <td>Standard file</td>
            </tr>
            <tr>
                <td><xsl:value-of select="$unknown"/></td>
                <td>Unknown file</td>
            </tr>
            <tr>
                <td><xsl:value-of select="$directory"/></td>
                <td>Directory</td>
            </tr>
        </table>

        <table id="rat-reports resources" cellspacing="0"
               summary="Resources discovered in this RAT report">
            <caption>
                Resources discovered in this RAT report
            </caption>
            <xsl:for-each select='descendant::resource'>
                <tr>
                    <td>
                        <xsl:if test="@isDirectory = 'true'"><xsl:value-of select="$directory"/></xsl:if>
                        <xsl:if test="license/@approval = 'false'"><xsl:value-of select="$notapproved"/></xsl:if>
                        <xsl:choose>
                            <xsl:when test="substring(@type, 1, 1) = 'A'"><xsl:value-of select="$archive"/></xsl:when>
                            <xsl:when test="substring(@type, 1, 1) = 'B'"><xsl:value-of select="$binary"/></xsl:when>
                            <xsl:when test="substring(@type, 1, 1) = 'I'"><xsl:value-of select="$ignored"/></xsl:when>
                            <xsl:when test="substring(@type, 1, 1) = 'N'"><xsl:value-of select="$notice"/></xsl:when>
                            <xsl:when test="substring(@type, 1, 1) = 'S'"><xsl:value-of select="$standard"/></xsl:when>
                            <xsl:when test="substring(@type, 1, 1) = 'U'"><xsl:value-of select="$unknown"/></xsl:when>
                        </xsl:choose>
                    </td>
                    <td>
                        <div class="document-entry">
                            <span class="document-name"><xsl:value-of select='@name'/></span>
                            <div class="document-meta">
                                <span class="document-mediatype"><xsl:value-of select="@mediaType"/></span>
                                <span class="document-encoding"><xsl:value-of select="@encoding"/></span>
                            </div>
                        </div>
                        <xsl:for-each select='descendant::license'>
                            <div class="license-entry">
                                <div>
                                    <xsl:if test="@approval = 'false'">
                                        <span class='approval-icon'><xsl:value-of select="$notapproved"/></span>
                                    </xsl:if>
                                    <span class="license-name"><xsl:value-of select="@name"/></span>
                                </div>
                                <span class="license-id">ID:
                                    <xsl:choose>
                                        <xsl:when test="@id = '?????'">UUID</xsl:when>
                                        <xsl:otherwise><xsl:value-of select="@id"/></xsl:otherwise>
                                    </xsl:choose>
                                </span>
                                <span class="license-family">Family:
                                    <xsl:choose>
                                        <xsl:when test="@family = '?????'">Unknown</xsl:when>
                                        <xsl:otherwise><xsl:value-of select="@family"/></xsl:otherwise>
                                    </xsl:choose>
                                </span>
                            </div>
                        </xsl:for-each>
                    </td>
                </tr>
            </xsl:for-each>
        </table>
    </xsl:template>

    <xsl:template name="summary">
        <h2>Summary</h2>

        <div class="section">
            <p>Generated at <xsl:value-of select='@timestamp' />  by
                <xsl:value-of select='concat(version/@product, " ", rat-report/version/@version, " (", rat-report/version/@vendor, ")")'/>
            </p>

            <table id="rat-reports summary statistics" cellspacing="0"
                   summary="A  summary of statistics from this RAT report">
                <caption>
                    Table 1: A summary of statistics from this RAT report.
                </caption>
                <tr>
                    <th colspan="1">Name:</th>
                    <th colspan="1">Count:</th>
                    <th colspan="1">Description:</th>
                </tr>
                <xsl:for-each select='descendant::statistic'>
                    <tr>
                       <td>
                            <xsl:if test='@approval="false"'>
                                <span class='approval-icon'><xsl:value-of select="$notapproved"/></span>
                            </xsl:if>
                            <xsl:choose>
                                <xsl:when test="@name = '?????'">Unknown</xsl:when>
                                <xsl:otherwise><xsl:value-of select="@name"/></xsl:otherwise>
                            </xsl:choose>
                        </td>
                        <td><xsl:value-of select='@count'/></td>
                        <td><xsl:value-of select='@description'/></td>
                    </tr>
                </xsl:for-each>
            </table>

            <h3>License Statistics</h3>
            <div class="subsection">
                <h4>Categories</h4>
                <div class="subsection">
                    <table id="rat-reports summary license-categories" cellspacing="0"
                           summary="A summary of license categories found in this RAT report">
                        <caption>
                            Table 2: License categories found in this RAT report.
                        </caption>
                        <tr>
                            <th colspan="1">Name:</th>
                            <th colspan="1">Count:</th>
                        </tr>

                        <xsl:for-each select='descendant::licenseCategory'>
                            <tr>
                                <td>
                                    <xsl:if test='@approved="false"'>
                                        <span class='approval-icon'><xsl:value-of select="$notapproved"/></span>
                                    </xsl:if>
                                    <xsl:choose>
                                        <xsl:when test="@name = '?????'">Unknown</xsl:when>
                                        <xsl:otherwise><xsl:value-of select="@name"/></xsl:otherwise>
                                    </xsl:choose>
                                </td>
                                <td><xsl:value-of select='@count'/></td>
                            </tr>
                        </xsl:for-each>
                    </table>
                </div>

                <h4>Licenses</h4>
                <div class="subsection">
                    <table id="rat-reports summary licenses" cellspacing="0"
                           summary="Licenses found in this RAT report">
                        <caption>
                            Table 3: Licenses found in this RAT report.
                        </caption>
                        <tr>
                            <td colspan="1">Name:</td>
                            <td colspan="1">Count:</td>
                        </tr>

                        <xsl:for-each select='descendant::licenseName'>
                            <tr>
                                <td>
                                    <xsl:if test='@approved="false"'>
                                        <span class='approval-icon'><xsl:value-of select="$notapproved"/></span>
                                    </xsl:if>
                                    <xsl:choose>
                                        <xsl:when test="@name = '?????'">Unknown</xsl:when>
                                        <xsl:otherwise><xsl:value-of select="@name"/></xsl:otherwise>
                                    </xsl:choose>
                                </td>
                                <td><xsl:value-of select='@count'/></td>
                            </tr>
                        </xsl:for-each>
                    </table>
                </div>
            </div>

            <h3>Document types</h3>
            <div class="subsection">
                <table id="rat-reports summary document-types" cellspacing="0"
                       summary="Document types found in this RAT report">
                    <caption>
                        Table 4: Document types found in this RAT report.
                    </caption>
                    <tr>
                        <th colspan="1">Name:</th>
                        <th colspan="1">Count:</th>
                    </tr>

                    <xsl:for-each select='descendant::documentType'>
                        <tr>
                            <td><xsl:value-of select="@name"/></td>
                            <td><xsl:value-of select="@count"/></td>
                        </tr>
                    </xsl:for-each>
                </table>
            </div>
        </div>
    </xsl:template>

    <xsl:template name="unapproved-files">
        <h2>Files with unapproved licenses</h2>

        <ul class="unapproved-licenses">
            <xsl:for-each select='descendant::resource[license/@approval="false"]'>
                <li class="document-name"><xsl:value-of select='@name'/></li>
            </xsl:for-each>
        </ul>
    </xsl:template>

    <xsl:template name="archives">
        <h2>Archives</h2>
        <ul>
            <xsl:for-each select='descendant::resource[@type="ARCHIVE"]'>
                <li class="document-name"><xsl:value-of select='@name'/></li>
            </xsl:for-each>
        </ul>
    </xsl:template>


    <xsl:template name="license">
        <xsl:param name="family"/>
        <xsl:param name="id"/>
        <xsl:param name="name"/>
        <xsl:param name="approval"/>

        <tr>
            <td> </td>
            <td>
                <table>
                    <tr>
                        <xsl:if test="$approval = 'false'">
                            <xsl:value-of select="$notapproved"/>
                        </xsl:if>
                    </tr>
                    <tr>
                        <div class="document-license">
                            <span class="license-name"><xsl:value-of select="$name"/></span>
                            <div class="documment-license-meta">
                                <span class="id"><xsl:value-of select="$id"/></span>
                                <span class="family"><xsl:value-of select="$family"/></span>
                            </div>
                        </div>
                    </tr>
                </table>
            </td>
        </tr>
    </xsl:template>
</xsl:transform>
