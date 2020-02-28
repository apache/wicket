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
package org.apache.wicket.markup.head;

/**
 * Abstract base class for all {@link HeaderItem}s that have a <em>nonce</em> to
 * allow the browser to check <em>Content Security Policye</em> (CSP) .
 * 
 * @author svenmeier
 */
public abstract class AbstractCspHeaderItem extends HeaderItem
{
	private static final long serialVersionUID = 1L;

	/**
	 * CSP Nonce
	 */
	private String nonce;

	/**
	 * @return CSP nonce
	 */
	public String getNonce()
	{
		return nonce;
	}

	/**
	 * Set the CSP nonce
	 * 
	 * @param nonce
	 * @return {@code this} object, for method chaining
	 */
	public AbstractCspHeaderItem setNonce(String nonce)
	{
		this.nonce = nonce;
		return this;
	}
}