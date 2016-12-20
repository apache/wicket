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

/**
 * The auto complete builder is used to build the corresponding attribute for form and input tags.
 * To use the auto completion just open the autofill options within your browser. In chrome for
 * example it is accessed with the following URL:
 * <a href="chrome://settings/autofillEditAddress">chrome://settings/autofillEditAddress</a>
 * 
 * @author Tobias Soloschenko
 * 
 * @since 8.0.0
 *
 */
public class AutoCompleteBuilder implements AutoCompleteContactBuilder
{
	/**
	 * The section prefix specificed by the whatwg standard
	 */
	public static final String SECTION_PREFIX = "section-";

	private String sectionName;

	private AutoCompleteAddressType addressType;

	private AutoCompleteFields field;

	private AutoCompleteContact contact;

	private AutoCompleteContactDetails contactDetail;

	private boolean empty;

	/**
	 * Initializes a new auto complete builder
	 * 
	 * @return the auto complete builder
	 */
	public static AutoCompleteBuilder init()
	{
		return new AutoCompleteBuilder();
	}

	/**
	 * Empties out the autocomplete outcome
	 * 
	 * @return the auto complete builder
	 */
	public AutoCompleteBuilder empty()
	{
		this.empty = true;
		return this;
	}

	/**
	 * Applies a section to the auto completion field
	 * 
	 * @param sectionName
	 *            the name of the section
	 * @return the auto complete builder itself
	 */
	public AutoCompleteBuilder withSection(String sectionName)
	{
		this.sectionName = sectionName;
		return this;
	}

	/**
	 * Assigns the auto completion to a specific address type
	 * 
	 * @param addressType
	 *            the auto completion address type
	 * @return the auto complete builder itself
	 */
	public AutoCompleteBuilder forAddressType(AutoCompleteAddressType addressType)
	{
		this.addressType = addressType;
		return this;
	}

	/**
	 * Applies the field to the autocomplete attribute
	 * 
	 * @param field
	 *            the field
	 * @return the auto complete builder
	 */
	public AutoCompleteBuilder forField(AutoCompleteFields field)
	{
		this.field = field;
		return this;
	}

	/**
	 * Applies the contact information to the autocomplete attribute
	 * 
	 * @param contact
	 *            the contact information are going to be applied to
	 * @return the auto complete builder
	 */
	public AutoCompleteContactBuilder forContact(AutoCompleteContact contact)
	{
		this.contact = contact;
		return this;
	}

	/**
	 * @see {@link AutoCompleteContactBuilder}
	 */
	@Override
	public AutoCompleteContactBuilder forField(AutoCompleteContactDetails contactDetail)
	{
		this.contactDetail = contactDetail;
		return (AutoCompleteContactBuilder)this;
	}

	/**
	 * Builds the attribute string
	 * 
	 * @return the attribute content in the right order
	 */
	@Override
	public String toString()
	{
		StringBuilder stringBuilder = new StringBuilder();
		if (!empty)
		{
			if (sectionName != null)
			{
				stringBuilder.append(SECTION_PREFIX);
				stringBuilder.append(sectionName);
				stringBuilder.append(" ");
			}
			if (addressType != null)
			{
				stringBuilder.append(addressType.getValue());
				stringBuilder.append(" ");
			}
			if (field != null)
			{
				stringBuilder.append(field.getValue());
			}
			else
			{
				if (contact != null)
				{
					stringBuilder.append(contact.getValue());
					stringBuilder.append(" ");
				}
				stringBuilder.append(contactDetail.getValue());
			}
		}
		return stringBuilder.toString();
	}
}
