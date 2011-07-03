<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet version="1.0" 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:wicket="http://wicket.apache.org/dtds.data/wicket-xhtml1.4-strict.dtd"
	exclude-result-prefixes="wicket">
	
<xsl:output method="xml" omit-xml-declaration="yes"/>

<!--  
  Just copy everything. This is basically the same as xsl:copy-of. 
  Note: you can not remove the xmlns:wicket from the output by means of XSLT because 
  that would generate ill-formed xml. A single wicket attribute or tag in the output
  requires the xmlns:wicket definition in the output.
-->
<xsl:template match="/ | @* | node()">
  <xsl:copy> <xsl:apply-templates select="@* | node()"/> </xsl:copy>
</xsl:template>

</xsl:stylesheet>