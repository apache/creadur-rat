<?xml version='1.0'?>
<!--
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
  
  http://www.apache.org/licenses/LICENSE-2.0
  
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->
<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:html="http://www.w3.org/1999/xhtml">
  <xsl:output method='xml' indent='yes' encoding='UTF-8' />

  <xsl:template match='html:html'>
    <xsl:comment>
      Licensed under the Apache License, Version 2.0 (the "License");
      you may not use this file except in compliance with the License.
      You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

      Unless required by applicable law or agreed to in writing,
      software distributed under the License is distributed on an "AS
      IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
      express or implied. See the License for the specific language
      governing permissions and limitations under the License.
    </xsl:comment>
    <document>
      <properties>
        <title>
          Audit Report For <xsl:value-of select='//html:span[@class="created"]'/>
        </title>
        <atom
          url="http://mail-archives.apache.org/mod_mbox/incubator-general/?format=atom">
          general@incubator.apache.org Archives
        </atom>
        <link href="http://purl.org/DC/elements/1.0/" rel="schema.DC" />
        </properties>
        <body>
        <xsl:for-each select='//html:div[@class="diff"]'>
          <xsl:call-template name='report'/>
        </xsl:for-each>
        </body>
    </document>
  </xsl:template>

  <xsl:template name='report'>
        <section id='Overview'>
          <title>Overview</title>
          <p>
            This report audits the changes made from
            <xsl:value-of select='.//html:a[@class="start-date"]' />
            till
            <xsl:value-of select='.//html:a[@class="end-date"]' />
            in:
          </p>
          <ul>
            <li>
              <a href='http://www.apache.org/dist/incubator'>
                www.apache.org/dist/incubator
              </a>
            </li>
            <li>
              <a href='http://archive.apache.org/dist/incubator'>
                archive.apache.org/dist/incubator
              </a>
            </li>
          </ul>
        </section>
        <section id='summary'><title>Summary</title>
          <ul>
          <li><xsl:value-of select='count(.//html:ul[@class="added"]//html:li[@class="resource"])'/> files were <a href='#added'>added</a></li>
          <li><xsl:value-of select='count(.//html:ul[@class="modified"]//html:li[@class="resource"])'/> files were <a href='#modified'>modified</a></li>
          <li><xsl:value-of select='count(.//html:ul[@class="deleted"]//html:li[@class="resource"])'/> files were <a href='#deleted'>deleted</a></li>
          </ul>
        </section>
        <section id='details'><title>Details</title>
          <section id='modified'><title>Modified</title>
            <ul>
              <xsl:for-each select='.//html:ul[@class="modified"]/html:li[@class="dir"]'>
                <li>In <cite><xsl:value-of select="text()"/></cite>:
                <ul>
                <xsl:for-each select='.//html:li[@class="resource"]'>
                  <xsl:sort
                    select="text()"
                    data-type = "text"
                    order = "ascending"
                    case-order = "lower-first"/>
                  <li>
                  <strong><xsl:value-of select="text()"/></strong> 
                  </li>
                </xsl:for-each>
                </ul>
                </li>
              </xsl:for-each>
            </ul>
          </section>
          <section id='added'><title>Added</title>
            <ul>
              <xsl:for-each select='.//html:ul[@class="added"]/html:li[@class="dir"]'>
                <li>In <cite><xsl:value-of select="text()"/></cite>:
                <ul>
                <xsl:for-each select='.//html:li[@class="resource"]'>
                  <xsl:sort
                    select="text()"
                    data-type = "text"
                    order = "ascending"
                    case-order = "lower-first"/>
                  <li>
                  <strong><xsl:value-of select="text()"/></strong> 
                  </li>
                </xsl:for-each>
                </ul>
                </li>
              </xsl:for-each>
            </ul>
          </section>
          <section id='deleted'><title>Deleted</title>
            <ul>
              <xsl:for-each select='.//html:ul[@class="deleted"]/html:li[@class="dir"]'>
                <li>In <cite><xsl:value-of select="text()"/></cite>:
                <ul>
                <xsl:for-each select='.//html:li[@class="resource"]'>
                  <xsl:sort
                    select="text()"
                    data-type = "text"
                    order = "ascending"
                    case-order = "lower-first"/>
                  <li>
                  <strong><xsl:value-of select="text()"/></strong> 
                  </li>
                </xsl:for-each>
                </ul>
                </li>
              </xsl:for-each>
            </ul>
          </section>
        </section>
  </xsl:template>  
</xsl:stylesheet>
