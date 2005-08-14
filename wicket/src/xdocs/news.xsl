<?xml version="1.0" encoding="utf-8"?>
<xsl:transform version="1.0" 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:date="http://exslt.org/dates-and-times"
	extension-element-prefixes="date"
	>
<xsl:output method="xml" version="1.0" encoding="iso-8859-1" indent="yes"/>

<xsl:template match="/">
<rss version="0.91">
  <channel>
    <title>Wicket News</title>
    <link>http://wicket.sourceforge.net</link>
    <description>News concerning Wicket</description>
    <language>en-us</language>
    <lastBuildDate><xsl:value-of select="date:format-date(date:date-time(), 'EEE, dd MMM yyyy HH:mm:ss z')"/></lastBuildDate>
    <pubDate><xsl:value-of select="date:format-date(date:date-time(), 'EEE, dd MMM yyyy HH:mm:ss z')"/></pubDate>
    <copyright>Copyright <xsl:value-of select="date:format-date(date:date-time(), 'yyyy')"/> Wicket Development Team</copyright>
	<xsl:apply-templates select="/document/body/section[@name='Latest News']/*" />
  </channel>
</rss>
</xsl:template>

<xsl:template match="subsection">
    <item>
      <title><xsl:value-of select="@name" /></title>
      <link>http://wicket.sourceforge.net/</link>
      <description>
    	<xsl:copy-of select="./*"/>
      </description>
    </item>
</xsl:template>
</xsl:transform>