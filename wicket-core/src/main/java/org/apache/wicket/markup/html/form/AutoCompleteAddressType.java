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
 * Auto completion address type according to the whatwg specification.
 * 
 * @author Tobias Soloschenko
 *
 * @see <a href=
 *      "https://html.spec.whatwg.org/multipage/forms.html">https://html.spec.whatwg.org/multipage/forms.html</a>
 * 
 */
public enum AutoCompleteAddressType {

	/**
	 * Meaning the field is part of the shipping address or contact information
	 */
	SHIPPING("shipping"),

	/**
	 * meaning the field is part of the billing address or contact information
	 */
	BILLING("billing");

	private String value;

	private AutoCompleteAddressType(String value)
	{
		this.value = value;
	}

	/**
	 * Gets the address type value
	 * 
	 * @return the value of the address type
	 */
	public String getValue()
	{
		return value;
	}
}
