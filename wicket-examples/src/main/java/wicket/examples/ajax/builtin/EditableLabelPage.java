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

import java.util.Arrays;
import java.util.List;

import wicket.extensions.ajax.markup.html.AjaxEditableChoiceLabel;
import wicket.extensions.ajax.markup.html.AjaxEditableLabel;
import wicket.extensions.ajax.markup.html.AjaxEditableMultiLineLabel;
import wicket.markup.html.basic.Label;
import wicket.markup.html.link.Link;
import wicket.model.AbstractReadOnlyModel;
import wicket.model.CompoundPropertyModel;

/**
 * Page to demo the inplace edit label {@link AjaxEditableLabel}
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public class EditableLabelPage extends BasePage<EditableLabelPage>
{

	/** available sites for the multiple select. */
	private static final List<String> SITES = Arrays.asList(new String[] { "The Server Side",
			"Java Lobby", "Java.Net" });
	private String site = SITES.get(0);
	private String text1 = "fox";
	private String text2 = "dog";
	private String text3 = "multiple\nlines of\ntextual content";
	private int refreshCounter = 0;

	/**
	 * Constructor
	 */
	public EditableLabelPage()
	{
		setModel(new CompoundPropertyModel<EditableLabelPage>(this));
		new AjaxEditableLabel(this, "text1");
		new AjaxEditableLabel(this, "text2");
		new AjaxEditableMultiLineLabel(this, "text3");
		new AjaxEditableChoiceLabel<String>(this, "site", SITES);

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
	 * @return gets text3
	 */
	public String getText3()
	{
		return text3;
	}

	/**
	 * @param text1
	 */
	public void setText1(final String text1)
	{
		this.text1 = text1;
	}

	/**
	 * @param text2
	 */
	public void setText2(final String text2)
	{
		this.text2 = text2;
	}

	/**
	 * @param text3
	 *            the text3 to set
	 */
	public void setText3(final String text3)
	{
		this.text3 = text3;
	}

	/**
	 * @return gets site
	 */
	public String getSite()
	{
		return site;
	}

	/**
	 * @param site
	 *            the site to set
	 */
	public void setSite(final String site)
	{
		this.site = site;
	}
}
