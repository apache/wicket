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

import java.util.Iterator;
import java.util.List;

import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.INamedParameters;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;

/**
 * This StatelessForm is the same as a normal form but with the statelesshint default to true. The
 * form can be newly constructed when the onSubmit of its form or its buttons is called. So you
 * can't depend on state within the page. The only state you can depend on is what was submitted
 * from the browser. So the model of the form or the formcomponents are updated with the submit
 * values.
 * 
 * @author jcompagner
 * @param <T>
 *            The type of the {@link Form}'s model object
 */
public class StatelessForm<T> extends Form<T>
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 * 
	 * @param id
	 */
	public StatelessForm(String id)
	{
		super(id);
	}

	/**
	 * Construct.
	 * 
	 * @param id
	 * @param model
	 */
	public StatelessForm(String id, IModel<T> model)
	{
		super(id, model);
	}

	@Override
	protected boolean getStatelessHint()
	{
		return true;
	}

	@Override
	protected MethodMismatchResponse onMethodMismatch()
	{
		setResponsePage(getPage().getClass(), getPage().getPageParameters());
		return MethodMismatchResponse.ABORT;
	}

	@Override
	protected CharSequence getActionUrl()
	{
		return urlFor(IFormSubmitListener.INTERFACE, getPage().getPageParameters());
	}

	/**
	 * Remove the page parameters for all form component otherwise they get appended to action URL
	 *
	 * {@inheritDoc}
	 */
	@Override
	public void process(IFormSubmitter submittingComponent)
	{
		// get the parameters before processing the form because the
		// application may remove this form from its parent in #onSubmit() (WICKET-5158)
		final PageParameters parameters = getPage().getPageParameters();

		super.process(submittingComponent);

		if (parameters != null)
		{
			visitFormComponents(new IVisitor<FormComponent<?>, Void>()
			{
				@Override
				public void component(final FormComponent<?> formComponent, final IVisit<Void> visit)
				{
					parameters.remove(formComponent.getInputName());
				}
			});
			parameters.remove(getHiddenFieldId());
			if (submittingComponent instanceof AbstractSubmitLink)
			{
				AbstractSubmitLink submitLink = (AbstractSubmitLink)submittingComponent;
				parameters.remove(submitLink.getInputName());
			}

			// remove the special parameter for IRequestListener
			List<INamedParameters.NamedPair> namedParameters = parameters.getAllNamed();
			Iterator<INamedParameters.NamedPair> iterator = namedParameters.iterator();
			while (iterator.hasNext())
			{
				INamedParameters.NamedPair namedParameter = iterator.next();
				if (Strings.isEmpty(namedParameter.getValue()))
				{
					parameters.remove(namedParameter.getKey());
					break;
				}
			}
		}
	}
}
