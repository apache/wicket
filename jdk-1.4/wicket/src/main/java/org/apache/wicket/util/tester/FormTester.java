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
 * A helper for testing validation and submission of Form component.
 *
 * @author Ingram Chen
 * @author Frank Bille (frankbille)
 */
public class FormTester
{
	/**
	 * A selector template for selecting seletable form component via index of
	 * option, support RadioGroup, CheckGroup, and AbstractChoice family.
	 *
	 */
	protected abstract class ChoiceSelector
	{
		/**
		 * ???
		 */
		private final class SearchOptionByIndexVisitor implements IVisitor
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
			public Object component(Component component)
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

		private final FormComponent formComponent;

		/**
		 * Construct.
		 *
		 * @param formComponent
		 */
		protected ChoiceSelector(FormComponent formComponent)
		{
			this.formComponent = formComponent;
		}

		/**
		 * implement whether toggle or cumulate selection
		 *
		 * @param formComponent
		 * @param value
		 */
		protected abstract void assignValueToFormComponent(FormComponent formComponent, String value);

		/**
		 *
		 * @param index
		 */
		protected final void doSelect(final int index)
		{
			if (formComponent instanceof RadioGroup)
			{
				Radio foundRadio = (Radio)formComponent.visitChildren(Radio.class,
						new SearchOptionByIndexVisitor(index));
				if (foundRadio == null)
				{
					fail("RadioGroup " + formComponent.getPath() + " does not has index:"
							+ index);
				}
				assignValueToFormComponent(formComponent, foundRadio.getValue());
			}
			else if (formComponent instanceof CheckGroup)
			{
				Check foundCheck = (Check)formComponent.visitChildren(Check.class,
						new SearchOptionByIndexVisitor(index));
				if (foundCheck == null)
				{
					fail("CheckGroup " + formComponent.getPath() + " does not have index:"
							+ index);
				}

				assignValueToFormComponent(formComponent, foundCheck.getValue());
			}
			else
			{
				String idValue = selectAbstractChoice(formComponent, index);
				if (idValue == null)
				{
					fail(formComponent.getPath() + " is not selectable component.");
				}
				else
				{
					assignValueToFormComponent(formComponent, idValue);
				}
			}
		}

