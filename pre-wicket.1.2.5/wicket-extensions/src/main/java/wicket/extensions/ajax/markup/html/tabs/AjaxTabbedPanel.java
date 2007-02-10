/*
 * $Id$ $Revision$ $Date$
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.extensions.ajax.markup.html.tabs;

import java.util.List;

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
	 * Constructor
	 * 
	 * @param id
	 * @param tabs
	 */
	public AjaxTabbedPanel(String id, List tabs)
	{
		super(id, tabs);
		setOutputMarkupId(true);
		 
		setVersioned(false);
	}

	protected WebMarkupContainer newLink(String linkId, final int index)
	{
		return new AjaxFallbackLink(linkId)
		{

			private static final long serialVersionUID = 1L;

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
	 * the ajax update the <code>target</code> argument can be null when the client
	 * browser does not support ajax and the fallback mode is used. See
	 * {@link AjaxFallbackLink} for details.
	 * 
	 * @param target
	 *            ajax target used to update this component
	 */
	protected void onAjaxUpdate(AjaxRequestTarget target)
	{
	}

}
