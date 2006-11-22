/*
 * $Id: AjaxLinkPage.java 4633 2006-02-26 00:22:21Z dashorst $ $Revision$
 * $Date: 2006-02-26 01:22:21 +0100 (So, 26 Feb 2006) $
 * 
 * ==================================================================== Licensed
 * under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the
 * License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.ajax.markup.html.ajaxLink;

import wicket.ajax.AjaxRequestTarget;
import wicket.ajax.markup.html.AjaxLink;
import wicket.markup.html.WebPage;
import wicket.markup.html.basic.Label;
import wicket.markup.html.border.BoxBorder;

/**
 * 
 */
public class AjaxPage2 extends WebPage
{
	private static final long serialVersionUID = 1L;
	
	private Label ajaxLabel;
	private BoxBorder myBorder;

	/**
	 * Construct.
	 */
	public AjaxPage2()
	{
		super();

		myBorder = new BoxBorder("pageLayout");
		myBorder.setTransparentResolver(true);
		add(myBorder);

		ajaxLabel = new Label("ajaxLabel", "AAAAAAA");
		ajaxLabel.setOutputMarkupId(true);
		add(ajaxLabel);

		add(new AjaxLink("ajaxLink")
		{
			private static final long serialVersionUID = 1L;

			public void onClick(final AjaxRequestTarget target)
			{
				Label ajaxLabel2 = new Label("ajaxLabel", "BBBBBBB");
				ajaxLabel2.setOutputMarkupId(true);
				ajaxLabel.getParent().replace(ajaxLabel2);
				if (target != null)
				{
					target.addComponent(ajaxLabel2, "ajaxLabel");
				}
			}
		});
	}
}