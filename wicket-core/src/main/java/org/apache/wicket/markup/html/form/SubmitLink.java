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

import org.apache.wicket.IRequestListener;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnEventHeaderItem;
import org.apache.wicket.model.IModel;

/**
 * A link which can be used exactly like a Button to submit a Form. The onclick of the link will use
 * JavaScript to submit the form.
 * 
 * <p>
 * You can use this class 2 ways. First with the constructor without a Form object then this Link
 * must be inside a Form so that it knows what form to submit to. Second way is to use the Form
 * constructor then that form will be used to submit to.
 * </p>
 * <p>
 * 
 * <pre>
 * Form f = new Form(&quot;linkForm&quot;, new CompoundPropertyModel(mod));
 *     f.add(new TextField(&quot;value1&quot;));
 *     f.add(new SubmitLink(&quot;link1&quot;) {
 *         protected void onSubmit() {
 *             System.out.println(&quot;Link1 was clicked, value1 is: &quot;
 *                                 + mod.getValue1());
 *         };
 *      });
 *      add(new SubmitLink(&quot;link2&quot;,f) {
 *          protected void onSubmit() {
 *              System.out.println(&quot;Link2 was clicked, value1 is: &quot;
 *                                 + mod.getValue1());
 *           };
 *      });
 * 
 *      &lt;form wicket:id=&quot;linkForm&quot; &gt;
 *          &lt;input wicket:id=&quot;value1&quot; type=&quot;text&quot; size=&quot;30&quot;/&gt;
 *          &lt;a wicket:id=&quot;link1&quot;&gt;Press link1 to submit&lt;/a&gt;
 *          &lt;input type=&quot;submit&quot; value=&quot;Send&quot;/&gt;
 *      &lt;/form&gt;
 *      &lt;a wicket:id=&quot;link2&quot;&gt;Press link 2 to submit&lt;/a&gt;
 * </pre>
 * 
 * </p>
 * <p>
 * If this link is not placed in a form or given a form to cooperate with, it will fall back to a
 * normal link behavior, meaning that {@link #onSubmit()} will be called without any other
 * consequences.
 * </p>
 * <p>
 * To customize the JavaScript code used to submit the form we must override {@link #getTriggerJavaScript()}. 
 * This can be helpful to implement additional client side behaviors like disabling the link during form submission.
 * </p>
 * 
 * @author chris
 * @author jcompagner
 * @author Igor Vaynberg (ivaynberg)
 * @author Eelco Hillenius
 * 
 */
public class SubmitLink extends AbstractSubmitLink
{
	private static final long serialVersionUID = 1L;

	/**
	 * With this constructor the SubmitLink must be inside a Form.
	 * 
	 * @param id
	 *            The id of the submitlink.
	 */
	public SubmitLink(String id)
	{
		super(id);
	}

	/**
	 * With this constructor the SubmitLink will submit the {@link Form} that is given when the link
	 * is clicked on.
	 * 
	 * The SubmitLink doesn't have to be inside the {@link Form}. But currently if it is outside the
	 * {@link Form} and the SubmitLink is rendered first, then the {@link Form} will have a
	 * generated javascript/css id. The markup javascript/css id that can exist will be overridden.
	 * 
	 * @param id
	 *            The id of the submitlink.
	 * @param form
	 *            The form which this submitlink must submit.
	 */
	public SubmitLink(String id, Form<?> form)
	{
		super(id, form);
	}


	/**
	 * With this constructor the SubmitLink must be inside a Form.
	 * 
	 * @param id
	 *            The id of the submitlink.
	 * @param model
	 *            The model for this submitlink, It won't be used by the submit link itself, but it
	 *            can be used for keeping state
	 */
	public SubmitLink(String id, IModel<?> model)
	{
		super(id, model);
	}

	/**
	 * With this constructor the SubmitLink will submit the {@link Form} that is given when the link
	 * is clicked on.
	 * 
	 * The SubmitLink doesn't have to be in inside the {@link Form}. But currently if it is outside
	 * the {@link Form} and the SubmitLink will be rendered first. Then the {@link Form} will have a
	 * generated javascript/css id. The markup javascript/css id that can exist will be overridden.
	 * 
	 * @param id
	 *            The id of the submitlink.
	 * @param model
	 *            The model for this submitlink, It won't be used by the submit link itself, but it
	 *            can be used for keeping state
	 * @param form
	 *            The form which this submitlink must submit.
	 */
	public SubmitLink(String id, IModel<?> model, Form<?> form)
	{
		super(id, model, form);
	}

	/**
	 * This method is here as a means to fall back on normal link behavior when this link is not
	 * nested in a form. Not intended to be called by clients directly.
	 * 
	 * @see IRequestListener#onRequest()
	 */
	public final void onLinkClicked()
	{
		onSubmit();
		onAfterSubmit();
	}

	@Override
	protected void onComponentTag(ComponentTag tag)
	{
		super.onComponentTag(tag);

		if (isEnabledInHierarchy())
		{
			if (tag.getName().equalsIgnoreCase("a") || tag.getName().equalsIgnoreCase("link")
				|| tag.getName().equalsIgnoreCase("area"))
			{
				tag.put("href", "#");
			}
			else if (tag.getName().equalsIgnoreCase("button"))
			{
				// WICKET-5597 prevent default submit
				tag.put("type", "button");
			}
		}
		else
		{
			disableLink(tag);
		}
	}
	
	@Override
	public void renderHead(IHeaderResponse response)
	{
		super.renderHead(response);

		if (isEnabledInHierarchy())
		{
			response.render(OnEventHeaderItem.forComponent(this, "click", getTriggerJavaScript()));
		}
	}

	/**
	 * Controls whether or not clicking on this link will trigger a javascript submit event, firing
	 * any submit handler added to the form. True by default.
	 * 
	 * @return true if form's javascript submit handlers should be invoked, false otherwise
	 */
	protected boolean shouldTriggerJavaScriptSubmitEvent()
	{
		return true;
	}

	/**
	 * The JavaScript which triggers this link. Method is non-final so that subclasses can decorate
	 * the provided script by wrapping their own JS around a call to super.getTriggerJavaScript().
	 * 
	 * @return The JavaScript to be executed when the link is clicked.
	 */
	protected CharSequence getTriggerJavaScript()
	{
		if (getForm() != null)
		{
			// find the root form - the one we are really going to submit
			Form<?> root = getForm().getRootForm();

			StringBuilder script = new StringBuilder();
			script.append(root.getJsForSubmitter(this, shouldTriggerJavaScriptSubmitEvent()));
			script.append("return false;");
			
			return script;
		}
		else
		{
			return null;
		}
	}

	@Override
	public void onError()
	{
	}

	/**
	 * Override this method to provide special submit handling in a multi-button form. This method
	 * will be called <em>after</em> the form's onSubmit method.
	 */
	@Override
	public void onAfterSubmit()
	{
	}

	/**
	 * Override this method to provide special submit handling in a multi-button form. This method
	 * will be called <em>before</em> the form's onSubmit method.
	 */
	@Override
	public void onSubmit()
	{
	}
}
