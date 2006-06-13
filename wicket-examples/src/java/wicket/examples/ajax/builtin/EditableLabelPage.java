/*
 * $Id: AjaxApplication.java 4860 2006-03-12 08:57:48Z ivaynberg $ $Revision:
 * 4860 $ $Date: 2006-03-12 09:57:48 +0100 (So, 12 Mrz 2006) $
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
package wicket.examples.ajax.builtin;

import wicket.extensions.ajax.markup.html.AjaxEditableLabel;
import wicket.markup.html.basic.Label;
import wicket.markup.html.link.Link;
import wicket.model.AbstractReadOnlyModel;
import wicket.model.PropertyModel;

/**
 * Page to demo the inplace edit label {@link AjaxEditableLabel}
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public class EditableLabelPage extends BasePage
{
	private String text1 = "fox";
	private String text2 = "dog";
	private int refreshCounter = 0;

	/**
	 * Constructor
	 */
	public EditableLabelPage()
	{
		new AjaxEditableLabel<String>(this, "text1", new PropertyModel<String>(this, "text1"));
		new AjaxEditableLabel<String>(this, "text2", new PropertyModel<String>(this, "text2"));

		new Label(this, "refresh-counter", new AbstractReadOnlyModel()
		{
			@Override
			public Object getObject()
			{
				return "" + refreshCounter;
			}
		});

		new Link(this, "refresh-link")
		{
			@Override
			public void onClick()
			{
				refreshCounter++;
			}
		};
	}

	/**
	 * @return text1
	 */
	public String getText1()
	{
		return text1;
	}

	/**
	 * @return text2
	 */
	public String getText2()
	{
		return text2;
	}

	/**
	 * @param text1
	 */
	public void setText1(String text1)
	{
		this.text1 = text1;
	}

	/**
	 * @param text2
	 */
	public void setText2(String text2)
	{
		this.text2 = text2;
	}
}
