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
  xmlns:html="http://www.w3.org/1999/xhtml"><xsl:output method='text' encoding='iso-8859-1' /><xsl:template match='/'>
Audit Report: <xsl:value-of select='//html:a[@class="start-date"]' /> -> <xsl:value-of select='//html:a[@class="end-date"]' />
======================================
<xsl:choose><xsl:when test='count(//html:ul[@class="added"]//html:li[@class="resource"]) + count(//html:ul[@class="modified"]//html:li[@class="resource"]) + count(//html:ul[@class="deleted"]//html:li[@class="resource"]) > 0 '>
 * <xsl:value-of select='count(//html:ul[@class="added"]//html:li[@class="resource"])'/> files were added
 * <xsl:value-of select='count(//html:ul[@class="modified"]//html:li[@class="resource"])'/> files were modified
 * <xsl:value-of select='count(//html:ul[@class="deleted"]//html:li[@class="resource"])'/> files were deleted
<xsl:if test='count(//html:ul[@class="modified"]//html:li[@class="resource"])'>
Modified
--------<xsl:for-each select='//html:ul[@class="modified"]/html:li[@class="dir"]'><xsl:if test='count(.//html:li[@class="resource"])'>
In <xsl:value-of select="text()"/>:<xsl:for-each select='.//html:li[@class="resource"]'><xsl:sort select="text()" data-type = "text" order = "ascending" case-order = "lower-first"/>
 * <xsl:value-of select="text()"/>
</xsl:for-each></xsl:if></xsl:for-each></xsl:if>
<xsl:if test='count(//html:ul[@class="added"]//html:li[@class="resource"])'>

Added
-----<xsl:for-each select='.//html:ul[@class="added"]/html:li[@class="dir"]'><xsl:if test='count(.//html:li[@class="resource"])'>
In <xsl:value-of select="text()"/>:<xsl:for-each select='.//html:li[@class="resource"]'><xsl:sort select="text()" data-type = "text" order = "ascending" case-order = "lower-first"/>
 * <xsl:value-of select="text()"/>
</xsl:for-each></xsl:if></xsl:for-each></xsl:if>
<xsl:if test='count(//html:ul[@class="deleted"]//html:li[@class="resource"])'>

Deleted
-------<xsl:for-each select='.//html:ul[@class="deleted"]/html:li[@class="dir"]'><xsl:if test='count(.//html:li[@class="resource"])'>
In <xsl:value-of select="text()"/>:<xsl:for-each select='.//html:li[@class="resource"]'><xsl:sort select="text()" data-type = "text" order = "ascending" case-order = "lower-first"/>
 * <xsl:value-of select="text()"/>
</xsl:for-each></xsl:if></xsl:for-each></xsl:if>
 </xsl:when>
  <xsl:otherwise>
Move along! Nothing to see here!
  </xsl:otherwise>
</xsl:choose>
  </xsl:template>
</xsl:stylesheet>
