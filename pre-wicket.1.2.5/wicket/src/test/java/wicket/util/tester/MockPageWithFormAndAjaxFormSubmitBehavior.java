/*
 * $Id$
 * $Revision$
 * $Date$
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
package wicket.util.tester;

import java.io.Serializable;

import wicket.ajax.AjaxRequestTarget;
import wicket.ajax.form.AjaxFormSubmitBehavior;
import wicket.markup.html.WebComponent;
import wicket.markup.html.WebPage;
import wicket.markup.html.form.Form;
import wicket.markup.html.form.TextField;
import wicket.model.CompoundPropertyModel;

/**
 * Mock page used for testing executeAjaxEvent.
 * 
 * @author Frank Bille (billen)
 */
public class MockPageWithFormAndAjaxFormSubmitBehavior extends WebPage
{
	private static final long serialVersionUID = 1L;

	private boolean executed = false;

	private Pojo pojo;

	/**
	 * Construct.
	 */
	public MockPageWithFormAndAjaxFormSubmitBehavior()
	{
		pojo = new Pojo("Mock name");

		Form form = new Form("form", new CompoundPropertyModel(pojo));
		add(form);

		form.add(new TextField("name"));

		// The Event behavior
		WebComponent eventComponent = new WebComponent("eventComponent");
		add(eventComponent);
		eventComponent.add(new AjaxFormSubmitBehavior(form, "onclick")
		{
			private static final long serialVersionUID = 1L;

			protected void onSubmit(AjaxRequestTarget target)
			{
				executed = true;
			}
		});
	}

	/**
	 * @return Is the onSubmit executed?
	 */
	public boolean isExecuted()
	{
		return executed;
	}

	/**
	 * @return The pojo used in the form
	 */
	public Pojo getPojo()
	{
		return pojo;
	}

	/**
	 * Pojo data object
	 */
	public static class Pojo implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private String name;

		/**
		 * Construct.
		 * 
		 * @param name
		 */
		public Pojo(String name)
		{
			this.name = name;
		}

		/**
		 * @return name
		 */
		public String getName()
		{
			return name;
		}

		/**
		 * @param name
		 */
		public void setName(String name)
		{
			this.name = name;
		}
	}
}
