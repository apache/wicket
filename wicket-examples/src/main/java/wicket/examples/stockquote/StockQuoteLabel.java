package wicket.examples.stockquote;

import wicket.MarkupContainer;
import wicket.markup.ComponentTag;
import wicket.markup.MarkupStream;
import wicket.markup.html.WebComponent;
import wicket.model.IModel;
import wicket.model.Model;

/**
 * Gets a stock quote from a webservice. This component is a full blown, built
 * from the ground up component. See the other stock quote examples for
 * alternatives of creating custom components.
 * 
 * The model that is provided is used as the symbol for lookup. When used
 * without an explicit model, the component tries the CompoundPropertyModel
 * method of retrieving the model first. When that results in an empty string,
 * the component will use its ID as symbol.
 * 
 * You can use this component in your webapplication just as you might want to
 * use a <code>Label</code>.
 */
public class StockQuoteLabel extends WebComponent<String>
{
	/**
	 * Constructor
	 * 
	 * @param parent
	 *            The parent of this component The parent of this component.
	 * @param id
	 *            See Component
	 */
	public StockQuoteLabel(final MarkupContainer parent, final String id)
	{
		super(parent, id);
	}

	/**
	 * Convenience constructor. Same as StockQuoteLabel(String, new
	 * Model(String))
	 * @param parent 
	 * 
	 * @param id
	 *            See Component
	 * @param symbol
	 *            The symbol to look up
	 * 
	 * @see wicket.Component#Component(MarkupContainer, String, IModel)
	 */
	public StockQuoteLabel(final MarkupContainer parent, final String id, final String symbol)
	{
		super(parent, id, new Model<String>(symbol));
	}

	/**
	 * @see wicket.Component#Component(MarkupContainer, String, IModel)
	 */
	public StockQuoteLabel(final MarkupContainer parent, final String id, final IModel<String> model)
	{
		super(parent, id, model);
	}

	/**
	 * @see wicket.Component#onComponentTagBody(wicket.markup.MarkupStream,
	 *      wicket.markup.ComponentTag)
	 */
	@Override
	protected void onComponentTagBody(final MarkupStream markupStream, final ComponentTag openTag)
	{
		String symbol = getModelObjectAsString();
		StockQuote quote = new StockQuote(symbol);
		replaceComponentTagBody(markupStream, openTag, quote.getQuote());
	}
}
