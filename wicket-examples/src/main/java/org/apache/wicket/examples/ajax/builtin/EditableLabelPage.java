/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.wicket.examples.ajax.builtin;

import java.util.Arrays;
import java.util.List;

import org.apache.wicket.extensions.ajax.markup.html.AjaxEditableChoiceLabel;
import org.apache.wicket.extensions.ajax.markup.html.AjaxEditableLabel;
import org.apache.wicket.extensions.ajax.markup.html.AjaxEditableMultiLineLabel;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.CompoundPropertyModel;


/**
 * Page to demo the inplace edit label {@link AjaxEditableLabel}
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public class EditableLabelPage extends BasePage
{
	/** available sites for the multiple select. */
	private static final List<String> SITES = Arrays.asList("The Server Side", "Java Lobby",
		"Java.Net");
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
		Form form = new Form("form", new CompoundPropertyModel<EditableLabelPage>(this));
		add(form);

		form.add(new AjaxEditableLabel("text1"));
		form.add(new AjaxEditableLabel("text2"));
		form.add(new AjaxEditableMultiLineLabel("text3"));
		form.add(new AjaxEditableChoiceLabel("site", SITES));

		form.add(new Label("refresh-counter", new AbstractReadOnlyModel<String>()
		{
			@Override
			public String getObject()
			{
				return "" + refreshCounter;
			}
		}));

		form.add(new Link("refresh-link")
		{
			@Override
			public void onClick()
			{
				refreshCounter++;
			}
		});
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

	/**
	 * @param text3
	 *            the text3 to set
	 */
	public void setText3(String text3)
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
	public void setSite(String site)
	{
		this.site = site;
	}
}
