package wicket.extensions.ajax.markup.html.autocomplete;

import java.io.Serializable;

import wicket.Response;

/**
 * A renderer used to generate html output for the {@link AutoCompleteBehavior}.
 * <p>
 * Helper implementations of this interface may abstract the implementation specific
 * details. Direct implementations of this interface should only be used when
 * total control is required.
 * 
 * @since 1.2
 * 
 * @author Igor Vaynberg (ivaynberg)
 * @author Janne Hietam&auml;ki (jannehietamaki)
 * 
 */
public interface IAutoCompleteRenderer extends Serializable
{
	/**
	 * Render the html fragment for the given completion object. Usually the
	 * html is written out by calling {@link Response#write(CharSequence)}.
	 * 
	 * @param object
	 *            completion choice object
	 * @param response
	 */
	void render(Object object, Response response);


	/**
	 * Render the html header fragment for the completion. Usually the
	 * html is written out by calling {@link Response#write(CharSequence)}.
	 * @param response
	 */
	void renderHeader(Response response);

	/**
	 * Render the html footer fragment for the completion. Usually the
	 * html is written out by calling {@link Response#write(CharSequence)}.
	 * @param response
	 */
	void renderFooter(Response response);

}
