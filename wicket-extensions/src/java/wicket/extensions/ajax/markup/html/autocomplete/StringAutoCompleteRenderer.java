package wicket.extensions.ajax.markup.html.autocomplete;

import wicket.Response;

/**
 * An renderer that assumes that assist objects are {@link String}s. Great for
 * quickly generating a list of assists.
 * 
 * @since 1.2
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public class StringAutoCompleteRenderer extends AbstractAutoCompleteRenderer
{
	private static final long serialVersionUID = 1L;

	/**
	 * A singleton instance
	 */
	public static final IAutoCompleteRenderer INSTANCE = new StringAutoCompleteRenderer();


	protected void renderChoices(Object object, Response response)
	{
		response.write(object.toString());
	}

	protected String getTextValue(Object object)
	{
		return object.toString();
	}

}
