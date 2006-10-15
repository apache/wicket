/*
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.jmx;


/**
 * Exposes Application related functionality for JMX.
 * 
 * @author eelcohillenius
 */
public class MarkupSettings implements MarkupSettingsMBean
{
	private final wicket.Application application;

	/**
	 * Create.
	 * 
	 * @param application
	 */
	public MarkupSettings(wicket.Application application)
	{
		this.application = application;
	}

	/**
	 * @see wicket.jmx.MarkupSettingsMBean#getAutomaticLinking()
	 */
	public boolean getAutomaticLinking()
	{
		return application.getMarkupSettings().getAutomaticLinking();
	}

	/**
	 * @see wicket.jmx.MarkupSettingsMBean#getCompressWhitespace()
	 */
	public boolean getCompressWhitespace()
	{
		return application.getMarkupSettings().getCompressWhitespace();
	}

	/**
	 * @see wicket.jmx.MarkupSettingsMBean#getDefaultAfterDisabledLink()
	 */
	public String getDefaultAfterDisabledLink()
	{
		return application.getMarkupSettings().getDefaultAfterDisabledLink();
	}

	/**
	 * @see wicket.jmx.MarkupSettingsMBean#getDefaultBeforeDisabledLink()
	 */
	public String getDefaultBeforeDisabledLink()
	{
		return application.getMarkupSettings().getDefaultBeforeDisabledLink();
	}

	/**
	 * @see wicket.jmx.MarkupSettingsMBean#getDefaultMarkupEncoding()
	 */
	public String getDefaultMarkupEncoding()
	{
		return application.getMarkupSettings().getDefaultMarkupEncoding();
	}

	/**
	 * @see wicket.jmx.MarkupSettingsMBean#getMarkupParserFactory()
	 */
	public String getMarkupParserFactory()
	{
		return Stringz.className(application.getMarkupSettings().getMarkupParserFactory());
	}

	/**
	 * @see wicket.jmx.MarkupSettingsMBean#getStripComments()
	 */
	public boolean getStripComments()
	{
		return application.getMarkupSettings().getStripComments();
	}

	/**
	 * @see wicket.jmx.MarkupSettingsMBean#getStripWicketTags()
	 */
	public boolean getStripWicketTags()
	{
		return application.getMarkupSettings().getStripWicketTags();
	}

	/**
	 * @see wicket.jmx.MarkupSettingsMBean#getStripXmlDeclarationFromOutput()
	 */
	public boolean getStripXmlDeclarationFromOutput()
	{
		return application.getMarkupSettings().getStripXmlDeclarationFromOutput();
	}

	/**
	 * @see wicket.jmx.MarkupSettingsMBean#setAutomaticLinking(boolean)
	 */
	public void setAutomaticLinking(boolean automaticLinking)
	{
		application.getMarkupSettings().setAutomaticLinking(automaticLinking);
	}

	/**
	 * @see wicket.jmx.MarkupSettingsMBean#setCompressWhitespace(boolean)
	 */
	public void setCompressWhitespace(boolean compressWhitespace)
	{
		application.getMarkupSettings().setCompressWhitespace(compressWhitespace);
	}

	/**
	 * @see wicket.jmx.MarkupSettingsMBean#setDefaultAfterDisabledLink(java.lang.String)
	 */
	public void setDefaultAfterDisabledLink(String defaultAfterDisabledLink)
	{
		application.getMarkupSettings().setDefaultAfterDisabledLink(defaultAfterDisabledLink);
	}

	/**
	 * @see wicket.jmx.MarkupSettingsMBean#setDefaultBeforeDisabledLink(java.lang.String)
	 */
	public void setDefaultBeforeDisabledLink(String defaultBeforeDisabledLink)
	{
		application.getMarkupSettings().setDefaultBeforeDisabledLink(defaultBeforeDisabledLink);
	}

	/**
	 * @see wicket.jmx.MarkupSettingsMBean#setDefaultMarkupEncoding(java.lang.String)
	 */
	public void setDefaultMarkupEncoding(String encoding)
	{
		application.getMarkupSettings().setDefaultMarkupEncoding(encoding);
	}

	/**
	 * @see wicket.jmx.MarkupSettingsMBean#setStripComments(boolean)
	 */
	public void setStripComments(boolean stripComments)
	{
		application.getMarkupSettings().setStripComments(stripComments);
	}

	/**
	 * @see wicket.jmx.MarkupSettingsMBean#setStripWicketTags(boolean)
	 */
	public void setStripWicketTags(boolean stripWicketTags)
	{
		application.getMarkupSettings().setStripWicketTags(stripWicketTags);
	}

	/**
	 * @see wicket.jmx.MarkupSettingsMBean#setStripXmlDeclarationFromOutput(boolean)
	 */
	public void setStripXmlDeclarationFromOutput(boolean strip)
	{
		application.getMarkupSettings().setStripXmlDeclarationFromOutput(strip);
	}
}
