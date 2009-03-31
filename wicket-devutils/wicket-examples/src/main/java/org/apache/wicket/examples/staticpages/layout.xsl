<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version='1.0'>
	<xsl:template match="/html/body">
		<p><b>Passed through the XSL stylesheet!</b></p>
		<div style="background-color: #D0D0D0; margin-left: 2em; padding: 0 .5em">
			<xsl:apply-templates/>
		</div>
	</xsl:template>

	<xsl:template match='node()|@*'>
		<xsl:copy>
			<xsl:apply-templates select='node()|@*'/>
		</xsl:copy>
	</xsl:template>
</xsl:stylesheet>
