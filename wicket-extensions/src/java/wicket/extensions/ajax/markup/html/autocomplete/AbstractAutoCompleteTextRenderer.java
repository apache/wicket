package wicket.extensions.ajax.markup.html.autocomplete;

import wicket.Response;

/**
 * Base for text renderers that simply want to show a string
 * 
 * @sicne 1.2
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public abstract class AbstractAutoCompleteTextRenderer extends AbstractAutoCompleteRenderer
{
	/**
	 * @see AbstractAutoCompleteRenderer#renderChoice(Object, Response)
	 */
	protected void renderChoice(Object object, Response response, String criteria)
	{
		response.write(getTextValue(object));
	}

}
