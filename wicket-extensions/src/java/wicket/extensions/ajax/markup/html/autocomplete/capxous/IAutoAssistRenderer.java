package wicket.extensions.ajax.markup.html.autocomplete.capxous;

import wicket.Response;

/**
 * A renderer used to generate html output for the {@link AutoAssistBehavior}.
 * For an explanation of output see the autoassist documentation at
 * http://capxous.com/autoassist/
 * <p>
 * Helper implementations of this interface may abstract the autoassist specific
 * details. Direct implementations of this interface should only be used when
 * total control is required.
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public interface IAutoAssistRenderer
{
	/**
	 * Render the html fragment for the given completion object. Usually the
	 * html is written out by calling {@link Response#write(String)}.
	 * 
	 * @param object
	 *            completion choice object
	 * @param response
	 */
	void render(Object object, Response response);
}
