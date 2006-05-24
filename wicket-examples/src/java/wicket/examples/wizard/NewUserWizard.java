/*
 * $Id: org.eclipse.jdt.ui.prefs 5004 2006-03-17 20:47:08 -0800 (Fri, 17 Mar
 * 2006) eelco12 $ $Revision: 5004 $ $Date: 2006-03-17 20:47:08 -0800 (Fri, 17
 * Mar 2006) $
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.examples.wizard;

import java.util.Arrays;
import java.util.List;

import wicket.MarkupContainer;
import wicket.extensions.wizard.StaticContentStep;
import wicket.extensions.wizard.Wizard;
import wicket.extensions.wizard.WizardModel;
import wicket.extensions.wizard.WizardStep;
import wicket.extensions.wizard.WizardModel.ICondition;
import wicket.markup.html.form.CheckBox;
import wicket.markup.html.form.ListMultipleChoice;
import wicket.markup.html.form.RequiredTextField;
import wicket.markup.html.form.TextField;
import wicket.markup.html.form.validation.EmailAddressPatternValidator;
import wicket.model.CompoundPropertyModel;
import wicket.model.IModel;
import wicket.model.Model;
import wicket.model.ResourceModel;
import wicket.model.StringResourceModel;

/**
 * This wizard shows some basic form use. It uses custom panels for the form
 * elements, and a single domain object ({@link User}) as it's subject. Also,
 * {@link UserRolesStep the user roles step} is an optional step, that will only
 * be executed when assignRoles is true (and that value is edited in the
 * {@link UserDetailsStep user details step}).
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
		public ConfirmationStep(MarkupContainer<?> parent)
		{
			super(parent,true);
			IModel userModel = new Model(user);
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
		public UserDetailsStep(MarkupContainer<?> parent)
		{
			super(parent,new ResourceModel("userdetails.title"), null);
			setSummaryModel(new StringResourceModel("userdetails.summary", this, new Model(user)));
			add(new RequiredTextField(this,"user.firstName"));
			add(new RequiredTextField(this,"user.lastName"));
			add(new TextField(this,"user.department"));
			add(new CheckBox(this,"assignRoles"));
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
		public UserNameStep(MarkupContainer<?> parent)
		{
			super(parent,new ResourceModel("username.title"), new ResourceModel("username.summary"));
			add(new RequiredTextField(this,"user.userName"));
			add(new RequiredTextField(this,"user.email").add(EmailAddressPatternValidator.getInstance()));
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
		public UserRolesStep(MarkupContainer<?> parent)
		{
			super(parent,new ResourceModel("userroles.title"), null);
			setSummaryModel(new StringResourceModel("userroles.summary", this, new Model(user)));
			add(new ListMultipleChoice(this,"user.roles", allRoles));
		}

		/**
		 * @see wicket.extensions.wizard.WizardModel.ICondition#evaluate()
		 */
		public boolean evaluate()
		{
			return assignRoles;
		}
	}

	/** cheap ass roles database. */
	private static final List allRoles = Arrays.asList(new String[] { "admin", "user", "moderator",
			"joker", "slacker" });

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
	public NewUserWizard(MarkupContainer parent, String id)
	{
		super(parent,id);

		// create a blank user
		user = new User();

		setModel(new CompoundPropertyModel(this));
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
	 * @see wicket.extensions.wizard.Wizard#onCancel()
	 */
	public void onCancel()
	{
		setResponsePage(Index.class);
	}

	/**
	 * @see wicket.extensions.wizard.Wizard#onFinish()
	 */
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