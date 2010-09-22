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
package org.apache.wicket.util.tester;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Check;
import org.apache.wicket.markup.html.form.CheckGroup;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;


/**
 * Mock page with form and checkgroup.
 * 
 * @author Frank Bille (billen)
 */
public class MockPageWithFormAndCheckGroup extends WebPage
{
	private static final long serialVersionUID = 1L;

	private List<Integer> selected = new ArrayList<Integer>();

	/**
	 * Construct.
	 */
	public MockPageWithFormAndCheckGroup()
	{
		Form<?> form = new Form<Void>("form");
		add(form);


		CheckGroup<Integer> checkGroup = new CheckGroup<Integer>("checkGroup",
			new PropertyModel<Collection<Integer>>(this, "selected"));
		form.add(checkGroup);

		checkGroup.add(new Check<Integer>("check1", new Model<Integer>(1)));
		checkGroup.add(new Check<Integer>("check2", new Model<Integer>(2)));

		add(new AjaxSubmitLink("submitLink", form)
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form)
			{
				target.add(this);
			}

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form)
			{
			}
		});
	}

	/**
	 * @return selected
	 */
	public List<Integer> getSelected()
	{
		return selected;
	}

	/**
	 * @param selected
	 */
	public void setSelected(List<Integer> selected)
	{
		this.selected = selected;
	}
}
