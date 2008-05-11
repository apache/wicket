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
package org.apache.wicket.util.tester;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.Component.IVisitor;
import org.apache.wicket.markup.html.form.AbstractTextComponent;
import org.apache.wicket.markup.html.form.Check;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.CheckGroup;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.IFormSubmittingComponent;
import org.apache.wicket.markup.html.form.ListMultipleChoice;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioChoice;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.protocol.http.MockHttpServletRequest;
import org.apache.wicket.protocol.http.WebRequestCycle;
import org.apache.wicket.util.file.File;
import org.apache.wicket.util.string.Strings;


/**
 * A helper class for testing validation and submission of <code>FormComponent</code>s.
 * 
 * @author Ingram Chen
 * @author Frank Bille (frankbille)
 * @since 1.2.6
 */
public class FormTester
{
	/**
	 * A selector template for selecting selectable <code>FormComponent</code>s with an index of
	 * option -- supports <code>RadioGroup</code>, <code>CheckGroup</code>, and
	 * <code>AbstractChoice</code> family.
	 */
	protected abstract class ChoiceSelector
	{
		/**
		 * TODO need Javadoc from author.
		 */
		private final class SearchOptionByIndexVisitor implements IVisitor<Component<?>>
		{
			int count = 0;

			private final int index;

			private SearchOptionByIndexVisitor(int index)
			{
				super();
				this.index = index;
			}

			/**
			 * @see org.apache.wicket.Component.IVisitor#component(org.apache.wicket.Component)
			 */
			public Object component(Component<?> component)
			{
				if (count == index)
				{
					return component;
				}
				else
				{
					count++;
					return CONTINUE_TRAVERSAL;
				}
			}
		}

		private final FormComponent<?> formComponent;

		/**
		 * Constructor.
		 * 
		 * @param formComponent
		 *            a <code>FormComponent</code>
		 */
		protected ChoiceSelector(FormComponent<?> formComponent)
		{
			this.formComponent = formComponent;
		}

		/**
		 * Implements whether toggle or accumulate the selection.
		 * 
		 * @param formComponent
		 *            a <code>FormComponent</code>
		 * @param value
		 *            a <code>String</code> value
		 */
		protected abstract void assignValueToFormComponent(FormComponent<?> formComponent,
			String value);

		/**
		 * Selects a given index in a selectable <code>FormComponent</code>.
		 * 
		 * @param index
		 */
		protected final void doSelect(final int index)
		{
			if (formComponent instanceof RadioGroup)
			{
				Radio<?> foundRadio = (Radio<?>)formComponent.visitChildren(Radio.class,
					new SearchOptionByIndexVisitor(index));
				if (foundRadio == null)
				{
					fail("RadioGroup " + formComponent.getPath() + " does not have index:" + index);
				}
				assignValueToFormComponent(formComponent, foundRadio.getValue());
			}
			else if (formComponent instanceof CheckGroup)
			{
				Check<?> foundCheck = (Check<?>)formComponent.visitChildren(Check.class,
					new SearchOptionByIndexVisitor(index));
				if (foundCheck == null)
				{
					fail("CheckGroup " + formComponent.getPath() + " does not have index:" + index);
				}

				assignValueToFormComponent(formComponent, foundCheck.getValue());
			}
			else
			{
				String idValue = selectAbstractChoice(formComponent, index);
				if (idValue == null)
				{
					fail(formComponent.getPath() + " is not a selectable Component.");
				}
				else
				{
					assignValueToFormComponent(formComponent, idValue);
				}
			}
		}

		/**
		 * Selects a given index in a selectable <code>FormComponent</code>.
		 * 
		 * @param formComponent
		 *            a <code>FormComponent</code>
		 * @param index
		 *            the index to select
		 * @return the id value at the selected index
		 */
		@SuppressWarnings("unchecked")
		private String selectAbstractChoice(FormComponent<?> formComponent, final int index)
		{
			try
			{
				Method getChoicesMethod = formComponent.getClass().getMethod("getChoices",
					(Class[])null);
				getChoicesMethod.setAccessible(true);
				List<Object> choices = (List<Object>)getChoicesMethod.invoke(formComponent,
					(Object[])null);

				Method getChoiceRendererMethod = formComponent.getClass().getMethod(
					"getChoiceRenderer", (Class[])null);
				getChoiceRendererMethod.setAccessible(true);
				IChoiceRenderer<Object> choiceRenderer = (IChoiceRenderer<Object>)getChoiceRendererMethod.invoke(
					formComponent, (Object[])null);

				return choiceRenderer.getIdValue(choices.get(index), index);
			}
			catch (SecurityException e)
			{
				throw new WicketRuntimeException("unexpect select failure", e);
			}
			catch (NoSuchMethodException e)
			{
				// component without getChoices() or getChoiceRenderer() is not
				// selectable
				return null;
			}
			catch (IllegalAccessException e)
			{
				throw new WicketRuntimeException("unexpect select failure", e);
			}
			catch (InvocationTargetException e)
			{
				throw new WicketRuntimeException("unexpect select failure", e);
			}
		}
	}

