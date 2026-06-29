<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:output method="text"/>

<!-- emit the text content of the document so a resolved entity would surface -->
<xsl:template match="/">[<xsl:value-of select="/root"/>]</xsl:template>

</xsl:stylesheet>
