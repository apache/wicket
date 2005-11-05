/*
 * $Id$ $Revision:
 * 1.51 $ $Date$
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
package wicket.properties;

import wicket.markup.html.WebPage;
import wicket.markup.html.basic.Label;
import wicket.markup.html.form.Form;
import wicket.markup.html.form.TextField;
import wicket.markup.html.form.validation.RequiredValidator;
import wicket.markup.html.panel.Panel;

/**
 * 
 * @author Juergen Donnerstag
 */
public class TestPage extends WebPage
{
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 * Construct.
	 */
	public TestPage()
	{
		add(new Label("label"));
		
		Form form1 = new Form("form1");
		add(form1);
		form1.add(new MyTextField("text1", "input-1"));
		form1.add(new MyTextField("text2", "input-2"));
		
		Form form2 = new TestForm("form2");
		add(form2);
		
		Panel panel1 = new Panel("panel1");
		form2.add(panel1);
		panel1.add(new MyTextField("text3", "input-3"));
		panel1.add(new MyTextField("text4", "input-4"));

		Panel panel2 = new TestPanel("panel2");
		form2.add(panel2);
		panel2.add(new MyTextField("text5", "input-5"));
		panel2.add(new MyTextField("text6", "input-6"));
	}
	
	/**
	 * 
	 * @return xxx
	 */
	public TextField getText1()
	{
		return (TextField) get("form1:text1");
	}
	
	/**
	 * 
	 * @return xxx
	 */
	public TextField getText2()
	{
		return (TextField) get("form1:text2");
	}
	
	/**
	 * 
	 * @return xxx
	 */
	public TextField getText3()
	{
		return (TextField) get("form2:panel1:text3");
	}
	
	/**
	 * 
	 * @return xxx
	 */
	public TextField getText4()
	{
		return (TextField) get("form2:panel1:text4");
	}
	
	/**
	 * 
	 * @return xxx
	 */
	public TextField getText5()
	{
		return (TextField) get("form2:panel2:text5");
	}
	
	/**
	 * 
	 * @return xxx
	 */
	public TextField getText6()
	{
		return (TextField) get("form2:panel2:text6");
	}
	
	/**
	 * 
	 */
	public static class MyTextField extends TextField
	{
		private static final long serialVersionUID = 1L;

		private String input;

		/**
		 * Construct.
		 * @param id
		 * @param input
		 */
		public MyTextField(final String id, final String input)
		{
			super(id);
			this.input = input;
			
			add(RequiredValidator.getInstance());
		}
		
		/**
		 * @see wicket.markup.html.form.FormComponent#getInput()
		 */
		public String getInput()
		{
			return input;
		}
	}
}
