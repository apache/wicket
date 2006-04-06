/*
 * $Id: org.eclipse.jdt.ui.prefs 5004 2006-03-17 20:47:08 -0800 (Fri, 17 Mar
 * 2006) eelco12 $ $Revision: 5004 $ $Date: 2006-03-17 20:47:08 -0800 (Fri, 17
 * Mar 2006) $
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
package wicket.markup.html.form.validation;

import java.util.Date;

import wicket.RequestCycle;
import wicket.WicketTestCase;
import wicket.markup.html.form.Form;
import wicket.markup.html.form.TextField;
import wicket.model.CompoundPropertyModel;
import wicket.protocol.http.MockPage;

/**
 * Tests for checking typed validators.
 * 
 * @author Martijn Dashorst
 */
public class TypeValidatorTest extends WicketTestCase
{
	/**
	 * Special component for handling date properties.
	 */
	public static class DateField extends TextField
	{
		private static final long serialVersionUID = 1L;

		/**
		 * Construct.
		 * 
		 * @param id
		 */
		public DateField(String id)
		{
			super(id, Date.class);
		}
		
		/**
		 * Test method with mock input.
		 * @see wicket.markup.html.form.FormComponent#getInput()
		 */
		public String getInput()
		{
			return "01/01/2001";
		}
	}

	/**
	 * Model object for the test.
	 */
	public static class Person
	{
		private Date birthdate = new Date();

		/**
		 * Sets the birthdate.
		 * 
		 * @param date
		 */
		public void setBirthdate(Date date)
		{
			this.birthdate = date;
		}

		/**
		 * Gets the birthdate.
		 * 
		 * @return the birthdate
		 */
		public Date getBirthdate()
		{
			return this.birthdate;
		}
	}

	/**
	 * Constructor for the test.
	 * 
	 * @param name
	 */
	public TypeValidatorTest(String name)
	{
		super(name);
	}

	/**
	 * DateField test.
	 */
	public void testDateField()
	{
		RequestCycle cycle = application.createRequestCycle();

		MockPage page = new MockPage();

		Form form = new Form("form", new CompoundPropertyModel(new Person()));
		DateField dateField = new DateField("birthdate");
		form.add(dateField);
		page.add(form);

		form.onFormSubmitted();
	}
}
