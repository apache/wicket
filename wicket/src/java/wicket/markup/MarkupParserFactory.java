package wicket.markup;

import wicket.Application;
import wicket.markup.parser.IMarkupFilter;
import wicket.markup.parser.filter.PrependContextPathHandler;
import wicket.settings.IMarkupSettings;

/**
 * Default implementation of IMarkupParserFactory
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public class MarkupParserFactory implements IMarkupParserFactory
{
	/** Wicket application object */
	private final Application application;

	/** @deprecated since 2.0 */
	private IMarkupFilter[] filters;

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
	 * Construct.
	 * <p>
	 * <b>Note:<b> be careful when you use this constructor. All additional
	 * filters provided MUST be stateless and not keep any information about the
	 * markup. The filter does not know when a new markup file starts or ends.
	 * 
	 * @param application
	 *            Application settings necessary to configure the parser
	 * @param filters
	 *            additional markup filters
	 * @deprecated since 2.0 please subclass and replace initMarkupFilters()
	 *             instead.
	 */
	public MarkupParserFactory(final Application application, final IMarkupFilter[] filters)
	{
		this.application = application;
		this.filters = filters;
	}

	/**
	 * Construct.
	 * <p>
	 * <b>Note:<b> be careful when you use this constructor. All additional
	 * filters provided MUST be stateless and not keep any information about the
	 * markup. The filter does not know when a new markup file starts or ends.
	 * 
	 * @param application
	 *            Application settings necessary to configure the parser
	 * @param filter
	 *            additional markup filter
	 * @deprecated since 2.0 please subclass and replace initMarkupFilters()
	 *             instead.
	 */
	public MarkupParserFactory(final Application application, final IMarkupFilter filter)
	{
		this.application = application;
		this.filters = new IMarkupFilter[] { filter };
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
		if (this.filters != null)
		{
			for (IMarkupFilter filter : this.filters)
			{
				parser.registerMarkupFilter(filter);
			}
		}
		parser.registerMarkupFilter(new PrependContextPathHandler(this.application));
	}
}
