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
package org.apache.wicket.examples.extendedform;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.examples.WicketExamplePage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.OutputField;
import org.apache.wicket.markup.html.form.RangeTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;

/**
 * Demonstrates different flavors of org.apache.wicket.examples.extendedform.<br>
 * <br>
 *
 * @author Tobias Soloschenko
 */
public final class Home extends WicketExamplePage {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor
     */
    public Home() {
	// Demonstrates the basics of the output tag
	// Note that you have to setOutputMarkupIds(true)
	Form<Void> form1 = new Form<Void>("form1");

	TextField<String> number1 = new TextField<String>("number1", Model.of("0"));
	form1.add(number1);

	TextField<String> number2 = new TextField<String>("number2", Model.of("0"));
	form1.add(number2);

	// Create an output field to show the results
	OutputField<Integer> outputField1 = new OutputField<Integer>("output1", Model.of(0));
	outputField1.setDependentFormComponents(number1, number2);
	outputField1.setForm(form1);
	outputField1.setInputScript("parseFloat(%s.value) + parseFloat(%s.value)");
	form1.add(outputField1);

	add(form1);

	// A range slider with a direct output and a default value
	Form<Void> form2 = new Form<Void>("form2");

	// Create a range slider
	RangeTextField<Integer> range = new RangeTextField<Integer>("range", Model.of(0));
	range.setMaximum(100);
	form2.add(range);

	final OutputField<Integer> outputField2 = new OutputField<Integer>("output2", Model.of(0));
	outputField2.setDependentFormComponents(range);
	outputField2.setForm(form2);
	outputField2.setInputScript("%s.valueAsNumber");
	form2.add(outputField2);

	final Label label = new Label("value", "Please submit.");
	label.setOutputMarkupId(true);

	form2.add(new AjaxButton("submit") {

	    private static final long serialVersionUID = 1L;

	    @Override
	    protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
		target.add(label.setDefaultModelObject("The current output value: "
			+ outputField2.getDefaultModelObject()));
	    }
	}.setDefaultFormProcessing(false));
	add(form2);

	// Adding the label
	add(label);
    }
}
