/*
 * $Id: CompoundPropertyModel.java 5861 2006-05-25 20:55:07 +0000 (Thu, 25 May
 * 2006) eelco12 $ $Revision$ $Date: 2006-05-25 20:55:07 +0000 (Thu, 25
 * May 2006) $
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.model;

import wicket.Component;

/**
 * A simple compound model which uses the component's name as the property
 * expression to retrieve properties on the nested model object.
 * 
 * @see wicket.model.IModel
 * @see wicket.model.Model
 * @see wicket.model.AbstractDetachableModel
 * 
 * @param <T>
 *            Type of model object this model holds
 * 
 * @author Jonathan Locke
 * @author Igor Vaynberg (ivaynberg)
 */
public class CompoundPropertyModel<T> implements IInheritableModel<T>
{
	private static final long serialVersionUID = 1L;

	private Object target;

	/**
	 * Constructor
	 * 
	 * @param target
	 *            target object against which property expressions will be
	 *            evauluated, can be an {@link IModel} for chaining
	 */
	public CompoundPropertyModel(Object target)
	{
		this.target = target;
	}

	/**
	 * @see wicket.model.IModel#getObject()
	 */
	@SuppressWarnings("unchecked")
	public T getObject()
	{
		if (target instanceof IModel)
		{
			return (T)((IModel)target).getObject();
		}
		return (T)target;
	}

	/**
	 * @see wicket.model.IModel#setObject(java.lang.Object)
	 */
	public void setObject(T object)
	{
		this.target = object;
	}

	/**
	 * @see wicket.model.IDetachable#detach()
	 */
	public void detach()
	{
		if (target != null && target instanceof IModel)
		{
			((IModel)target).detach();
		}
	}

	/**
	 * Returns the property expression that should be used against the target
	 * object
	 * 
	 * @param component
	 * @return property expression that should be used against the target object
	 */
	protected String propertyExpression(Component component)
	{
		return component.getId();
	}

	/**
	 * @see wicket.model.IInheritableModel#wrapOnInheritance(wicket.Component)
	 */
	@SuppressWarnings("unchecked")
	public <C> IWrapModel<C> wrapOnInheritance(Component<C> component)
	{
		return new AttachedCompoundPropertyModel(component);
	}

	/**
	 * Component aware variation of the {@link CompoundPropertyModel} that
	 * components that inherit the model get
	 * 
	 * @author ivaynberg
	 */
	private class AttachedCompoundPropertyModel extends AbstractPropertyModel
			implements
				IWrapModel,
				IInheritableModel,
				IAssignmentAware
	{
		private static final long serialVersionUID = 1L;

		private final Component owner;

		/**
		 * Constructor
		 * 
		 * @param owner
		 *            component that this model has been attached to
		 */
		public AttachedCompoundPropertyModel(Component owner)
		{
			super(CompoundPropertyModel.this);
			this.owner = owner;
		}

		/**
		 * @see wicket.model.AbstractPropertyModel#propertyExpression()
		 */
		@Override
		protected String propertyExpression()
		{
			return CompoundPropertyModel.this.propertyExpression(owner);
		}

		/**
		 * @see wicket.model.IWrapModel#getNestedModel()
		 */
		public IModel getNestedModel()
		{
			return CompoundPropertyModel.this;
		}

		/**
		 * @see wicket.model.IAssignmentAware#wrapOnAssignment(wicket.Component)
		 */
		public IWrapModel wrapOnAssignment(Component component)
		{
			return new AttachedCompoundPropertyModel(component);
		}

		/**
		 * @see wicket.model.IInheritableModel#wrapOnInheritance(wicket.Component)
		 */
		public IWrapModel wrapOnInheritance(Component component)
		{
			return new AttachedCompoundPropertyModel(component);
		};

		/**
		 * @see wicket.model.AbstractPropertyModel#detach()
		 */
		@Override
		public void detach()
		{
			super.detach();
			CompoundPropertyModel.this.detach();
		}

	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return new StringBuilder().append("Model:classname=[" + getClass().getName() + "]")
				.toString();
	}

}
