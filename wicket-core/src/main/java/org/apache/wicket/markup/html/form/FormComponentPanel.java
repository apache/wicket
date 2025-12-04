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

import static java.lang.Boolean.TRUE;

import org.apache.wicket.IQueueRegion;
import org.apache.wicket.MetaDataKey;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.panel.IMarkupSourcingStrategy;
import org.apache.wicket.markup.html.panel.PanelMarkupSourcingStrategy;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.visit.IVisitor;

/**
 * Panel (has it's own markup, defined between &lt;wicket:panel&gt; tags), that can act as a form
 * component. It typically wouldn't receive any input yourself, and often you can get by with
 * nesting form components in panels proper. However, using this panel can help you with building
 * components act to the outside world as one component, but internally uses separate components.
 * This component would then use these nested components to handle it's internal state, and would
 * use that internal state to get to one model object.
 * <p>
 * It is recommended that you override {@link #convertInput()} and let it set the value that
 * represents the compound value of the nested components. Often, this goes hand-in-hand with
 * overriding {@link #onBeforeRender()}, where you would analyze the model value, break it up and
 * distribute the appropriate values over the child components.
 * </p>
 * 
 * <p>
 * Here is a simple example of a panel with two components that multiplies and sets that as the
 * master model object. Note that for this simple example, setting the model value wouldn't make
 * sense, as the lhs and rhs cannot be known.
 * </p>
 * 
 * <pre>
 * public class Multiply extends FormComponentPanel
 * {
 * 	private TextField left;
 * 	private int lhs = 0;
 * 	private int rhs = 0;
 * 	private TextField right;
 * 
 * 	public Multiply(String id)
 * 	{
 * 		super(id);
 * 		init();
 * 	}
 * 
 * 	public Multiply(String id, IModel model)
 * 	{
 * 		super(id, model);
 * 		init();
 * 	}
 * 
 * 	protected void convertInput()
 * 	{
 * 		Integer lhs = (Integer)left.getConvertedInput();
 * 		Integer rhs = (Integer)right.getConvertedInput();
 * 		setConvertedInput(lhs * rhs);
 * 	}
 * 
 * 	private void init()
 * 	{
 * 		add(left = new TextField(&quot;left&quot;, new PropertyModel(this, &quot;lhs&quot;), Integer.class));
 * 		add(right = new TextField(&quot;right&quot;, new PropertyModel(this, &quot;rhs&quot;), Integer.class));
 * 		left.setRequired(true);
 * 		right.setRequired(true);
 * 	}
 * }
 * </pre>
 * 
 * With this markup:
 * 
 * <pre>
 *   &lt;wicket:panel&gt;
 *     &lt;input type=&quot;text&quot; wicket:id=&quot;left&quot; size=&quot;2&quot; /&gt; * &lt;input type=&quot;text&quot; wicket:id=&quot;right&quot; size=&quot;2&quot; /&gt;
 *   &lt;/wicket:panel&gt;
 * </pre>
 * 
 * Which could be used, for example as:
 * 
 * <pre>
 *   add(new Multiply(&quot;multiply&quot;), new PropertyModel(m, &quot;multiply&quot;)));
 *   add(new Label(&quot;multiplyLabel&quot;, new PropertyModel(m, &quot;multiply&quot;)));
 * </pre>
 * 
 * and:
 * 
 * <pre>
 *   &lt;span wicket:id=&quot;multiply&quot;&gt;[multiply]&lt;/span&gt;
 *   = &lt;span wicket:id=&quot;multiplyLabel&quot;&gt;[result]&lt;/span&gt;
 * </pre>
 * 
 * </p>
 * 
 * @author eelcohillenius
 * 
 * @param <T>
 *            The model object type
 */
