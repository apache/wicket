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
package org.apache.wicket.examples.compref;

import org.apache.wicket.util.io.IClusterable;
import org.apache.wicket.examples.WicketExamplePage;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;


/**
 * Page with examples on {@link org.apache.wicket.markup.html.form.TextField}.
 * 
 * @author Eelco Hillenius
 */
public class TextFieldPage extends WicketExamplePage
{
	/**
	 * Constructor
	 */
	public TextFieldPage()
	{
		final Input input = new Input();

		// we use the magical compound property model here as our 'super' model.
		// when components do not have an explicit model, but on of their
		// parents
		// has the compound property model set, it will use that parent's model
		// as the taget for getting and setting the property which is based on
		// its own component id. Thus, component with id 'text' will act as if
		// its
		// model is property 'text' of the compound property model. And as the
		// compound
		// property model's actual object is an instance of 'Input', it will map
		// to
		// Input's 'text' property.
		setDefaultModel(new CompoundPropertyModel<>(input));

		// Add a FeedbackPanel for displaying our messages
		FeedbackPanel feedbackPanel = new FeedbackPanel("feedback");
		add(feedbackPanel);

		// Add a form with an onSumbit implementation that sets a message
		Form<?> form = new Form("form")
		{
			@Override
			protected void onSubmit()
			{
				info("input: " + input);
			}
		};
		add(form);

		// add a simple text field that uses Input's 'text' property. Nothing
		// can go wrong here
		form.add(new TextField<>("text"));

		// here we add a text field that uses Input's 'integer' property.
		// Something could go
		// wrong here, as the user's (textual) input might be an invalid value
		// for an
		// Integer object. If we provide the class constructor argument like we
		// do here, we
		// get two things:
		// 1. A type validator is added, so that before any actual updating is
		// tried, first the
		// user input is checked for validity. When the user input is wrong for
		// an integer,
		// the model updating is cancelled, and an error message is displayed to
		// the user
		// 2.When updating the model, the given type is explicitly used instead
		// of trying
		// to figure out what type should be converted to.
		// Note that the default validation message mechanism uses resource
		// bundles for the actual
		// message lookup. The message for this component can be found in
		// TextFieldPage.properties
		// with key 'form.integer.IConverter'. Read more about how this works
		// in the javadocs
		// of AbstractValidator
		form.add(new TextField<>("integer", Integer.class));
	}

	/**
	 * Simple data class that acts as a holder for the data for the input fields.
	 */
	private static class Input implements IClusterable
	{
		// Normally we would have played nice and made it a proper JavaBean with
		// getters and
		// setters for its properties. But this is an example which we like to
		// keep small.

		/** some plain text. */
		public String text = "some text";

		/** an integer. */
		public Integer integer = 12;

		/**
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString()
		{
			return "text = '" + text + "', integer = '" + integer + "'";
		}
	}

	/**
	 * Override base method to provide an explanation
	 */
	@Override
	protected void explain()
	{
		String html = "<input type=\"text\" wicket:id=\"text\" />\n"
			+ "<input type=\"text\" wicket:id=\"integer\" />";
		String code = "&nbsp;&nbsp;&nbsp;&nbsp;// add a simple text field that uses Input's 'text' property. Nothing can go wrong here\n"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;form.add(new TextField(\"text\"));\n"
			+ "\n"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;// here we add a text field that uses Input's 'integer' property. Something could go\n"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;// wrong here, as the user's (textual) input might be an invalid value for an\n"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;// Integer object. If we provide the class constructor argument like we do here, we\n"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;// get two things:\n"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;// 1. A type validator is added, so that before any actual updating is tried, first the\n"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;// &nbsp;&nbsp;&nbsp;&nbsp;user input is checked for validity. When the user input is wrong for an integer,\n"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;//&nbsp;&nbsp;&nbsp;&nbsp;the model updating is cancelled, and an error message is displayed to the user\n"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;// 2.When updating the model, the given type is explicitly used instead of trying\n"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;//&nbsp;&nbsp;&nbsp;&nbsp;to figure out what type should be converted to.\n"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;// Note that the default validation message mechanism uses resource bundles for the actual\n"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;// message lookup. The message for this component can be found in TextFieldPage.properties\n"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;// with key 'form.integer.IConverter'. Read more about how this works in the javadocs\n"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;// of AbstractValidator\n"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;form.add(new TextField(\"integer\", Integer.class));";
		add(new ExplainPanel(html, code));

	}

}