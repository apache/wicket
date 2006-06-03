package wicket.extensions.ajax.markup.html.tabs;

import java.util.List;

import wicket.MarkupContainer;
import wicket.ajax.AjaxRequestTarget;
import wicket.ajax.markup.html.AjaxFallbackLink;
import wicket.extensions.markup.html.tabs.TabbedPanel;
import wicket.markup.html.WebMarkupContainer;

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
	 * @see TabbedPanel#TabbedPanel(MarkupContainer, String, List)
	 */
	public AjaxTabbedPanel(MarkupContainer parent, final String id, List tabs)
	{
		super(parent, id, tabs);
		setOutputMarkupId(true);
	}

	@Override
	protected WebMarkupContainer newLink(MarkupContainer parent, String linkId, final int index)
	{
		return new AjaxFallbackLink(parent, linkId)
		{

			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target)
			{
				setSelectedTab(index);
				if (target != null)
				{
					target.addComponent(AjaxTabbedPanel.this);
				}
				onAjaxUpdate(target);
			}

		};
	}

	/**
	 * A template method that lets users add additional behavior when ajax
	 * update occurs. This method is called after the current tab has been set
	 * so access to it can be obtained via {@link #getSelectedTab()}.
	 * <p>
	 * <strong>Note</strong> Since an {@link AjaxFallbackLink} is used to back
	 * the ajax update the <code>target</code> argument can be null when the
	 * client browser does not support ajax and the fallback mode is used. See
	 * {@link AjaxFallbackLink} for details.
	 * 
	 * @param target
	 *            ajax target used to update this component
	 */
	protected void onAjaxUpdate(AjaxRequestTarget target)
	{
	}

}
