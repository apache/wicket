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
package org.apache.wicket.examples.repeater;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.validation.validator.StringValidator;

/**
 * Edits the name and the address of a contact, every field required, or enters a new contact,
 * one without an id. Shown in the overlay of the DynamicDataTable, it calls
 * {@link #onSaved(Contact, AjaxRequestTarget)} or {@link #onCancel(AjaxRequestTarget)} when the
 * user is done.
 */
abstract class ContactEditPanel extends Panel
{
	private static final long serialVersionUID = 1L;

	private final long contactId;

	private String firstName;

	private String lastName;

	private String country;

	private String city;

	private String address;

	ContactEditPanel(String id, Contact contact)
	{
		super(id);
		contactId = contact.getId();
		firstName = contact.getFirstName();
		lastName = contact.getLastName();
		country = contact.getCountry();
		city = contact.getCity();
		address = contact.getAddress();

		Form<Void> form = new Form<>("form");
		form.add(AttributeAppender.replace("data-dt-changed", () -> form.hasError() ? "true" : null));
		add(form);
		FeedbackPanel feedback = new FeedbackPanel("feedback");
		feedback.setOutputMarkupId(true);
		form.add(feedback);

		form.add(text("firstName", 60));
		form.add(text("lastName", 60));
		form.add(markInvalid(new DropDownChoice<>("country", new PropertyModel<>(this, "country"),
			countries(), new ChoiceRenderer<>()
			{
				private static final long serialVersionUID = 1L;

				@Override
				public Object getDisplayValue(String code)
				{
					return countryName(code);
				}

				@Override
				public String getIdValue(String code, int index)
				{
					return code;
				}
			}).setRequired(true).setLabel(new ResourceModel("country"))));
		form.add(text("city", 60));
		form.add(text("address", 120));

		form.add(new AjaxLink<Void>("cancel")
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target)
			{
				onCancel(target);
			}
		});
		form.add(new AjaxButton("save")
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(AjaxRequestTarget target)
			{
				ContactsDatabase database = DatabaseLocator.getLargeDatabase();
				Contact contact = contactId == 0 ? new Contact() : database.get(contactId);
				if (contact == null)
				{
					error(getString("contactGone"));
					target.add(feedback);
					return;
				}
				contact.setFirstName(firstName);
				contact.setLastName(lastName);
				contact.setCountry(country);
				contact.setCity(city);
				contact.setAddress(address);
				if (contactId == 0)
				{
					database.save(contact);
				}
				else
				{
					database.update(contact);
				}
				onSaved(contact, target);
			}

			@Override
			protected void onError(AjaxRequestTarget target)
			{
				target.add(form);
			}
		});
	}

	private FormComponent<String> text(String property, int maximumLength)
	{
		TextField<String> field = new TextField<>(property, new PropertyModel<>(this, property));
		field.setRequired(true);
		field.setLabel(new ResourceModel(property));
		field.add(StringValidator.maximumLength(maximumLength));
		return markInvalid(field);
	}

	private static <F extends FormComponent<?>> F markInvalid(F field)
	{
		field.add(AttributeAppender.replace("aria-invalid", () -> field.isValid() ? null : "true"));
		return field;
	}

	private List<String> countries()
	{
		return Arrays.stream(Locale.getISOCountries())
			.sorted(Comparator.comparing(this::countryName))
			.toList();
	}

	private String countryName(String code)
	{
		return Locale.of("", code).getDisplayCountry(getLocale());
	}

	/**
	 * Called after the contact was saved.
	 *
	 * @param contact
	 *            the saved contact
	 * @param target
	 *            the Ajax request target
	 */
	protected abstract void onSaved(Contact contact, AjaxRequestTarget target);

	/**
	 * Called when the user cancels editing.
	 *
	 * @param target
	 *            the Ajax request target
	 */
	protected abstract void onCancel(AjaxRequestTarget target);
}