	/**
	 * A factory that creates an appropriate <code>ChoiceSelector</code> based on type of
	 * <code>FormComponent</code>.
	 */
	private class ChoiceSelectorFactory
	{
		/**
		 * <code>MultipleChoiceSelector</code> class.
		 */
		private final class MultipleChoiceSelector extends ChoiceSelector
		{
			/**
			 * Constructor.
			 * 
			 * @param formComponent
			 *            a <code>FormComponent</code>
			 */
			protected MultipleChoiceSelector(FormComponent<?> formComponent)
			{
				super(formComponent);
				if (!allowMultipleChoice(formComponent))
				{
					fail("Component:'" + formComponent.getPath() +
						"' Does not support multiple selection.");
				}
			}

			/**
			 * 
			 * @see org.apache.wicket.util.tester.FormTester.ChoiceSelector#assignValueToFormComponent(org.apache.wicket.markup.html.form.FormComponent,
			 *      java.lang.String)
			 */
			@Override
			protected void assignValueToFormComponent(FormComponent<?> formComponent, String value)
			{
				// multiple selectable should retain selected option
				addFormComponentValue(formComponent, value);
			}
		}

		/**
		 * <code>SingleChoiceSelector</code> class.
		 */
		private final class SingleChoiceSelector extends ChoiceSelector
		{
			/**
			 * Constructor.
			 * 
			 * @param formComponent
			 *            a <code>FormComponent</code>
			 */
			protected SingleChoiceSelector(FormComponent<?> formComponent)
			{
				super(formComponent);
			}

			/**
			 * @see org.apache.wicket.util.tester.FormTester.ChoiceSelector#assignValueToFormComponent(org.apache.wicket.markup.html.form.FormComponent,
			 *      java.lang.String)
			 */
			@Override
			protected void assignValueToFormComponent(FormComponent<?> formComponent, String value)
			{
				// single selectable should overwrite already selected option
				setFormComponentValue(formComponent, value);
			}
		}

		/**
		 * Creates a <code>ChoiceSelector</code>.
		 * 
		 * @param formComponent
		 *            a <code>FormComponent</code>
		 * @return ChoiceSelector a <code>ChoiceSelector</code>
		 */
		protected ChoiceSelector create(FormComponent<?> formComponent)
		{
			if (formComponent == null)
			{
				fail("Trying to select on null component.");
			}

			if (formComponent instanceof RadioGroup || formComponent instanceof DropDownChoice ||
				formComponent instanceof RadioChoice)
			{
				return new SingleChoiceSelector(formComponent);
			}
			else if (allowMultipleChoice(formComponent))
			{
				return new MultipleChoiceSelector(formComponent);
			}
			else
			{
				fail("Selecting on the component:'" + formComponent.getPath() +
					"' is not supported.");
				return null;
			}
		}

		/**
		 * Creates a <code>MultipleChoiceSelector</code>.
		 * 
		 * @param formComponent
		 *            a <code>FormComponent</code>
		 * @return ChoiceSelector a <code>ChoiceSelector</code>
		 */
		protected ChoiceSelector createForMultiple(FormComponent<?> formComponent)
		{
			return new MultipleChoiceSelector(formComponent);
		}

		/**
		 * Tests if a given <code>FormComponent</code> allows multiple choice.
		 * 
		 * @param formComponent
		 *            a <code>FormComponent</code>
		 * @return <code>true</code> if the given FormComponent allows multiple choice
		 */
		private boolean allowMultipleChoice(FormComponent<?> formComponent)
		{
			return formComponent instanceof CheckGroup ||
				formComponent instanceof ListMultipleChoice;
		}
	}

