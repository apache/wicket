package wicket.examples.stockquote;

import wicket.MarkupContainer;
import wicket.markup.html.basic.Label;
import wicket.model.AbstractReadOnlyModel;

/**
 * StockQuote example provided by Jonathan Locke. This component shows you how
 * to reuse existing components (the StockQuoteLabel ISA Label), and how to use
 * models internally in your component.
 * 
 * Note that this component doesn't work with CompoundPropertyModel's lazy
 * initialization.
 */
public class StockQuoteLabel2 extends Label
{
	/**
	 * Constructor taking the symbol directly.
	 * 
	 * @param parent
	 *            The parent of this component The parent of this component.
	 * @param id
	 *            the component id
	 * @param symbol
	 *            the symbol to look up
	 */
	public StockQuoteLabel2(MarkupContainer parent, final String id, final String symbol)
	{
		super(parent, id, new AbstractReadOnlyModel()
		{
			/**
			 * Gets the stockquote for the given symbol.
			 */
			@Override
			public Object getObject()
			{
				final StockQuote quote = new StockQuote(symbol);
				return quote.getQuote();
			}
		});
	}
}