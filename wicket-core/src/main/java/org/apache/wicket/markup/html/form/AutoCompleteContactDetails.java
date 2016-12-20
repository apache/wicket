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
 * Auto completion contact detail according to the whatwg specification.
 * 
 * @author Tobias Soloschenko
 *
 * @see <a href=
 *      "https://html.spec.whatwg.org/multipage/forms.html">https://html.spec.whatwg.org/multipage/forms.html</a>
 * 
 */
public enum AutoCompleteContactDetails {

	/**
	 * +1 617 253 5702
	 */
	TEL("tel"),

	/**
	 * +1
	 */
	TEL_COUNTRY_CODE("tel-country-code"),

	/**
	 * 617 253 5702
	 */
	TEL_NATIONAL("tel-national"),

	/**
	 * 617
	 */
	TEL_AREA_CODE("tel-area-code"),

	/**
	 * 2535702
	 */
	TEL_LOCAL("tel-local"),

	/**
	 * 253
	 */
	TEL_LOCAL_PREFIX("tel-local-prefix"),

	/**
	 * 5702
	 */
	TEL_LOCAL_SUFFIX("tel-local-suffix"),

	/**
	 * 1000
	 */
	TEL_EXTENSION("tel-extension"),

	/**
	 * timbl@w3.org
	 */
	EMAIL("email"),

	/**
	 * irc://example.org/timbl,isuser
	 */
	IMPP("impp");
	
	private String value;
	
	/**
	 * Creates an auto completion contact detail with the given value
	 * 
	 * @param value
	 *            the value of the contact detail
	 */
	private AutoCompleteContactDetails(String value)
	{
		this.value = value;
	}

	/**
	 * Gets the value of the auto completion contact detail
	 * 
	 * @return the value of the auto completion contact detail
	 */
	public String getValue()
	{
		return value;
	}
}
