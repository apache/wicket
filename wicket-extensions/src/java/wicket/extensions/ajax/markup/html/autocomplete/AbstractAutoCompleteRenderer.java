package wicket.extensions.ajax.markup.html.autocomplete;

import wicket.Response;

/**
 * A renderer that abstracts autoassist specific details and allows subclasses
 * to only render the visual part of the assist instead of having to also render
 * the necessary autoassist javascript hooks.
 * 
 * @since 1.2
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public abstract class AbstractAutoCompleteRenderer implements IAutoCompleteRenderer
{

	/**
	 * @see wicket.extensions.ajax.markup.html.autocomplete.IAutoCompleteRenderer#render(java.lang.Object,
	 *      wicket.Response)
	 */
	public final void render(Object object, Response response, String criteria)
	{
		String textValue = getTextValue(object);
		if (textValue == null)
		{
			throw new IllegalStateException(
					"A call to textValue(Object) returned an illegal value: null for object: "
							+ object.toString());
		}
		textValue = textValue.replaceAll("\\\"", "&quot;");
		
		response.write("<li textvalue=\"" + textValue + "\">");
		renderChoice(object, response, criteria);
		response.write("</li>");
	}


	/**
	 * @see wicket.extensions.ajax.markup.html.autocomplete.IAutoCompleteRenderer#renderHeader(wicket.Response)
	 */
	public final void renderHeader(Response response)
	{
		response.write("<ul>");
	}

	/**
	 * @see wicket.extensions.ajax.markup.html.autocomplete.IAutoCompleteRenderer#renderFooter(wicket.Response)
	 */
	public final void renderFooter(Response response)
	{
		response.write("</ul>");
	}

	/**
	 * Render the visual portion of the assist. Usually the html representing
	 * the assist choice object is written out to the response use
	 * {@link Response#write(CharSequence)}
	 * 
	 * @param object
	 *            current assist choice
	 * @param response
	 */
	protected abstract void renderChoice(Object object, Response response, String criteria);

	/**
	 * Retrieves the text value that will be set on the textbox if this assist
	 * is selected
	 * 
	 * @param object
	 *            assist choice object
	 * @return the text value that will be set on the textbox if this assist is
	 *         selected
	 */
	protected abstract String getTextValue(Object object);
}