	private final ChoiceSelectorFactory choiceSelectorFactory = new ChoiceSelectorFactory();

	/**
	 * An instance of <code>FormTester</code> can only be used once. Create a new instance of each
	 * test.
	 */
	private boolean closed = false;

	/** path to <code>FormComponent</code> */
	private final String path;

	/** <code>BaseWicketTester</code> that create <code>FormTester</code> */
	private final BaseWicketTester baseWicketTester;

	/** <code>FormComponent</code> to be tested */
	private final Form<?> workingForm;

	/**
	 * @see WicketTester#newFormTester(String)
	 * 
	 * @param path
	 *            path to <code>FormComponent</code>
	 * @param workingForm
	 *            <code>FormComponent</code> to be tested
	 * @param wicketTester
	 *            <code>WicketTester</code> that creates <code>FormTester</code>
	 * @param fillBlankString
	 *            specifies whether to fill child <code>TextComponent</code>s with blank
	 *            <code>String</code>s
	 */
	protected FormTester(final String path, final Form<?> workingForm,
		final BaseWicketTester wicketTester, final boolean fillBlankString)
	{
		this.path = path;
		this.workingForm = workingForm;
		baseWicketTester = wicketTester;
		baseWicketTester.setupRequestAndResponse();

		// fill blank String for Text Component.
		workingForm.visitFormComponents(new FormComponent.AbstractVisitor()
		{
			@SuppressWarnings("unchecked")
			@Override
			public void onFormComponent(final FormComponent<?> formComponent)
			{
				// do nothing for invisible component
				if (!formComponent.isVisibleInHierarchy())
				{
					return;
				}

				// if component is text field and do not have exist value, fill
				// blank String if required
				if (formComponent instanceof AbstractTextComponent)
				{
					if (Strings.isEmpty(formComponent.getValue()))
					{
						if (fillBlankString)
						{
							setFormComponentValue(formComponent, "");
						}
					}
					else
					{
						setFormComponentValue(formComponent, formComponent.getValue());
					}
				}
				else if ((formComponent instanceof DropDownChoice) ||
					(formComponent instanceof RadioChoice) || (formComponent instanceof CheckBox))
				{
					setFormComponentValue(formComponent, formComponent.getValue());
				}
				else if (formComponent instanceof ListMultipleChoice)
				{
					final String[] modelValues = formComponent.getValue().split(
						FormComponent.VALUE_SEPARATOR);
					for (int i = 0; i < modelValues.length; i++)
					{
						addFormComponentValue(formComponent, modelValues[i]);
					}
				}
				else if (formComponent instanceof CheckGroup)
				{
					final Collection<?> checkGroupValues = (Collection<?>)formComponent.getModelObject();
					formComponent.visitChildren(Check.class, new IVisitor<Component<?>>()
					{
						public Object component(Component<?> component)
						{
							if (checkGroupValues.contains(component.getModelObject()))
							{
								addFormComponentValue(formComponent,
									((Check<?>)component).getValue());
							}
							return CONTINUE_TRAVERSAL;
						}
					});
				}
				else if (formComponent instanceof RadioGroup)
				{
					// TODO 1.5: see if all these transformations can be factored out into
					// checkgroup/radiogroup by them implementing some sort of interface {
					// getValue(); } otherwise all these implementation details leak into the tester
					final Object value = formComponent.getModelObject();
					if (value != null)
					{
						formComponent.visitChildren(Radio.class, new IVisitor<Component<?>>()
						{
							public Object component(Component<?> component)
							{
								if (value.equals(component.getModelObject()))
								{
									addFormComponentValue(formComponent,
										((Radio<?>)component).getValue());
									return STOP_TRAVERSAL;
								}
								return CONTINUE_TRAVERSAL_BUT_DONT_GO_DEEPER;
							}
						});
					}
				}
			}

		});
	}

	/**
	 * Retrieves the current <code>Form</code> object.
	 * 
	 * @return the working <code>Form</code>
	 */
	public Form<?> getForm()
	{
		return workingForm;
	}

	/**
	 * Gets the value for an <code>AbstractTextComponent</code> with the provided id.
	 * 
	 * @param id
	 *            <code>Component</code> id
	 * @return the value of the text component
	 */
	public String getTextComponentValue(String id)
	{
		Component<?> c = getForm().get(id);
		if (c instanceof AbstractTextComponent)
		{
			return ((AbstractTextComponent<?>)c).getValue();
		}
		return null;
	}

