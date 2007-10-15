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
package org.apache.wicket.validation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A compound {@link IValidator}. Once an error is reported against the {@link IValidatable} being
 * checked, the rest of the validator chain is ignored.
 * 
 * @author Igor Vaynberg (ivaynberg)
 * @since 1.2.6
 */
public class CompoundValidator implements IValidator
{
	private static final long serialVersionUID = 1L;

	private final List validators = new ArrayList(2);

	/**
	 * Constructor.
	 */
	public CompoundValidator()
	{
	}

	/**
	 * Adds an <code>IValidator</code> to the chain of validators.
	 * 
	 * @param validator
	 *            an <code>IValidator</code> to be added
	 * @return this <code>ValidationError</code> for chaining purposes
	 */
	public final CompoundValidator add(IValidator validator)
	{
		if (validator == null)
		{
			throw new IllegalArgumentException("Argument `validator` cannot be null");
		}
		validators.add(validator);
		return this;
	}

	/**
	 * @see IValidator#validate(IValidatable)
	 */
	public final void validate(IValidatable validatable)
	{
		Iterator it = validators.iterator();
		while (it.hasNext() && validatable.isValid())
		{
			((IValidator)it.next()).validate(validatable);
		}
	}
}
