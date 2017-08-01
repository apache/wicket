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
package org.apache.wicket.http2.markup.head;

/**
 * A push header to be applied when the resource is pushed
 *
 * @author Tobias Soloschenko
 *
 */
public class PushItemHeaderValue
{
	/**
	 * The header operation to be used
	 *
	 * @author Tobias Soloschenko
	 *
	 */
	public enum HeaderOperation {
		/**
		 * Header value is going to be set
		 */
		SET,
		/**
		 * Header value is going to be add
		 */
		ADD
	}

	private String value;

	private HeaderOperation operation;

	/**
	 * @param value
	 *            the value of the header
	 * @param operation
	 *            the header operation
	 */
	public PushItemHeaderValue(String value, HeaderOperation operation)
	{
		this.value = value;
		this.operation = operation;
	}

	/**
	 * The value of the header
	 *
	 * @return the value of the header
	 */
	public String getValue()
	{
		return value;
	}

	/**
	 * Sets the value of the header
	 *
	 * @param value
	 *            the value of the header
	 */
	public void setValue(String value)
	{
		this.value = value;
	}

	/**
	 * Gets the header operation
	 *
	 * @return the header operation
	 */
	public HeaderOperation getOperation()
	{
		return operation;
	}

	/**
	 * Sets the header operation
	 *
	 * @param operation
	 *            the header operation
	 */
	public void setOperation(HeaderOperation operation)
	{
		this.operation = operation;
	}
}
