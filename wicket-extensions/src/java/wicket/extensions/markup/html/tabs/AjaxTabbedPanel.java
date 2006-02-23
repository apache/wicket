package wicket.extensions.markup.html.tabs;

import java.util.List;

import wicket.ajax.AjaxRequestTarget;
import wicket.ajax.markup.html.AjaxFallbackLink;
import wicket.markup.html.link.Link;

/**
 * Ajaxified version of the tabbed panel. Uses AjaxFallbackLink instead of
 * regular wicket links so it can update itself inplace.
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public class AjaxTabbedPanel extends TabbedPanel
{
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 * @param id
	 * @param tabs
	 */
	public AjaxTabbedPanel(String id, List tabs)
	{
		super(id, tabs);
		setOutputMarkupId(true);
	}
	
	protected Link newLink(String linkId, final int index)
	{
		return new AjaxFallbackLink(linkId) {

			private static final long serialVersionUID = 1L;

			protected void onClick(AjaxRequestTarget target)
			{
				setSelectedTab(index);
				if (target!=null) {
					target.addComponent(AjaxTabbedPanel.this);
				}
			}
			
		};
	}


}
