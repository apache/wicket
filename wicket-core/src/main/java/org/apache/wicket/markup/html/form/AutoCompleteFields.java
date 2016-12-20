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
 * Auto completion personal data according to the whatwg specification.
 * 
 * @author Tobias Soloschenko
 *
 * @see <a href=
 *      "https://html.spec.whatwg.org/multipage/forms.html">https://html.spec.whatwg.org/multipage/forms.html</a>
 * 
 */
public enum AutoCompleteFields {

	/**
	 * Simply turns on the auto completion
	 */
	ON("on"),

	/**
	 * Simply turns off the auto completion
	 */
	OFF("off"),

	/**
	 * Sir Timothy John Berners-Lee, OM, KBE, FRS, FREng, FRSA
	 */
	NAME("name"),

	/**
	 * Sir
	 */
	HONORIFIC_PREFIX("honorific-prefix"),

	/**
	 * Timothy
	 */
	GIVEN_NAME("given-name"),

	/**
	 * John
	 */
	ADDITIONAL_NAME("additional-name"),

	/**
	 * Berners-Lee
	 */
	FAMILY_NAME("family-name"),

	/**
	 * OM, KBE, FRS, FREng, FRSA
	 */
	HONORIFIC_SUFFIX("honorific-suffix"),

	/**
	 * Tim
	 */
	NICKNAME("nickname"),

	/**
	 * timbl
	 */
	USERNAME("username"),

	/**
	 * GUMFXbadyrS3
	 */
	NEW_PASSWORD("new-password"),

	/**
	 * qwerty
	 */
	CURRENT_PASSWORD("current-password"),

	/**
	 * Professor
	 */
	ORGANIZATION_TITLE("organization-title"),

	/**
	 * World Wide Web Consortium
	 */
	ORGANIZATION("organization"),

	/**
	 * Multiple lines 32 Vassar Street MIT Room 32-G524
	 */
	STREET_ADDRESS("street-address"),

	/**
	 * 32 Vassar Street
	 */
	ADDRESS_LINE1("address-line1"),

	/**
	 * MIT Room 32-G524
	 */
	ADDRESS_LINE2("address-line2"),

	/**
	 * See {@link AutoComplete.ADRESS_LINE2}
	 */
	ADDRESS_LINE3("address-line3"),

	/**
	 * The most fine-grained administrative level, in addresses with four administrative levels
	 */
	ADDRESS_LEVEL4("address-level4"),

	/**
	 * The third administrative level, in addresses with three or more administrative levels
	 */
	ADDRESS_LEVEL3("address-level3"),

	/**
	 * Cambridge
	 */
	ADDRESS_LEVEL2("address-level2"),

	/**
	 * MA
	 */
	ADDRESS_LEVEL1("address-level1"),

	/**
	 * US
	 */
	COUNTRY("country"),

	/**
	 * US
	 */
	COUNTRY_NAME("country-name"),

	/**
	 * 02139
	 */
	POSTAL_CODE("postal-code"),

	/**
	 * Tim Berners-Lee
	 */
	CC_NAME("cc-name"),

	/**
	 * Tim
	 */
	CC_GIVEN_NAME("cc-given-name"),

	/**
	 * -
	 */
	CC_ADDITIONAL_NAME("cc-additional-name"),

	/**
	 * Berners-Lee
	 */
	CC_FAMILY_NAME("cc-family-name"),

	/**
	 * 4114360123456785
	 */
	CC_NUMBER("cc-number"),

	/**
	 * 2014-12
	 */
	CC_EXP("cc-exp"),

	/**
	 * 12
	 */
	CC_EXP_MONTH("cc-exp-month"),

	/**
	 * 2014
	 */
	CC_EXP_YEAR("cc-exp-year"),

	/**
	 * 419
	 */
	CC_CSC("cc-csc"),

	/**
	 * Visa
	 */
	CC_TYPE("cc-type"),

	/**
	 * GBP
	 */
	TRANSACTION_CURRENCY("transaction-currency"),

	/**
	 * 401.00
	 */
	TRANSACTION_AMOUNT("transaction-amount"),

	/**
	 * en
	 */
	LANGUAGE("language"),

	/**
	 * 1955-06-08
	 */
	BDAY("bday"),

	/**
	 * 8
	 */
	BDAY_DAY("bday-day"),

	/**
	 * 6
	 */
	BDAY_MONTH("bday-month"),

	/**
	 * 1955
	 */
	BDAY_YEAR("bday-year"),

	/**
	 * Male
	 */
	SEX("sex"),

	/**
	 * https://www.w3.org/People/Berners-Lee/
	 */
	URL("url"),

	/**
	 * https://www.w3.org/Press/Stock/Berners-Lee/2001-europaeum-eighth.jpg
	 */
	PHOTO("photo");

	private String value;

	private AutoCompleteFields(String value)
	{
		this.value = value;
	}

	/**
	 * Gets the value of the auto completion
	 * 
	 * @return the value of the auto completion
	 */
	public String getValue()
	{
		return value;
	}

}
