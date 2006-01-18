package wicket.markup;

import wicket.markup.parser.IMarkupFilter;
import wicket.markup.parser.XmlPullParser;
import wicket.settings.IMarkupSettings;

/**
 * Default implementation of IMarkupParserFactory
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public class MarkupParserFactory implements IMarkupParserFactory
{
	private IMarkupFilter[] filters;
	private IMarkupSettings settings;

	/**
	 * Construct.
	 * 
	 * @param settings
	 *            markup settings necessary to configure the parser
	 */
	public MarkupParserFactory(IMarkupSettings settings)
	{
		this.settings = settings;
	}

	/**
	 * Construct.
	 * 
	 * @param settings
	 *            markup settings necessary to configure the parser
	 * @param filters
	 *            additional markup filters
	 */
	public MarkupParserFactory(IMarkupSettings settings, IMarkupFilter[] filters)
	{
		this(settings);
		this.filters = filters;
	}

	/**
	 * Construct.
	 * 
	 * @param settings
	 *            markup settings necessary to configure the parser
	 * @param filter
	 *            additional markup filter
	 */
	public MarkupParserFactory(IMarkupSettings settings, IMarkupFilter filter)
	{
		this(settings);
		this.filters = new IMarkupFilter[] { filter };
	}
	
	/**
	 * @see wicket.markup.IMarkupParserFactory#newMarkupParser()
	 */
	public MarkupParser newMarkupParser()
	{
		final MarkupParser parser = new MarkupParser(new XmlPullParser(settings
				.getDefaultMarkupEncoding()))
		{
			public void initFilterChain()
			{
				if (filters != null)
				{
					for (int i = 0; i < filters.length; i++)
					{
						appendMarkupFilter(filters[i]);
					}
				}
			}
		};
		return parser;
	}
}
