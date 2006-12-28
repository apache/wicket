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
package wicket.markup.html.basic;

import wicket.MarkupContainer;
import wicket.PageParameters;
import wicket.markup.html.WebPage;
import wicket.markup.html.form.Form;
import wicket.markup.html.form.StatelessForm;
import wicket.markup.html.form.TextField;
import wicket.model.Model;

/**
 * Mock page for testing.
 */
public class SimplePage_15 extends WebPage
{
	private static final long serialVersionUID = 1L;

	private TextField suchTextField;

	/**
	 * Construct.
	 */
	public SimplePage_15()
	{
		this(new PageParameters());
	}

	/** 
	 * Creates a new instance of TestClass
	 * 
	 * @param paramsIn
	 */
	public SimplePage_15(PageParameters paramsIn)
	{
		super(paramsIn);
		
		// print out the result
		new Label(this, "value", paramsIn.getString(Integer.toString(paramsIn.size() - 1), ""));
		
		// create the form
		Form form = new SuchForm(this, "suchForm");
		suchTextField = new TextField<String>(form, "suchFeld", new Model<String>(""));
	}

	protected String getSuchfeld()
	{
		return suchTextField.getModelObjectAsString();
	}

	/**
	 * 
	 */
	public class SuchForm extends StatelessForm
	{
		private static final long serialVersionUID = 1L;

		/**
		 * Construct.
		 * 
		 * @param parent
		 * @param id
		 */
		public SuchForm(MarkupContainer parent, String id)
		{
			super(parent, id);
		}

		@SuppressWarnings("unchecked")
		@Override
		public void onSubmit()
		{
			String suchFeld = getSuchfeld();
			if (suchFeld.length() > 0)
			{
				PageParameters params = new PageParameters();
				params.add(Integer.toString(params.size()), suchFeld);
				setResponsePage(SimplePage_15.class, params);
			}
		}
	}
}