	/**
	 * Simulates selecting an option of a <code>FormComponent</code>. Supports
	 * <code>RadioGroup</code>, <code>CheckGroup</code>, and <code>AbstractChoice</code>
	 * family currently. The behavior is similar to interacting on the browser: For a single choice,
	 * such as <code>Radio</code> or <code>DropDownList</code>, the selection will toggle each
	 * other. For multiple choice, such as <code>Checkbox</code> or
	 * <code>ListMultipleChoice</code>, the selection will accumulate.
	 * 
	 * @param formComponentId
	 *            relative path (from <code>Form</code>) to the selectable
	 *            <code>FormComponent</code>
	 * @param index
	 *            index of the selectable option, starting from 0
	 */
	public void select(String formComponentId, int index)
	{
		checkClosed();
		FormComponent<?> component = (FormComponent<?>)workingForm.get(formComponentId);

		ChoiceSelector choiceSelector = choiceSelectorFactory.create(component);
		choiceSelector.doSelect(index);
		if (component instanceof DropDownChoice)
		{
			try
			{
				Method wantOnSelectionChangedNotificationsMethod = DropDownChoice.class.getDeclaredMethod(
					"wantOnSelectionChangedNotifications", new Class[0]);
				wantOnSelectionChangedNotificationsMethod.setAccessible(true);
				boolean wantOnSelectionChangedNotifications = ((Boolean)wantOnSelectionChangedNotificationsMethod.invoke(
					component, new Object[0])).booleanValue();
				if (wantOnSelectionChangedNotifications)
				{
					((DropDownChoice<?>)component).onSelectionChanged();
				}
			}
			catch (Exception e)
			{
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * A convenience method to select multiple options for the <code>FormComponent</code>. The
	 * method only support multiple selectable <code>FormComponent</code>s.
	 * 
	 * @see #select(String, int)
	 * 
	 * @param formComponentId
	 *            relative path (from <code>Form</code>) to the selectable
	 *            <code>FormComponent</code>
	 * @param indexes
	 *            index of the selectable option, starting from 0
	 */
	public void selectMultiple(String formComponentId, int[] indexes)
	{
		checkClosed();

		ChoiceSelector choiceSelector = choiceSelectorFactory.createForMultiple((FormComponent<?>)workingForm.get(formComponentId));

		for (int i = 0; i < indexes.length; i++)
		{
			choiceSelector.doSelect(indexes[i]);
		}
	}

	/**
	 * Simulates filling in a field on a <code>Form</code>.
	 * 
	 * @param formComponentId
	 *            relative path (from <code>Form</code>) to the selectable
	 *            <code>FormComponent</code> or <code>IFormSubmittingComponent</code>
	 * @param value
	 *            the field value
	 */
	public void setValue(final String formComponentId, final String value)
	{
		checkClosed();

		Component<?> component = workingForm.get(formComponentId);
		if (component instanceof IFormSubmittingComponent)
		{
			setFormSubmittingComponentValue((IFormSubmittingComponent)component, value);
		}
		else if (component instanceof FormComponent)
		{
			setFormComponentValue((FormComponent<?>)component, value);
		}
	}

	/**
	 * Sets the <code>File</code> on a {@link FileUploadField}.
	 * 
	 * @param formComponentId
	 *            relative path (from <code>Form</code>) to the selectable
	 *            <code>FormComponent</code>. The <code>FormComponent</code> must be of a type
	 *            <code>FileUploadField</code>.
	 * @param file
	 *            the <code>File</code> to upload.
	 * @param contentType
	 *            the content type of the file. Must be a valid mime type.
	 */
	public void setFile(final String formComponentId, final File file, final String contentType)
	{
		checkClosed();

		FormComponent<?> formComponent = (FormComponent<?>)workingForm.get(formComponentId);

		if (formComponent instanceof FileUploadField == false)
		{
			throw new IllegalArgumentException("'" + formComponentId + "' is not " +
				"a FileUploadField. You can only attach a file to form " +
				"component of this type.");
		}

		MockHttpServletRequest servletRequest = baseWicketTester.getServletRequest();
		servletRequest.addFile(formComponent.getInputName(), file, contentType);
	}

	/**
	 * Submits the <code>Form</code>. Note that <code>submit</code> can be executed only once.
	 */
	public void submit()
	{
		checkClosed();
		try
		{
			MockHttpServletRequest servletRequest = baseWicketTester.getServletRequest();

			WebRequestCycle requestCycle = baseWicketTester.createRequestCycle();
			servletRequest.setRequestToComponent(workingForm);

			servletRequest.setUseMultiPartContentType(isMultiPart());
			baseWicketTester.processRequestCycle(requestCycle);
		}
		finally
		{
			closed = true;
		}
	}

	private boolean isMultiPart()
	{
		try
		{
			Field multiPart = Form.class.getDeclaredField("multiPart");
			multiPart.setAccessible(true);
			return multiPart.getBoolean(workingForm);
		}
		catch (SecurityException e)
		{
			throw new RuntimeException(e);
		}
		catch (NoSuchFieldException e)
		{
			throw new RuntimeException(e);
		}
		catch (IllegalAccessException e)
		{
			throw new RuntimeException(e);
		}
	}

	/**
	 * A convenience method for submitting the <code>Form</code> with an alternate button.
	 * <p>
	 * Note that if the button is associated with a model, it's better to use the
	 * <code>setValue</code> method instead:
	 * 
	 * <pre>
	 * formTester.setValue(&quot;to:my:button&quot;, &quot;value on the button&quot;);
	 * formTester.submit();
	 * </pre>
	 * 
	 * @param buttonComponentId
	 *            relative path (from <code>Form</code>) to the button
	 */
	public void submit(String buttonComponentId)
	{
		setValue(buttonComponentId, "marked");
		submit();
	}

	/**
	 * Adds an additional <code>FormComponent</code>'s value into request parameter -- this
	 * method retains existing parameters but removes any duplicate parameters.
	 * 
	 * @param formComponent
	 *            a <code>FormComponent</code>
	 * @param value
	 *            a value to add
	 */
	private void addFormComponentValue(FormComponent<?> formComponent, String value)
	{
		if (parameterExist(formComponent))
		{
			String[] values = baseWicketTester.getServletRequest().getParameterValues(
				formComponent.getInputName());
			// remove duplicated
			HashSet<String> all = new HashSet<String>(Arrays.asList(values));
			all.add(value);
			Map<String, String[]> newParameters = new HashMap<String, String[]>();
			newParameters.put(formComponent.getInputName(), all.toArray(new String[all.size()]));
			baseWicketTester.getServletRequest().setParameters(newParameters);
		}
		else
		{
			setFormComponentValue(formComponent, value);
		}
	}

	/**
	 * <code>FormTester</code> must only be used once. Create a new instance of
	 * <code>FormTester</code> for each test.
	 */
	private void checkClosed()
	{
		if (closed)
		{
			throw new IllegalStateException("'" + path +
				"' already sumbitted. Note that FormTester " + "is allowed to submit only once");
		}
	}

	/**
	 * Returns <code>true</code> if the parameter exists in the <code>FormComponent</code>.
	 * 
	 * @param formComponent
	 *            a <code>FormComponent</code>
	 * @return <code>true</code> if the parameter exists in the <code>FormComponent</code>
	 */
	private boolean parameterExist(FormComponent<?> formComponent)
	{
		String parameter = baseWicketTester.getServletRequest().getParameter(
			formComponent.getInputName());
		return parameter != null && parameter.trim().length() > 0;
	}

	/**
	 * Set formComponent's value into request parameter, this method overwrites existing parameters.
	 * 
	 * @param formComponent
	 *            a <code>FormComponent</code>
	 * @param value
	 *            a value to add
	 */
	private void setFormComponentValue(FormComponent<?> formComponent, String value)
	{
		baseWicketTester.getServletRequest().setParameter(formComponent.getInputName(), value);
	}

	/**
	 * Set component's value into request parameter, this method overwrites existing parameters.
	 * 
	 * @param component
	 *            an {@link IFormSubmittingComponent}
	 * @param value
	 *            a value to add
	 */
	private void setFormSubmittingComponentValue(IFormSubmittingComponent component, String value)
	{
		baseWicketTester.getServletRequest().setParameter(component.getInputName(), value);
	}

	private void fail(String message)
	{
		throw new WicketRuntimeException(message);
	}

}
