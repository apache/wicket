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
package org.apache.wicket.markup.html.form;

import java.util.ArrayList;

import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.model.IModel;

/**
 * Creates an output field for a form
 * 
 * @author Tobias Soloschenko
 *
 */
public class OutputField<T> extends FormComponent<T> {
    private static final long serialVersionUID = 1L;

    private Form<?> form;

    private FormComponent<?>[] dependentFormComponents;

    private OutputDefaultAjaxBehavior outputDefaultAjaxBehavior;

    private String inputScript;

    private class OutputDefaultAjaxBehavior extends AbstractDefaultAjaxBehavior {

	private static final long serialVersionUID = 1L;

	/**
	 * Applies the output value to the dynamic extra parameters
	 */
	@Override
	protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
	    super.updateAjaxAttributes(attributes);
	    attributes.getDynamicExtraParameters().add("return { value : " + getMarkupId() + "_value() }");
	}

	/**
	 * Updates the model value
	 */
	@Override
	protected void respond(AjaxRequestTarget target) {
	    String modelObject = getRequest().getRequestParameters().getParameterValue("value").toString();
	    target.add(getComponent().setDefaultModelObject(modelObject));
	    updated(target);
	}
    }

    /**
     * Creates an output field for the given ids with the given model
     * 
     * @param id
     *            the id of the output field
     * @param model
     *            the model of the output field
     * @param forIds
     *            the ids of the fields used in for attribute
     */
    public OutputField(String id, IModel<T> model) {
	super(id, model);
	add(outputDefaultAjaxBehavior = new OutputDefaultAjaxBehavior());
	setOutputMarkupId(true);

    }

    /**
     * Replace the content with the default model object
     */
    @Override
    public void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag) {
	replaceComponentTagBody(markupStream, openTag, getDefaultModelObjectAsString());
    }

    @Override
    protected void onComponentTag(ComponentTag tag) {
	super.onComponentTag(tag);

	// Must be attached to an output tag
	checkComponentTag(tag, "output");

	// Check if the required components have been set
	checkDependentFormComponents(dependentFormComponents);
	if (form == null) {
	    throw new WicketRuntimeException("Please provide a form to the output field");
	}

	tag.put("name", getInputName());

	String ids = "";
	for (FormComponent<?> dependendFormComponent : this.dependentFormComponents) {
	    ids += dependendFormComponent.getMarkupId() + " ";
	}
	int lastIndexOf = ids.lastIndexOf(" ");
	if (lastIndexOf != -1) {
	    ids = ids.substring(0, lastIndexOf);
	}

	tag.put("for", ids);

	if (form != null) {
	    tag.put("form", form.getMarkupId());
	}
    }

    /**
     * Gets the form the output field belongs to
     * 
     * @return the form
     */
    public Form<?> getForm() {
	return form;
    }

    /**
     * Sets the form the output field belongs to
     * 
     * @param form
     *            the form
     */
    public void setForm(Form<?> form) {
	if (form != null) {
	    form.setOutputMarkupId(true);
	    form.setOutputMarkupPlaceholderTag(true);
	    this.form = form;
	}
    }

    /**
     * Gets a list of dependentFormComponents the output tag belongs to
     * 
     * @return a list of dependentFormComponents the output tag belogs to
     */
    public FormComponent<?>[] getDependentFormComponents() {
	return dependentFormComponents;
    }

    /**
     * Sets a list of dependentFormComponents the output tag belongs to
     * 
     * @param dependentFormComponents
     *            a list of dependentFormComponents the output tags belongs to
     */
    public void setDependentFormComponents(FormComponent<?>... dependentFormComponents) {

	checkDependentFormComponents(dependentFormComponents);

	for (FormComponent<?> dependentFormComponent : dependentFormComponents) {
	    dependentFormComponent.setOutputMarkupId(true);
	    dependentFormComponent.setOutputMarkupPlaceholderTag(true);
	}
	this.dependentFormComponents = dependentFormComponents;
    }

    /**
     * Gets the input script of the output field
     * 
     * @return the input script of the output field
     */
    public String getInputScript() {

	checkDependentFormComponents(dependentFormComponents);

	ArrayList<String> markupIds = new ArrayList<String>();
	markupIds.add(this.getMarkupId());
	for (FormComponent<?> dependentFormComponent : dependentFormComponents) {
	    markupIds.add(dependentFormComponent.getMarkupId());
	}

	return String.format(inputScript, markupIds.toArray());
    }

    /**
     * Sets the input script of the output field
     * 
     * @param inputScript
     *            the input script of the output field
     */
    public void setInputScript(String inputScript) {
	// Assign the calculated value of the input script to the output field
	this.inputScript = "%s.value=" + inputScript;
    }

    @Override
    public void renderHead(IHeaderResponse response) {
	super.renderHead(response);

	String element = "document.getElementById('" + getMarkupId() + "')";

	response.render(OnDomReadyHeaderItem.forScript("window." + getMarkupId() + "_value = function(){ return "
		+ element + ".value;}"));

	response.render(OnDomReadyHeaderItem.forScript(String.format("$('#%s').on('change', function() { %s });",
		getForm().getMarkupId(), outputDefaultAjaxBehavior.getCallbackScript())));

	response.render(OnDomReadyHeaderItem.forScript(String.format("$('#%s').on('input', function() { %s });",
		getForm().getMarkupId(), getInputScript())));
    }

    /**
     * Checks if required dependent form components has been set
     * 
     * @param dependentFormComponents
     *            the form components to be checked
     */
    private void checkDependentFormComponents(FormComponent<?>... dependentFormComponents) {
	if (dependentFormComponents == null || dependentFormComponents.length == 0) {
	    throw new WicketRuntimeException("Please apply a not empty list of dependent form components!");
	}
    }

    /**
     * This method can be overridden and is invoked when ever the output field
     * has been updated
     * 
     * @param target
     *            the ajax request target if the output field has been updated
     */
    protected void updated(AjaxRequestTarget target) {
    }
}
