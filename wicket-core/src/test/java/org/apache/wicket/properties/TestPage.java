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
package org.apache.wicket.properties;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;

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

		Form<Void> form1 = new Form<Void>("form1");
		add(form1);
		form1.add(new MyTextField("text1", "input-1"));
		form1.add(new MyTextField("text2", "input-2"));
		form1.add(new MyTextField("text7", "input-3"));

		Form<Void> form2 = new TestForm("form2");
		add(form2);

		Panel panel1 = new EmptyPanel("panel1");
		form2.add(panel1);
		panel1.add(new MyTextField("text3", "input-3"));
		panel1.add(new MyTextField("text4", "input-4"));

		Panel panel2 = new TestPanel("panel2");
		form2.add(panel2);
		panel2.add(new MyTextField("text5", "input-5"));
		panel2.add(new MyTextField("text6", "input-6"));
		panel2.add(new MyTextField("text8", "input-8"));
		panel2.add(new MyTextField("text9", "input-9"));
		panel2.add(new MyTextField("text10", "input-10"));
		panel2.add(new MyTextField("text11", "input-11"));
		panel2.add(new MyTextField("text12", "input-12"));

		Form<Void> form3 = new TestForm("form3")
		{
			private static final long serialVersionUID = 1L;

			/**
			 * @see org.apache.wicket.markup.html.form.Form#getValidatorKeyPrefix()
			 */
			@Override
			public String getValidatorKeyPrefix()
			{
				return "myValidator";
			}
		};

		add(form3);
		form3.add(new MyTextField("text13", "input-13"));
		form3.add(new MyTextField("text14", "input-14"));
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
	 * @return xxx
	 */
	public MyTextField getText13()
	{
		return (MyTextField)get("form3:text13");
	}

	/**
	 * 
	 * @return xxx
	 */
	public MyTextField getText14()
	{
		return (MyTextField)get("form3:text14");
	}

	/**
	 * 
	 */
	public static class MyTextField extends TextField<String>
	{
		private static final long serialVersionUID = 1L;

		private String input;

		/**
		 * Construct.
		 * 
		 * @param id
		 * @param input
		 */
        MyTextField(final String id, final String input)
		{
			super(id);
			this.input = input;

			setRequired(true);
		}

		/**
		 * @see org.apache.wicket.markup.html.form.FormComponent#getInput()
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
