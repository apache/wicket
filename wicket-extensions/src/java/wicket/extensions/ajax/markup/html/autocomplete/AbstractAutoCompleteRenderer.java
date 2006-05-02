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
	public final void render(Object object, Response response)
	{
	        response.write("<li>");
		renderChoice(object, response);
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
	protected abstract void renderChoice(Object object, Response response);

}
