package wicket.markup;

import wicket.Application;
import wicket.markup.parser.IMarkupFilter;
import wicket.markup.parser.XmlPullParser;

/**
 * Default implementation of IMarkupParserFactory
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public class MarkupParserFactory implements IMarkupParserFactory
{
	private IMarkupFilter[] filters;
	private Application application;

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
	 * Construct.
	 * 
	 * @param application
	 *            Application settings necessary to configure the parser
	 * @param filters
	 *            additional markup filters
	 */
	public MarkupParserFactory(final Application application, IMarkupFilter[] filters)
	{
		this(application);
		this.filters = filters;
	}

	/**
	 * Construct.
	 * 
	 * @param application
	 *            Application settings necessary to configure the parser
	 * @param filter
	 *            additional markup filter
	 */
	public MarkupParserFactory(final Application application, IMarkupFilter filter)
	{
		this(application);
		this.filters = new IMarkupFilter[] { filter };
	}

	/**
	 * @see wicket.markup.IMarkupParserFactory#newMarkupParser()
	 */
	public MarkupParser newMarkupParser()
	{
		final MarkupParser parser = new MarkupParser(application, new XmlPullParser())
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
