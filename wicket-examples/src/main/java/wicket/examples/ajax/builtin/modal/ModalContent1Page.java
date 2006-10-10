/**
 * 
 */
package wicket.examples.ajax.builtin.modal;

import wicket.Page;
import wicket.ajax.AjaxRequestTarget;
import wicket.ajax.markup.html.AjaxLink;
import wicket.extensions.ajax.markup.html.modal.ModalWindow;
import wicket.markup.html.WebPage;

/**
 * @author Matej Knopp
 * 
 */
public class ModalContent1Page extends WebPage {
 
	/**
	 * 
	 */
	public ModalContent1Page() {
		this(null);
	}

	/**
	 * 
	 * @param modalWindowPage
	 */
	public ModalContent1Page(final ModalWindowPage modalWindowPage) 
	{
		new AjaxLink(this, "closeOK") 
		{
			@Override
			public void onClick(AjaxRequestTarget target) {
				if (modalWindowPage != null)
				modalWindowPage.setResult("Modal window 1 - close link OK");
				ModalWindow.close(target);
			}
		};

		new AjaxLink(this, "closeCancel") 
		{
			@Override
			public void onClick(AjaxRequestTarget target) 
			{
				if (modalWindowPage != null)
					modalWindowPage.setResult("Modal window 1 - close link Cancel");
				ModalWindow.close(target);
			}
		};

		final ModalWindow modal = new ModalWindow(this, "modal");
		modal.setPageMapName("modal-1");
				
		modal.setResizable(false);		
		modal.setInitialWidth(30);
		modal.setInitialHeight(15);
		modal.setWidthUnit("em");
		modal.setHeightUnit("em");
		
		modal.setCssClassName(ModalWindow.CSS_CLASS_GRAY);

		modal.setPageCreator(new ModalWindow.PageCreator() {
			public Page createPage() {
				return new ModalContent2Page();
			}
		});

		modal.setCloseButtonCallback(new ModalWindow.CloseButtonCallback() {
			public boolean onCloseButtonClicked(AjaxRequestTarget target) {
				target.appendJavascript("alert('You can\\'t close this modal window using close button."
						+ " Use the link inside the window instead.');");
				return false;
			}
		});

		new AjaxLink(this, "open") {
			@Override
			public void onClick(AjaxRequestTarget target) {
				modal.show(target);
			}
		};

	}
}
