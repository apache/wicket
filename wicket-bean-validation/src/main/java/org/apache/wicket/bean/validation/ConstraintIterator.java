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

	private List<Iterator<ConstraintDescriptor<?>>> stack = new ArrayList<Iterator<ConstraintDescriptor<?>>>();

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
