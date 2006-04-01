package wicket.extensions.ajax.markup.html.autocomplete.capxous;

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
public abstract class AbstractAutoAssistRenderer implements IAutoAssistRenderer
{

	/**
	 * @see wicket.extensions.ajax.markup.html.autocomplete.capxous.IAutoAssistRenderer#render(java.lang.Object,
	 *      wicket.Response)
	 */
	public final void render(Object object, Response response)
	{
		response.write("<div onSelect=\"this.txtBox.value='");
		response.write(getTextValue(object));
		response.write("';\">");
		renderAssist(object, response);
		response.write("</div>");
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
	protected abstract void renderAssist(Object object, Response response);

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
