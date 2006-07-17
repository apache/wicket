package wicket.markup;

import wicket.Application;
import wicket.markup.parser.IMarkupFilter;
import wicket.markup.parser.XmlPullParser;
import wicket.markup.parser.filter.PrependContextPathHandler;

/**
 * Default implementation of IMarkupParserFactory
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public class MarkupParserFactory implements IMarkupParserFactory
{
	private IMarkupFilter[] filters;

	/**
	 * Construct.
	 * 
	 * @param application
	 *            Application settings necessary to configure the parser
	 */
	public MarkupParserFactory(final Application application)
	{
		this.filters = new IMarkupFilter[] { new PrependContextPathHandler(application) };
	}

	/**
	 * Construct.
	 * 
	 * @param application
	 *            Application settings necessary to configure the parser
	 * @param filters
	 *            additional markup filters
	 */
	public MarkupParserFactory(final Application application, final IMarkupFilter[] filters)
	{
		this.filters = new IMarkupFilter[filters.length + 1];
		System.arraycopy(filters, 0, this.filters, 0, filters.length);
		this.filters[filters.length] = new PrependContextPathHandler(application);
	}

	/**
	 * Construct.
	 * 
	 * @param application
	 *            Application settings necessary to configure the parser
	 * @param filter
	 *            additional markup filter
	 */
	public MarkupParserFactory(final Application application, final IMarkupFilter filter)
	{
		this.filters = new IMarkupFilter[] { filter, new PrependContextPathHandler(application) };
	}

	/**
	 * @see wicket.markup.IMarkupParserFactory#newMarkupParser()
	 */
	public MarkupParser newMarkupParser()
	{
		final MarkupParser parser = new MarkupParser(new XmlPullParser())
		{
			@Override
			public void initFilterChain()
			{
				if (filters != null)
				{
					for (IMarkupFilter element : filters)
					{
						appendMarkupFilter(element);
					}
				}
			}
		};
		return parser;
	}
}
