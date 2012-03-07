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
package org.apache.wicket.examples.wizard;

import java.util.Arrays;
import java.util.List;

import org.apache.wicket.extensions.wizard.StaticContentStep;
import org.apache.wicket.extensions.wizard.Wizard;
import org.apache.wicket.extensions.wizard.WizardModel;
import org.apache.wicket.extensions.wizard.WizardModel.ICondition;
import org.apache.wicket.extensions.wizard.WizardStep;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.ListMultipleChoice;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.validation.AbstractFormValidator;
import org.apache.wicket.markup.html.form.validation.EqualInputValidator;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.validation.ValidationError;
import org.apache.wicket.validation.validator.EmailAddressValidator;


/**
 * This wizard shows some basic form use. It uses custom panels for the form elements, and a single
 * domain object ({@link User}) as it's subject. Also, the user roles step}is an optional step, that
 * will only be executed when assignRoles is true (and that value is edited in the user details
 * step).
 * 
 * @author Eelco Hillenius
 */
public class NewUserWizard extends Wizard
{
	/**
	 * The confirmation step.
	 */
	private final class ConfirmationStep extends StaticContentStep
	{
		/**
		 * Construct.
		 */
		public ConfirmationStep()
		{
			super(true);
			IModel<User> userModel = new Model<User>(user);
			setTitleModel(new ResourceModel("confirmation.title"));
			setSummaryModel(new StringResourceModel("confirmation.summary", this, userModel));
			setContentModel(new StringResourceModel("confirmation.content", this, userModel));
		}
	}

	/**
	 * The user details step.
	 */
	private final class UserDetailsStep extends WizardStep
	{
		/**
		 * Construct.
		 */
		public UserDetailsStep()
		{
			setTitleModel(new ResourceModel("confirmation.title"));
			setSummaryModel(new StringResourceModel("userdetails.summary", this, new Model<User>(
				user)));
			add(new RequiredTextField<String>("user.firstName"));
			add(new RequiredTextField<String>("user.lastName"));
			add(new TextField<String>("user.department"));
			add(new CheckBox("assignRoles"));
		}
	}

	/**
	 * The user name step.
	 */
	private final class UserNameStep extends WizardStep
	{
		/**
		 * Construct.
		 */
		public UserNameStep()
		{
			super(new ResourceModel("username.title"), new ResourceModel("username.summary"));

			add(new RequiredTextField<String>("user.userName"));

			FormComponent<String> email = new RequiredTextField<String>("user.email").add(EmailAddressValidator.getInstance());
			add(email);

			TextField<String> emailRepeat = new TextField<String>("emailRepeat",
				new Model<String>());
			add(emailRepeat);

			add(new EqualInputValidator(email, emailRepeat));
		}
	}

	/**
	 * The user details step.
	 */
	private final class UserRolesStep extends WizardStep implements ICondition
	{
		/**
		 * Construct.
		 */
		public UserRolesStep()
		{
			super(new ResourceModel("userroles.title"), null);
			setSummaryModel(new StringResourceModel("userroles.summary", this,
				new Model<User>(user)));
			final ListMultipleChoice<String> rolesChoiceField = new ListMultipleChoice<String>(
				"user.roles", allRoles);
			add(rolesChoiceField);
			final TextField<String> rolesSetNameField = new TextField<String>("user.rolesSetName");
			add(rolesSetNameField);
			add(new AbstractFormValidator()
			{
				public FormComponent[] getDependentFormComponents()
				{
					// name and roles don't have anything to validate,
					// so might as well just skip them here
					return null;
				}

				public void validate(Form<?> form)
				{
					String rolesInput = rolesChoiceField.getInput();
					if (rolesInput != null && (!"".equals(rolesInput)))
					{
						if ("".equals(rolesSetNameField.getInput()))
						{
							rolesSetNameField.error(new ValidationError().addKey("error.noSetNameForRoles"));
						}
					}
				}
			});
		}

		/**
		 * @see org.apache.wicket.extensions.wizard.WizardModel.ICondition#evaluate()
		 */
		public boolean evaluate()
		{
			return assignRoles;
		}
	}

	/** cheap ass roles database. */
	private static final List<String> allRoles = Arrays.asList("admin", "user", "moderator",
		"joker", "slacker");

	/** Whether the assign roles step should be executed. */
	private boolean assignRoles = false;

	/** The user we are editing. */
	private User user;

	/**
	 * Construct.
	 * 
	 * @param id
	 *            The component id
	 */
	public NewUserWizard(String id)
	{
		super(id);

		// create a blank user
		user = new User();

		setDefaultModel(new CompoundPropertyModel<NewUserWizard>(this));
		WizardModel model = new WizardModel();
		model.add(new UserNameStep());
		model.add(new UserDetailsStep());
		model.add(new UserRolesStep());
		model.add(new ConfirmationStep());

		// initialize the wizard with the wizard model we just built
		init(model);
	}

	/**
	 * Gets user.
	 * 
	 * @return user
	 */
	public User getUser()
	{
		return user;
	}

	/**
	 * Gets assignRoles.
	 * 
	 * @return assignRoles
	 */
	public boolean isAssignRoles()
	{
		return assignRoles;
	}

	/**
	 * @see org.apache.wicket.extensions.wizard.Wizard#onCancel()
	 */
	@Override
	public void onCancel()
	{
		setResponsePage(Index.class);
	}

	/**
	 * @see org.apache.wicket.extensions.wizard.Wizard#onFinish()
	 */
	@Override
	public void onFinish()
	{
		setResponsePage(Index.class);
	}

	/**
	 * Sets assignRoles.
	 * 
	 * @param assignRoles
	 *            assignRoles
	 */
	public void setAssignRoles(boolean assignRoles)
	{
		this.assignRoles = assignRoles;
	}

	/**
	 * Sets user.
	 * 
	 * @param user
	 *            user
	 */
	public void setUser(User user)
	{
		this.user = user;
	}
}