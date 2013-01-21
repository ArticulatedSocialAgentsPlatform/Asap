<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  version="2.0">


<xsl:output method="text"/>
<xsl:output method="xml" indent="yes" name="xml"/>


<xsl:template match="constraints">
    <xsl:apply-templates select="./*" mode="change-ns"/>
</xsl:template>

<xsl:template match="@scope">
	<xsl:attribute name="scope">
  	   <xsl:value-of select="."/>
	</xsl:attribute>
</xsl:template>

<xsl:template match="@stroke[.='point']">
	<xsl:attribute name="pointStroke">true</xsl:attribute>
</xsl:template>

<xsl:template match="gesture">
    <xsl:element name="murml-gesture" namespace="http://www.techfak.uni-bielefeld.de/ags/soa/murml">
    <xsl:element name="murml-description" namespace="http://www.techfak.uni-bielefeld.de/ags/soa/murml">
	<xsl:apply-templates select="@scope"/>
	<xsl:apply-templates select="constraints/@stroke"/>
	<xsl:apply-templates select="constraints"/>
    </xsl:element>
    </xsl:element>
</xsl:template>

<!-- namespace changer from http://stackoverflow.com/questions/553904/namespaces-in-xslt -->
<xsl:template match="@*|node()" priority="-10" mode="change-ns">
    <xsl:copy/>
</xsl:template>

<xsl:template match="definition">
  <bml id="bml1" xmlns="http://www.bml-initiative.org/bml/bml-1.0">
    <xsl:apply-templates/>
  </bml>
</xsl:template>

<xsl:template match="*" mode="change-ns">
  <xsl:element name="{name()}" namespace="http://www.techfak.uni-bielefeld.de/ags/soa/murml">
    <xsl:apply-templates select="@*|node()" mode="change-ns"/>
  </xsl:element>
</xsl:template>


</xsl:stylesheet>
