package wicket.extensions.ajax.markup.html.autocomplete;


/**
 * An renderer that assumes that assist objects are {@link String}s. Great for
 * quickly generating a list of assists.
 * 
 * @since 1.2
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public final class StringAutoCompleteRenderer extends AbstractAutoCompleteTextRenderer
{
	private static final long serialVersionUID = 1L;

	/**
	 * A singleton instance
	 */
	public static final IAutoCompleteRenderer INSTANCE = new StringAutoCompleteRenderer();

	/**
	 * @see AbstractAutoCompleteTextRenderer#getTextValue(Object)
	 */
	protected String getTextValue(Object object)
	{
		return object.toString();
	}

}
