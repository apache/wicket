package wicket.extensions.ajax.markup.html.autocomplete;

import java.io.Serializable;

import wicket.Response;

/**
 * A renderer used to generate html output for the {@link AutoCompleteBehavior}.
 * <p>
 * Helper implementations of this interface may abstract the implementation specific
 * details. Direct implementations of this interface should only be used when
 * total control is required.
 * <p>
 * The autocompletion value is supplied via an attribute on the first html element
 * named <code>textvalue</code>, if no attribute is found the innerHtml property
 * of the first element will be used instead.
 * 
 * For example:
 * 
 * <pre>
 * new IAutoCompleteRenderer() {
 *     void renderHead(Response r) { r.write("<ul>"); }
 *     
 *     void render(Object o, Response r) {
 *        // notice the textvalue attribute we define for li element
 *        r.write("<li textvalue=\""+o.toString()+"\"><i>"+o.toString()+"</i></li>";
 *     }
 *     
 *     void renderFooter(Response r) { r.write("</ul>"); }
 * }
 * </pre>
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
	 * @param criteria 
	 */
	void render(Object object, Response response, String criteria);


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
