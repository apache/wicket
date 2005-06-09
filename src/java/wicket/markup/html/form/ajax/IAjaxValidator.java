/**
 * 
 */
package wicket.markup.html.form.ajax;

import wicket.ajax.IAjaxListener;
import wicket.util.resource.IResourceStream;

/**
 * @author jcompagner
 *
 */
public interface IAjaxValidator extends IAjaxListener
{
	/**
	 * @return 
	 * 
	 */
	public IResourceStream validateInput();
}