public abstract class FormComponentPanel<T> extends FormComponent<T> implements IQueueRegion
{
	/**
	 * By setting this key to <code>true</code> (and implementing {@link #processInputOfChildren()}) it will be possible
	 * to add a {@link org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior AjaxFormComponentUpdatingBehavior}
	 * to this panel, and be able to access the updated model object of this panel in that behavior.
	 * <p>
	 * The panel must override {@link #processInputOfChildren()} and (should) call
	 * {@link #processInputOfChild(FormComponent)} for each of its sub form components.
	 * <p>
	 * <code>AjaxFormComponentUpdatingBehavior</code> works for <code>FormComponentPanel</code>s that contain
	 * {@link CheckBoxMultipleChoice}, {@link CheckGroup}, {@link RadioChoice} and/or {@link RadioGroup} fields. There
	 * is no need to use
	 * {@link org.apache.wicket.ajax.form.AjaxFormChoiceComponentUpdatingBehavior AjaxFormChoiceComponentUpdatingBehavior}.
	 * <p>
	 * The <code>AjaxFormComponentUpdatingBehavior</code> should use <code>"input change"</code> for the events in most
	 * cases, so changes to all descendent form components will result in an Ajax update. <strong>Warning</strong>: some
	 * form components will result in 2 events being emitted. For example, <code>&lt;input type="number"&gt;</code>.
	 * <p>
	 * <strong>Warning</strong>: some components may send excessive Ajax updates. Make sure the extra updates are not an
	 * issue for your situation, or take steps to prevent them.
	 * <p>
	 * Note that the values of all form components of the panel will be submitted on each event, so use with panels with
	 * possibly large values should probably be avoided.
	 */
	public static final MetaDataKey<Boolean> WANT_CHILDREN_TO_PROCESS_INPUT_IN_AJAX_UPDATE = new MetaDataKey<>()
	{
		private static final long serialVersionUID = 1L;
	};

	private static final long serialVersionUID = 1L;

	/**
	 * Constructor.
	 * 
	 * @param id
	 *          The component id
	 */
	public FormComponentPanel(String id)
	{
		super(id);
	}

	/**
	 * Constructor.
	 * 
	 * @param id
	 *          The component id
	 * @param model
	 *          The component model
	 */
	public FormComponentPanel(String id, IModel<T> model)
	{
		super(id, model);
	}

	@Override
	public boolean checkRequired()
	{
		return true;
	}

	@Override
	protected IMarkupSourcingStrategy newMarkupSourcingStrategy()
	{
		return PanelMarkupSourcingStrategy.get(false);
	}

	@Override
	protected void onComponentTag(final ComponentTag tag)
	{
		super.onComponentTag(tag);

		// remove unapplicable attributes that might have been set by the call to super
		tag.remove("name");
		tag.remove("disabled");
	}

	@Override
	public void clearInput()
	{
		super.clearInput();

		// Visit all the (visible) form components and clear the input on each.
		visitFormComponentsPostOrder(this, (IVisitor<FormComponent<?>, Void>) (formComponent, visit) ->
		{
			if (formComponent != FormComponentPanel.this && formComponent.isVisibleInHierarchy())
			{
				formComponent.clearInput();
			}
		});
	}

	/**
	 * Called by {@link org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior AjaxFormComponentUpdatingBehavior}
	 * if {@link #WANT_CHILDREN_TO_PROCESS_INPUT_IN_AJAX_UPDATE} is set to <code>true</code>. Each nested form component
	 * must be asked to process its input. You should use {@link #processInputOfChild(FormComponent)} as this method
	 * takes child <code>FormComponentPanel</code>s that also want their children to process the input into account.
	 */
	public void processInputOfChildren()
	{
	}

	/**
	 * Tell the given child component to process its input. If the child component is a <code>FormComponentPanel</code>
	 * that wants its children to process their input, it will be told to do so.
	 *
	 * @param child the component that must be told to process its children.
	 */
	protected final void processInputOfChild(FormComponent<?> child)
	{
		if (child instanceof FormComponentPanel<?> formComponentPanel
				&& formComponentPanel.getMetaData(WANT_CHILDREN_TO_PROCESS_INPUT_IN_AJAX_UPDATE) == TRUE)
		{
			formComponentPanel.processInputOfChildren();
		}
		child.processInput();
	}
}
