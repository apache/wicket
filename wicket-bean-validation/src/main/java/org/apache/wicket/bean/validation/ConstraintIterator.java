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
package org.apache.wicket.bean.validation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.validation.Validator;
import javax.validation.metadata.BeanDescriptor;
import javax.validation.metadata.ConstraintDescriptor;
import javax.validation.metadata.PropertyDescriptor;

/**
 * Iterates constraint descriptors for the specified property.
 * 
 * @author igor
 * 
 */
class ConstraintIterator implements Iterator<ConstraintDescriptor<?>>
{
	private ConstraintDescriptor<?> next;

	private List<Iterator<ConstraintDescriptor<?>>> stack = new ArrayList<>();

	public ConstraintIterator(Validator validator, Property property, Class<?>... groups)
	{
		BeanDescriptor beanDesc = validator.getConstraintsForClass(property.getOwner());
		if (beanDesc != null)
		{
			PropertyDescriptor propDesc = beanDesc.getConstraintsForProperty(property.getName());
			if (propDesc != null)
			{
				// TODO use hasConstraintDesc to optimize...?
				Set<ConstraintDescriptor<?>> constraints = propDesc.findConstraints()
					.unorderedAndMatchingGroups(groups)
					.getConstraintDescriptors();
				if (constraints != null)
				{
					stack.add(constraints.iterator());
					findNext();
				}
			}
		}
	}

	@Override
	public boolean hasNext()
	{
		return next != null;
	}

	private void findNext()
	{
		next = null;
		while (!stack.isEmpty())
		{
			Iterator<ConstraintDescriptor<?>> top = stack.get(stack.size() - 1);
			if (top.hasNext())
			{
				next = top.next();
				Set<ConstraintDescriptor<?>> composing = next.getComposingConstraints();
				if (!composing.isEmpty())
				{
					stack.add(composing.iterator());
				}
				break;
			}
			else
			{
				stack.remove(stack.size() - 1);
			}
		}
	}

	@Override
	public ConstraintDescriptor<?> next()
	{
		ConstraintDescriptor<?> ret = next;
		findNext();
		return ret;
	}

	@Override
	public void remove()
	{
		throw new UnsupportedOperationException();
	}

}
