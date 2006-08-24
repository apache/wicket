/*
 * $Id: MarkupParser.java 5791 2006-05-20 00:32:57 +0000 (Sat, 20 May 2006)
 * joco01 $ $Revision$ $Date: 2006-05-20 00:32:57 +0000 (Sat, 20 May
 * 2006) $
 * 
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
package wicket.markup;

import wicket.Application;
import wicket.markup.parser.filter.PrependContextPathHandler;
import wicket.settings.IMarkupSettings;

/**
 * Default implementation of IMarkupParserFactory
 * 
 * @TODO This class doesn't serve any purpose anymore and should be removed
 * @author Igor Vaynberg (ivaynberg)
 */
public class MarkupParserFactory implements IMarkupParserFactory
{
	/** Wicket application object */
	private final Application application;

	/**
	 * Construct.
	 * 
	 */
	public MarkupParserFactory()
	{
		this.application = Application.get();
	}

	/**
	 * Construct.
	 * 
	 * @param application
	 *            Application settings necessary to configure the parser
	 */
	public MarkupParserFactory(final Application application)
	{
		this.application = application;
	}

	/**
	 * @see wicket.markup.IMarkupParserFactory#newMarkupParser()
	 */
	public MarkupParser newMarkupParser(final MarkupResourceStream resource)
	{
		// Create a Markup parser
		final MarkupParser parser = new MarkupParser(resource);

		// Initialize the settings
		initSettings(parser);

		// Add additional markup filters to the chain
		initMarkupFilters(parser);

		return parser;
	}

	/**
	 * Initialize the settings of a new Markup parser. You may subclass the
	 * factory and add additional functionality if needed. Don't forget to call
	 * super.initSettings().
	 * 
	 * @param parser
	 *            The Markup parser
	 */
	protected void initSettings(final MarkupParser parser)
	{
		final IMarkupSettings settings = this.application.getMarkupSettings();
		parser.setCompressWhitespace(settings.getCompressWhitespace());
		parser.setStripComments(settings.getStripComments());
		parser.setDefaultMarkupEncoding(settings.getDefaultMarkupEncoding());
	}

	/**
	 * Register additional markup filters. You may subclass the factory and add
	 * additional filters if needed. Don't forget to call
	 * super.initMarkupFilters(). Or register the markup filter with the new
     * MarkupParser returned by newMarkupParser().
	 * 
	 * @param parser
	 *            The Markup parser
	 */
	protected void initMarkupFilters(final MarkupParser parser)
	{
		parser.registerMarkupFilter(new PrependContextPathHandler(this.application));
	}
}
