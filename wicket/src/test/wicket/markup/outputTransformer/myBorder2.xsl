<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet version="1.0" 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:wicket="http://wicket.sourceforge.net">
	
<xsl:output method="xml" omit-xml-declaration="yes"/>

<xsl:template match="*">
  <xsl:copy> <xsl:apply-templates/> </xsl:copy>
</xsl:template>

</xsl:stylesheet>