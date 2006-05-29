/*
 * $Id$
 * $Revision$ $Date$
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

import wicket.MarkupContainer;
import wicket.markup.html.WebPage;
import wicket.markup.html.basic.Label;
import wicket.markup.html.form.Form;
import wicket.markup.html.form.TextField;
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
		new Label(this, "label");

		Form form1 = new Form(this, "form1");
		new MyTextField(form1, "text1", "input-1");
		new MyTextField(form1, "text2", "input-2");
		new MyTextField(form1, "text7", "input-3");

		Form form2 = new TestForm(this, "form2");

		Panel panel1 = new Panel(form2, "panel1");
		new MyTextField(panel1, "text3", "input-3");
		new MyTextField(panel1, "text4", "input-4");

		Panel panel2 = new TestPanel(form2, "panel2");
		new MyTextField(panel2, "text5", "input-5");
		new MyTextField(panel2, "text6", "input-6");
		new MyTextField(panel2, "text8", "input-8");
		new MyTextField(panel2, "text9", "input-9");
		new MyTextField(panel2, "text10", "input-10");
		new MyTextField(panel2, "text11", "input-11");
		new MyTextField(panel2, "text12", "input-12");
	}

	/**
	 * 
	 * @return xxx
	 */
	public MyTextField getText1()
	{
		return (MyTextField)get("form1:text1");
	}

	/**
	 * 
	 * @return xxx
	 */
	public MyTextField getText2()
	{
		return (MyTextField)get("form1:text2");
	}

	/**
	 * 
	 * @return xxx
	 */
	public MyTextField getText3()
	{
		return (MyTextField)get("form2:panel1:text3");
	}

	/**
	 * 
	 * @return xxx
	 */
	public MyTextField getText4()
	{
		return (MyTextField)get("form2:panel1:text4");
	}

	/**
	 * 
	 * @return xxx
	 */
	public MyTextField getText5()
	{
		return (MyTextField)get("form2:panel2:text5");
	}

	/**
	 * 
	 * @return xxx
	 */
	public MyTextField getText6()
	{
		return (MyTextField)get("form2:panel2:text6");
	}

	/**
	 * 
	 * @return xxx
	 */
	public MyTextField getText7()
	{
		return (MyTextField)get("form1:text7");
	}

	/**
	 * 
	 * @return xxx
	 */
	public MyTextField getText8()
	{
		return (MyTextField)get("form2:panel2:text8");
	}

	/**
	 * 
	 * @return xxx
	 */
	public MyTextField getText9()
	{
		return (MyTextField)get("form2:panel2:text9");
	}

	/**
	 * 
	 * @return xxx
	 */
	public MyTextField getText10()
	{
		return (MyTextField)get("form2:panel2:text10");
	}

	/**
	 * 
	 * @return xxx
	 */
	public MyTextField getText11()
	{
		return (MyTextField)get("form2:panel2:text11");
	}

	/**
	 * 
	 * @return xxx
	 */
	public MyTextField getText12()
	{
		return (MyTextField)get("form2:panel2:text12");
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
		 * 
		 * @param parent
		 * @param id
		 * @param input
		 */
		public MyTextField(MarkupContainer parent, final String id, final String input)
		{
			super(parent, id);
			this.input = input;

			setRequired(true);
		}

		/**
		 * @see wicket.markup.html.form.FormComponent#getInput()
		 */
		@Override
		public String getInput()
		{
			return input;
		}

		/**
		 * @param input
		 */
		public void setInput(String input)
		{
			this.input = input;
		}
	}
}
