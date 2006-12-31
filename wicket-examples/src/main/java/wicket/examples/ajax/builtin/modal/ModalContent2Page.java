/**
 * 
 */
package wicket.examples.ajax.builtin.modal;

import wicket.ajax.AjaxRequestTarget;
import wicket.ajax.markup.html.AjaxLink;
import wicket.extensions.ajax.markup.html.modal.ModalWindow;
import wicket.markup.html.WebPage;

/**
 * @author Matej Knopp
 *
 */
public class ModalContent2Page extends WebPage {

	/**
	 * 
	 */
	public ModalContent2Page() 
	{
		new AjaxLink(this, "close")
		{
			@Override
			public void onClick(final AjaxRequestTarget target) {
				ModalWindow.close(target);
			}
		};

		
	}


}
