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
package wicket.util.tester;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;
import wicket.Component;
import wicket.WicketRuntimeException;
import wicket.Component.IVisitor;
import wicket.markup.html.form.AbstractTextComponent;
import wicket.markup.html.form.Check;
import wicket.markup.html.form.CheckGroup;
import wicket.markup.html.form.DropDownChoice;
import wicket.markup.html.form.Form;
import wicket.markup.html.form.FormComponent;
import wicket.markup.html.form.IChoiceRenderer;
import wicket.markup.html.form.ListMultipleChoice;
import wicket.markup.html.form.Radio;
import wicket.markup.html.form.RadioChoice;
import wicket.markup.html.form.RadioGroup;
import wicket.util.string.Strings;

/**
 * A helper for testing validaiton and submission of Form component.
 * 
 * @author Ingram Chen
 */
public class FormTester
{
	/**
	 * An instance of FormTester can only be used once. Create a new instance of
	 * each test
	 */
	private boolean closed = false;

	/** form component to be test */
	private Form workingForm;

	/** wicketTester that create FormTester */
	private final WicketTester wicketTester;

	/** path to form component */
	private final String path;

	private ChoiceSelectorFactory choiceSelectorFactory = new ChoiceSelectorFactory();

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
			final WicketTester wicketTester, final boolean fillBlankString)
	{
		this.path = path;
		this.workingForm = workingForm;
		this.wicketTester = wicketTester;
		this.wicketTester.setupRequestAndResponse();

		// fill blank String for Text Component.
		workingForm.visitFormComponents(new FormComponent.IVisitor()
		{
			public void formComponent(FormComponent formComponent)
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
					if (fillBlankString && Strings.isEmpty(formComponent.getValue()))
					{
						setFormComponentValue(formComponent, "");
						return;
					}
				}
			}

		});
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
	 * submit the form. note that submit() can be executed only once.
	 */
	public void submit()
	{
		checkClosed();
		try
		{
			wicketTester.getServletRequest().setRequestToComponent(workingForm);
			wicketTester.processRequestCycle();
		}
		finally
		{
			closed = true;
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
		ChoiceSelector choiceSelector = choiceSelectorFactory.create((FormComponent)workingForm
				.get(formComponentId));
		choiceSelector.doSelect(index);
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
	 * set formComponent's value into request parameter, this method overwrites
	 * exist parameters.
	 * 
	 * @param formComponent
	 * @param value
	 */
	private void setFormComponentValue(FormComponent formComponent, String value)
	{
		wicketTester.getServletRequest().setParameter(formComponent.getInputName(), value);
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
			String[] values = wicketTester.getServletRequest().getParameterValues(
					formComponent.getInputName());
			// remove duplicated
			HashSet all = new HashSet(Arrays.asList(values));
			all.add(value);
			Map newParameters = new HashMap();
			newParameters.put(formComponent.getInputName(), all.toArray(new String[all.size()]));
			wicketTester.getServletRequest().setParameters(newParameters);
		}
		else
		{
			setFormComponentValue(formComponent, value);
		}
	}

	/**
	 * 
	 * @param formComponent
	 * @return Boolean
	 */
	private boolean parameterExist(FormComponent formComponent)
	{
		String parameter = wicketTester.getServletRequest().getParameter(
				formComponent.getInputName());
		return parameter != null && parameter.trim().length() > 0;
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
			 * @see wicket.util.tester.FormTester.ChoiceSelector#assignValueToFormComponent(wicket.markup.html.form.FormComponent,
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
					Assert.fail("Component:'" + formComponent.getPath()
							+ "' Not support multiple selection.");
				}
			}

			/**
			 * 
			 * @see wicket.util.tester.FormTester.ChoiceSelector#assignValueToFormComponent(wicket.markup.html.form.FormComponent,
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
		 * @param formComponent
		 * @return ChoiceSelector
		 */
		protected ChoiceSelector create(FormComponent formComponent)
		{
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
				Assert.fail("Selecting on the component:'" + formComponent.getPath()
						+ "' is not supported.");
				return null;
			}
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

		/**
		 * 
		 * @param formComponent
		 * @return ChoiceSelector
		 */
		protected ChoiceSelector createForMultiple(FormComponent formComponent)
		{
			return new MultipleChoiceSelector(formComponent);
		}
	}

	/**
	 * A selector template for selecting seletable form component via index of
	 * option, support RadioGroup, CheckGroup, and AbstractChoice family.
	 * 
	 */
	protected abstract class ChoiceSelector
	{
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
					Assert.fail("RadioGroup " + formComponent.getPath() + " does not has index:"
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
					Assert.fail("CheckGroup " + formComponent.getPath() + " does not have index:"
							+ index);
				}


				assignValueToFormComponent(formComponent, String.valueOf(foundCheck.getValue()));
			}
			else
			{
				String idValue = selectAbstractChoice(formComponent, index);
				if (idValue == null)
				{
					Assert.fail(formComponent.getPath() + " is not selectable component.");
				}
				else
				{
					assignValueToFormComponent(formComponent, idValue);
				}
			}
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

		/**
		 * ???
		 */
		private final class SearchOptionByIndexVisitor implements IVisitor
		{
			private final int index;

			int count = 0;

			private SearchOptionByIndexVisitor(int index)
			{
				super();
				this.index = index;
			}

			/**
			 * @see wicket.Component.IVisitor#component(wicket.Component)
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
	}
}