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


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import junit.framework.Assert;

import org.apache.wicket.Component;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.markup.html.form.AbstractTextComponent;
import org.apache.wicket.markup.html.form.Check;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.CheckGroup;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.IFormSubmittingComponent;
import org.apache.wicket.markup.html.form.IOnChangeListener;
import org.apache.wicket.markup.html.form.ListMultipleChoice;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioChoice;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.protocol.http.mock.MockHttpServletRequest;
import org.apache.wicket.util.file.File;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.string.StringValue;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;

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
		private final class SearchOptionByIndexVisitor implements IVisitor<Component, Component>
		{
			int count = 0;

			private final int index;

			private SearchOptionByIndexVisitor(int index)
			{
				super();
				this.index = index;
			}

			/**
			 * @see org.apache.wicket.util.visit.IVisitor#component(Object,
			 *      org.apache.wicket.util.visit.IVisit)
			 */
			@Override
			public void component(final Component component, final IVisit<Component> visit)
			{
				if (count == index)
				{
					visit.stop(component);
				}
				else
				{
					count++;
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

		public String getChoiceValueForIndex(int index)
		{
			if (formComponent instanceof RadioGroup)
			{
				Radio<?> foundRadio = (Radio<?>)formComponent.visitChildren(Radio.class,
					new SearchOptionByIndexVisitor(index));
				if (foundRadio == null)
				{
					fail("RadioGroup " + formComponent.getPath() + " does not have index:" + index);
					return null;
				}
				return foundRadio.getValue();
			}
			else if (formComponent instanceof CheckGroup)
			{
				Check<?> foundCheck = (Check<?>)formComponent.visitChildren(Check.class,
					new SearchOptionByIndexVisitor(index));
				if (foundCheck == null)
				{
					fail("CheckGroup " + formComponent.getPath() + " does not have index:" + index);
					return null;
				}

				return foundCheck.getValue();
			}
			else
			{
				String idValue = selectAbstractChoice(formComponent, index);
				if (idValue == null)
				{
					fail(formComponent.getPath() + " is not a selectable Component.");
					return null;
				}
				else
				{
					return idValue;
				}
			}

		}

		/**
		 * Selects a given index in a selectable <code>FormComponent</code>.
		 * 
		 * @param index
		 */
		protected final void doSelect(final int index)
		{
			String value = getChoiceValueForIndex(index);
			assignValueToFormComponent(formComponent, value);
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
		private String selectAbstractChoice(final FormComponent<?> formComponent, final int index)
		{
			try
			{
				Method getChoicesMethod = formComponent.getClass().getMethod("getChoices",
					(Class<?>[])null);
				getChoicesMethod.setAccessible(true);
				List<Object> choices = (List<Object>)getChoicesMethod.invoke(formComponent,
					(Object[])null);

				Method getChoiceRendererMethod = formComponent.getClass().getMethod(
					"getChoiceRenderer", (Class<?>[])null);
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
	private final BaseWicketTester tester;

	/** <code>FormComponent</code> to be tested */
	private final Form<?> workingForm;

	private boolean clearFeedbackMessagesBeforeSubmit = true;

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
		tester = wicketTester;

		// fill blank String for Text Component.
		workingForm.visitFormComponents(new IVisitor<FormComponent<?>, Void>()
		{
			@Override
			public void component(final FormComponent<?> formComponent, final IVisit<Void> visit)
			{
				// do nothing for invisible or disabled component -- the browser would not send any
				// parameter for a disabled component
				if (!(formComponent.isVisibleInHierarchy() && formComponent.isEnabledInHierarchy()))
				{
					return;
				}

				String[] values = getInputValue(formComponent);
				if (formComponent instanceof AbstractTextComponent<?>)
				{
					if (values.length == 0 && fillBlankString)
					{
						setFormComponentValue(formComponent, "");
					}
				}
				for (String value : values)
				{
					addFormComponentValue(formComponent, value);
				}
			}
		});
		workingForm.detach();
	}

	/**
	 * Gets request parameter values for the form component that represents its current model value
	 * 
	 * @param formComponent
	 * @return array containing parameter values
	 */
	public static String[] getInputValue(FormComponent<?> formComponent)
	{
		// do nothing for invisible or disabled component -- the browser would not send any
		// parameter for a disabled component
		if (!(formComponent.isVisibleInHierarchy() && formComponent.isEnabledInHierarchy()))
		{
			return new String[] { };
		}

		// if component is text field and do not have exist value, fill
		// blank String if required
		if (formComponent instanceof AbstractTextComponent)
		{
			return new String[] { getFormComponentValue(formComponent) };
		}
		else if ((formComponent instanceof DropDownChoice) ||
			(formComponent instanceof RadioChoice) || (formComponent instanceof CheckBox))
		{
			return new String[] { getFormComponentValue(formComponent) };
		}
		else if (formComponent instanceof ListMultipleChoice)
		{
			return getFormComponentValue(formComponent).split(FormComponent.VALUE_SEPARATOR);
		}
		else if (formComponent instanceof CheckGroup)
		{
			final Collection<?> checkGroupValues = (Collection<?>)formComponent.getDefaultModelObject();
			final List<String> result = new ArrayList<String>();
			formComponent.visitChildren(Check.class, new IVisitor<Component, Void>()
			{
				@Override
				public void component(final Component component, final IVisit<Void> visit)
				{
					if (checkGroupValues.contains(component.getDefaultModelObject()))
					{
						result.add(getFormComponentValue((Check<?>)component));
					}
				}
			});
			return result.toArray(new String[result.size()]);
		}
		else if (formComponent instanceof RadioGroup)
		{
			// TODO 1.5: see if all these transformations can be factored out into
			// checkgroup/radiogroup by them implementing some sort of interface {
			// getValue(); } otherwise all these implementation details leak into the tester
			final Object value = formComponent.getDefaultModelObject();
			String result = null;
			if (value != null)
			{
				result = formComponent.visitChildren(Radio.class, new IVisitor<Component, String>()
				{
					@Override
					public void component(final Component component, final IVisit<String> visit)
					{
						if (value.equals(component.getDefaultModelObject()))
						{
							visit.stop(getFormComponentValue((Radio<?>)component));
						}
						else
						{
							visit.dontGoDeeper();
						}
					}
				});
			}
			if (result == null)
			{
				return new String[] { };
			}
			else
			{
				return new String[] { result };
			}
		}
		return new String[] { };
	}


	private static String getFormComponentValue(final FormComponent<?> formComponent)
	{
		boolean oldEscape = formComponent.getEscapeModelStrings();
		formComponent.setEscapeModelStrings(false);
		String val = formComponent.getValue();
		formComponent.setEscapeModelStrings(oldEscape);
		return val;
	}

	private static String getFormComponentValue(final Check<?> formComponent)
	{
		boolean oldEscape = formComponent.getEscapeModelStrings();
		formComponent.setEscapeModelStrings(false);
		String val = formComponent.getValue();
		formComponent.setEscapeModelStrings(oldEscape);
		return val;
	}

	private static String getFormComponentValue(final Radio<?> formComponent)
	{
		boolean oldEscape = formComponent.getEscapeModelStrings();
		formComponent.setEscapeModelStrings(false);
		String val = formComponent.getValue();
		formComponent.setEscapeModelStrings(oldEscape);
		return val;
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
	public String getTextComponentValue(final String id)
	{
		Component c = getForm().get(id);
		if (c instanceof AbstractTextComponent)
		{
			return ((AbstractTextComponent<?>)c).getValue();
		}
		return null;
	}

	/**
	 * Simulates selecting an option of a <code>FormComponent</code>. Supports
	 * <code>RadioGroup</code>, <code>CheckGroup</code>, and <code>AbstractChoice</code> family
	 * currently. The behavior is similar to interacting on the browser: For a single choice, such
	 * as <code>Radio</code> or <code>DropDownList</code>, the selection will toggle each other. For
	 * multiple choice, such as <code>Checkbox</code> or <code>ListMultipleChoice</code>, the
	 * selection will accumulate.
	 * 
	 * @param formComponentId
	 *            relative path (from <code>Form</code>) to the selectable
	 *            <code>FormComponent</code>
	 * @param index
	 *            index of the selectable option, starting from 0
	 * @return This
	 */
	public FormTester select(final String formComponentId, int index)
	{
		checkClosed();
		FormComponent<?> component = (FormComponent<?>)workingForm.get(formComponentId);

		ChoiceSelector choiceSelector = choiceSelectorFactory.create(component);
		choiceSelector.doSelect(index);

		try
		{
			Method wantOnSelectionChangedNotificationsMethod = component.getClass()
				.getDeclaredMethod("wantOnSelectionChangedNotifications");

			try
			{
				wantOnSelectionChangedNotificationsMethod.setAccessible(true);
				boolean wantOnSelectionChangedNotifications = (Boolean)wantOnSelectionChangedNotificationsMethod.invoke(component);
				if (wantOnSelectionChangedNotifications)
				{
					tester.invokeListener(component, IOnChangeListener.INTERFACE);
				}
			}
			catch (final Exception x)
			{
				throw new RuntimeException(x);
			}

		}
		catch (final NoSuchMethodException ignored)
		{
			// this form component has no auto page reload mechanism
		}

		return this;
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
	 * @return This
	 */
	public FormTester selectMultiple(String formComponentId, int[] indexes)
	{
		return selectMultiple(formComponentId, indexes, false);
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
	 * @param replace
	 *            If true, than all previous selects are first reset, thus existing selects are
	 *            replaced. If false, than the new indexes will be added.
	 * @return This
	 */
	public FormTester selectMultiple(String formComponentId, int[] indexes, final boolean replace)
	{
		checkClosed();

		if (replace == true)
		{
			// Reset first
			setValue(formComponentId, "");
		}

		ChoiceSelector choiceSelector = choiceSelectorFactory.createForMultiple((FormComponent<?>)workingForm.get(formComponentId));

		for (int index : indexes)
		{
			choiceSelector.doSelect(index);
		}

		return this;
	}

	/**
	 * Simulates filling in a field on a <code>Form</code>.
	 * 
	 * @param formComponentId
	 *            relative path (from <code>Form</code>) to the selectable
	 *            <code>FormComponent</code> or <code>IFormSubmittingComponent</code>
	 * @param value
	 *            the field value
	 * @return This
	 */
	public FormTester setValue(final String formComponentId, final String value)
	{
		Component component = workingForm.get(formComponentId);
		Assert.assertNotNull("Unable to set value. Couldn't find component with name: " +
			formComponentId, component);
		return setValue(component, value);
	}

	/**
	 * Simulates filling in a field on a <code>Form</code>.
	 * 
	 * @param formComponent
	 *            relative path (from <code>Form</code>) to the selectable
	 *            <code>FormComponent</code> or <code>IFormSubmittingComponent</code>
	 * @param value
	 *            the field value
	 * @return This
	 */
	public FormTester setValue(final Component formComponent, final String value)
	{
		Args.notNull(formComponent, "formComponent");

		checkClosed();

		if (formComponent instanceof IFormSubmittingComponent)
		{
			setFormSubmittingComponentValue((IFormSubmittingComponent)formComponent, value);
		}
		else if (formComponent instanceof FormComponent)
		{
			setFormComponentValue((FormComponent<?>)formComponent, value);
		}
		else
		{
			fail("Component with id: " + formComponent.getId() + " is not a FormComponent");
		}

		return this;
	}

	/**
	 * @param checkBoxId
	 * @param value
	 * @return This
	 */
	public FormTester setValue(String checkBoxId, boolean value)
	{
		return setValue(checkBoxId, Boolean.toString(value));
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
	 * @return This
	 */
	public FormTester setFile(final String formComponentId, final File file,
		final String contentType)
	{
		checkClosed();

		FormComponent<?> formComponent = (FormComponent<?>)workingForm.get(formComponentId);

		if (formComponent instanceof FileUploadField == false)
		{
			fail("'" + formComponentId + "' is not " +
				"a FileUploadField. You can only attach a file to form " +
				"component of this type.");
		}

		MockHttpServletRequest servletRequest = tester.getRequest();
		servletRequest.addFile(formComponent.getInputName(), file, contentType);

		return this;
	}

	/**
	 * Submits the <code>Form</code>. Note that <code>submit</code> can be executed only once.
	 * 
	 * @return This
	 */
	public FormTester submit()
	{
		checkClosed();
		try
		{
			if (clearFeedbackMessagesBeforeSubmit)
			{
				tester.clearFeedbackMessages();
			}
			tester.getRequest().setUseMultiPartContentType(workingForm.isMultiPart());
			tester.submitForm(path);
		}
		finally
		{
			closed = true;
		}

		return this;
	}

	public boolean isClearFeedbackMessagesBeforeSubmit()
	{
		return clearFeedbackMessagesBeforeSubmit;
	}

	public FormTester setClearFeedbackMessagesBeforeSubmit(boolean clearFeedbackMessagesBeforeSubmit)
	{
		this.clearFeedbackMessagesBeforeSubmit = clearFeedbackMessagesBeforeSubmit;
		return this;
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
	 * @return This
	 */
	public FormTester submit(final String buttonComponentId)
	{
		setValue(buttonComponentId, "marked");
		return submit();
	}

	/**
	 * A convenience method for submitting the <code>Form</code> with an alternate button.
	 * <p>
	 * Note that if the button is associated with a model, it's better to use the
	 * <code>setValue</code> method instead:
	 * 
	 * <pre>
	 * formTester.setValue(myButton, &quot;value on the button&quot;);
	 * formTester.submit();
	 * </pre>
	 * 
	 * @param buttonComponent
	 *            relative path (from <code>Form</code>) to the button
	 * @return This
	 */
	public FormTester submit(final Component buttonComponent)
	{
		Args.notNull(buttonComponent, "buttonComponent");

		setValue(buttonComponent, "marked");
		return submit();
	}

	/**
	 * A convenience method to submit the Form via a SubmitLink which may inside or outside of the
	 * Form.
	 * 
	 * @param path
	 *            The path to the SubmitLink
	 * @param pageRelative
	 *            if true, than the 'path' to the SubmitLink is relative to the page. Thus the link
	 *            can be outside the form. If false, the path is relative to the form and thus the
	 *            link is inside the form.
	 * @return This
	 */
	public FormTester submitLink(String path, final boolean pageRelative)
	{
		if (pageRelative)
		{
			tester.clickLink(path, false);
		}
		else
		{
			path = this.path + ":" + path;
			tester.clickLink(path, false);
		}
		return this;
	}

	/**
	 * Adds an additional <code>FormComponent</code>'s value into request parameter -- this method
	 * retains existing parameters but removes any duplicate parameters.
	 * 
	 * @param formComponent
	 *            a <code>FormComponent</code>
	 * @param value
	 *            a value to add
	 * @return This
	 */
	private FormTester addFormComponentValue(FormComponent<?> formComponent, String value)
	{
		if (parameterExist(formComponent))
		{
			List<StringValue> values = tester.getRequest()
				.getPostParameters()
				.getParameterValues(formComponent.getInputName());
			// remove duplicated

			HashSet<String> all = new HashSet<String>();
			for (StringValue val : values)
			{
				all.add(val.toString());
			}
			all.add(value);

			values = new ArrayList<StringValue>();
			for (String val : all)
			{
				values.add(StringValue.valueOf(val));
			}
			tester.getRequest()
				.getPostParameters()
				.setParameterValues(formComponent.getInputName(), values);
		}
		else
		{
			setFormComponentValue(formComponent, value);
		}

		return this;
	}

	/**
	 * <code>FormTester</code> must only be used once. Create a new instance of
	 * <code>FormTester</code> for each test.
	 */
	private void checkClosed()
	{
		if (closed)
		{
			fail("'" + path + "' already submitted. Note that FormTester " +
				"is allowed to submit only once");
		}
	}

	/**
	 * Returns <code>true</code> if the parameter exists in the <code>FormComponent</code>.
	 * 
	 * @param formComponent
	 *            a <code>FormComponent</code>
	 * @return <code>true</code> if the parameter exists in the <code>FormComponent</code>
	 */
	private boolean parameterExist(final FormComponent<?> formComponent)
	{
		String parameter = tester.getRequest()
			.getPostParameters()
			.getParameterValue(formComponent.getInputName())
			.toString();

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
	private void setFormComponentValue(final FormComponent<?> formComponent, final String value)
	{
		tester.getRequest()
			.getPostParameters()
			.setParameterValue(formComponent.getInputName(), value);
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
		tester.getRequest().getPostParameters().setParameterValue(component.getInputName(), value);
	}

	/**
	 * 
	 * @param message
	 */
	private void fail(String message)
	{
		throw new WicketRuntimeException(message);
	}
}