		/**
		 * @param formComponent
		 * @param index
		 * @return xxx
		 */
		private String selectAbstractChoice(FormComponent formComponent, final int index)
		{
			try
			{
				Method getChoicesMethod = formComponent.getClass().getMethod("getChoices",
						(Class[])null);
				getChoicesMethod.setAccessible(true);
				List choices = (List)getChoicesMethod.invoke(formComponent, (Object[])null);

				Method getChoiceRendererMethod = formComponent.getClass().getMethod(
						"getChoiceRenderer", (Class[])null);
				getChoiceRendererMethod.setAccessible(true);
				IChoiceRenderer choiceRenderer = (IChoiceRenderer)getChoiceRendererMethod.invoke(
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
	 * A Factory to create appropriate ChoiceSelector based on type of
	 * formComponent
	 */
	private class ChoiceSelectorFactory
	{
		/**
		 *
		 */
		private final class MultipleChoiceSelector extends ChoiceSelector
		{
			/**
			 * Construct.
			 *
			 * @param formComponent
			 */
			protected MultipleChoiceSelector(FormComponent formComponent)
			{
				super(formComponent);
				if (!allowMultipleChoice(formComponent))
				{
					fail("Component:'" + formComponent.getPath()
							+ "' Does not support multiple selection.");
				}
			}

			/**
			 *
			 * @see org.apache.wicket.util.tester.FormTester.ChoiceSelector#assignValueToFormComponent(org.apache.wicket.markup.html.form.FormComponent,
			 *      java.lang.String)
			 */
			protected void assignValueToFormComponent(FormComponent formComponent, String value)
			{
				// multiple selectable should retain selected option
				addFormComponentValue(formComponent, value);
			}
		}

		/**
		 *
		 */
		private final class SingleChoiceSelector extends ChoiceSelector
		{
			/**
			 * Construct.
			 *
			 * @param formComponent
			 */
			protected SingleChoiceSelector(FormComponent formComponent)
			{
				super(formComponent);
			}

			/**
			 *
			 * @see org.apache.wicket.util.tester.FormTester.ChoiceSelector#assignValueToFormComponent(org.apache.wicket.markup.html.form.FormComponent,
			 *      java.lang.String)
			 */
			protected void assignValueToFormComponent(FormComponent formComponent, String value)
			{
				// single selectable should overwrite already selected option
				setFormComponentValue(formComponent, value);
			}
		}

		/**
		 *
		 * @param formComponent
		 * @return ChoiceSelector
		 */
		protected ChoiceSelector create(FormComponent formComponent)
		{
			if (formComponent == null)
			{
				fail("Trying to select on null component.");
			}

			if (formComponent instanceof RadioGroup || formComponent instanceof DropDownChoice
					|| formComponent instanceof RadioChoice)
			{
				return new SingleChoiceSelector(formComponent);
			}
			else if (allowMultipleChoice(formComponent))
			{
				return new MultipleChoiceSelector(formComponent);
			}
			else
			{
				fail("Selecting on the component:'" + formComponent.getPath()
						+ "' is not supported.");
				return null;
			}
		}

		/**
		 *
		 * @param formComponent
		 * @return ChoiceSelector
		 */
		protected ChoiceSelector createForMultiple(FormComponent formComponent)
		{
			return new MultipleChoiceSelector(formComponent);
		}

		/**
		 *
		 * @param formComponent
		 * @return boolean
		 */
		private boolean allowMultipleChoice(FormComponent formComponent)
		{
			return formComponent instanceof CheckGroup
			|| formComponent instanceof ListMultipleChoice;
		}
	}

	private final ChoiceSelectorFactory choiceSelectorFactory = new ChoiceSelectorFactory();

	/**
	 * An instance of FormTester can only be used once. Create a new instance of
	 * each test
	 */
	private boolean closed = false;

	/** path to form component */
	private final String path;

	/** baseWicketTester that create FormTester */
	private final BaseWicketTester baseWicketTester;

	/** form component to be test */
	private final Form workingForm;

	/**
	 * @see WicketTester#newFormTester(String)
	 *
	 * @param path
	 *            path to form component
	 * @param workingForm
	 *            form component to be test
	 * @param wicketTester
	 *            wicketTester that create FormTester
	 * @param fillBlankString
	 *            specify whether filling child Text Components with blank
	 *            String
	 */
	protected FormTester(final String path, final Form workingForm,
			final BaseWicketTester wicketTester, final boolean fillBlankString)
	{
		this.path = path;
		this.workingForm = workingForm;
		this.baseWicketTester = wicketTester;
		this.baseWicketTester.setupRequestAndResponse();

		// fill blank String for Text Component.
		workingForm.visitFormComponents(new FormComponent.AbstractVisitor()
		{
			public void onFormComponent(final FormComponent formComponent)
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
				else if ( (formComponent instanceof DropDownChoice) ||
						(formComponent instanceof RadioChoice) ||
						(formComponent instanceof CheckBox))
				{
					setFormComponentValue(formComponent, formComponent.getValue());
				}
				else if (formComponent instanceof ListMultipleChoice)
				{
					final String[] modelValues = formComponent.getValue().split(FormComponent.VALUE_SEPARATOR);
					for (int i = 0; i < modelValues.length; i++)
					{
						addFormComponentValue(formComponent, modelValues[i]);
					}
				}
				else if (formComponent instanceof CheckGroup)
				{
					final Collection checkGroupValues = (Collection) formComponent.getModelObject();
					formComponent.visitChildren(Check.class, new IVisitor()
					{
						public Object component(Component component)
						{
							if (checkGroupValues.contains(component.getModelObject()))
							{
								addFormComponentValue(formComponent, ((Check) component).getValue());
							}
							return CONTINUE_TRAVERSAL;
						}
					});
				}
			}

		});
	}

	/**
	 * @return work form
	 */
	public Form getForm()
	{
		return workingForm;
	}

	/**
	 * Gets value for text component with provided id.
	 *
	 * @param id
	 *            Component's id
	 * @return value text component
	 */
	public String getTextComponentValue(String id)
	{
		Component c = getForm().get(id);
		if (c instanceof AbstractTextComponent)
		{
			return ((AbstractTextComponent)c).getValue();
		}
		return null;
	}

	/**
	 * simulate selecting an option of a Form Component. Support RadioGroup,
	 * CheckGroup, and AbstractChoice family currently. The behavior is similar
	 * to interacting on the browser: For single choice, such as Radio or
	 * DropDownList, the selection will toggle each other. For multiple choice,
	 * such as Checkbox or ListMultipleChoice, the selection will cumulate.
	 *
	 * @param formComponentId
	 *            relative path (from form) to selectable formComponent
	 * @param index
	 *            index of selectable option, start from 0
	 */
	public void select(String formComponentId, int index)
	{
		checkClosed();
		FormComponent component = (FormComponent)workingForm
		.get(formComponentId);

		ChoiceSelector choiceSelector = choiceSelectorFactory.create(component);
		choiceSelector.doSelect(index);
		if (component instanceof DropDownChoice) {
			try
			{
				Method wantOnSelectionChangedNotificationsMethod = DropDownChoice.class.getDeclaredMethod("wantOnSelectionChangedNotifications", new Class[0]);
				wantOnSelectionChangedNotificationsMethod.setAccessible(true);
				boolean wantOnSelectionChangedNotifications = ((Boolean)wantOnSelectionChangedNotificationsMethod.invoke(component, new Object[0])).booleanValue();
				if (wantOnSelectionChangedNotifications) {
					((DropDownChoice)component).onSelectionChanged();
				}
			}
			catch (Exception e)
			{
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * A convenient method to select multiple options for the form component.
	 * The method only support multiple selectable form component.
	 *
	 * @see #select(String, int)
	 *
	 * @param formComponentId
	 *            relative path (from form) to selectable formComponent
	 * @param indexes
	 *            index of selectable option, start from 0
	 */
	public void selectMultiple(String formComponentId, int[] indexes)
	{
		checkClosed();

		ChoiceSelector choiceSelector = choiceSelectorFactory
		.createForMultiple((FormComponent)workingForm.get(formComponentId));

		for (int i = 0; i < indexes.length; i++)
		{
			choiceSelector.doSelect(indexes[i]);
		}
	}

	/**
	 * simulate filling a field of a Form.
	 *
	 * @param formComponentId
	 *            relative path (from form) to formComponent
	 * @param value
	 *            field value of form.
	 */
	public void setValue(final String formComponentId, final String value)
	{
		checkClosed();

		FormComponent formComponent = (FormComponent)workingForm.get(formComponentId);
		setFormComponentValue(formComponent, value);
	}

	/**
	 * Set the file on a {@link FileUploadField}.
	 *
	 * @param formComponentId
	 *            relative path (from form) to formComponent. The form component
	 *            must be of a type FileUploadField.
	 * @param file
	 *            The file to upload.
	 * @param contentType
	 *            The content type of the file. Must be a correct mimetype.
	 */
	public void setFile(final String formComponentId, final File file, final String contentType)
	{
		checkClosed();

		FormComponent formComponent = (FormComponent)workingForm.get(formComponentId);

		if (formComponent instanceof FileUploadField == false)
		{
			throw new IllegalArgumentException("'" + formComponentId + "' is not "
					+ "a FileUploadField. You can only attach a file to form "
					+ "component of this type.");
		}

		MockHttpServletRequest servletRequest = baseWicketTester.getServletRequest();
		servletRequest.addFile(formComponent.getInputName(), file, contentType);
	}

	/**
	 * submit the form. note that submit() can be executed only once.
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
	 * A convenient method to submit form with alternative button.
	 *
	 * Note that if the button associates with a model, it's better to use
	 * setValue() instead:
	 *
	 * <pre>
	 * formTester.setValue(&quot;to:my:button&quot;, &quot;value on the button&quot;);
	 * formTester.submit();
	 * </pre>
	 *
	 * @param buttonComponentId
	 *            relative path (from form) to the button
	 */
	public void submit(String buttonComponentId)
	{
		setValue(buttonComponentId, "marked");
		submit();
	}

	/**
	 * add additional formComponent's value into request parameter, this method
	 * retain exist parameters but remove any duplicated parameters.
	 *
	 * @param formComponent
	 * @param value
	 */
	private void addFormComponentValue(FormComponent formComponent, String value)
	{
		if (parameterExist(formComponent))
		{
			String[] values = baseWicketTester.getServletRequest().getParameterValues(
					formComponent.getInputName());
			// remove duplicated
			HashSet all = new HashSet(Arrays.asList(values));
			all.add(value);
			Map newParameters = new HashMap();
			newParameters.put(formComponent.getInputName(), all.toArray(new String[all.size()]));
			baseWicketTester.getServletRequest().setParameters(newParameters);
		}
		else
		{
			setFormComponentValue(formComponent, value);
		}
	}

	/**
	 * FormTester must only be used once. Create a new instance of FormTester
	 * for each test.
	 */
	private void checkClosed()
	{
		if (closed)
		{
			throw new IllegalStateException("'" + path
					+ "' already sumbitted. Note that FormTester "
					+ "is allowed to submit only once");
		}
	}

	/**
	 *
	 * @param formComponent
	 * @return Boolean
	 */
	private boolean parameterExist(FormComponent formComponent)
	{
		String parameter = baseWicketTester.getServletRequest().getParameter(
				formComponent.getInputName());
		return parameter != null && parameter.trim().length() > 0;
	}

	/**
	 * set formComponent's value into request parameter, this method overwrites
	 * exist parameters.
	 *
	 * @param formComponent
	 * @param value
	 */
	private void setFormComponentValue(FormComponent formComponent, String value)
	{
		baseWicketTester.getServletRequest().setParameter(formComponent.getInputName(), value);
	}


	private void fail(String message)
	{
		throw new WicketRuntimeException(message);
	}

}
