/*
 * $Id: ResourceModel.java 5791 2006-05-20 00:32:57 +0000 (Sat, 20 May 2006)
 * joco01 $ $Revision$ $Date: 2006-05-20 00:32:57 +0000 (Sat, 20 May
 * 2006) $
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

import wicket.Application;
import wicket.Component;

/**
 * A model that represents a localized resource string. This is a lightweight
 * version of the {@link StringResourceModel}. It lacks parameter
 * substitutions, but is generaly easier to use.
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public class ResourceModel extends AbstractReadOnlyModel<String>
		implements
			IAssignmentAware<String>
{
	private static final long serialVersionUID = 1L;

	private String defaultValue;

	private String resourceKey;

	private Component component;

	void setComponent(Component c)
	{
		component = c;
	}

	/**
	 * Constructor
	 * 
	 * @param resourceKey
	 *            key of the resource this model represents
	 */
	public ResourceModel(String resourceKey)
	{
		this(resourceKey, null);
	}

	/**
	 * Constructor
	 * 
	 * @param resourceKey
	 *            key of the resource this model represents
	 * @param defaultValue
	 *            value that will be returned if resource does not exist
	 * 
	 */
	public ResourceModel(String resourceKey, String defaultValue)
	{
		this.resourceKey = resourceKey;
		this.defaultValue = defaultValue;
	}

	/**
	 * @see wicket.model.AbstractReadOnlyModel#getObject()
	 */
	@Override
	public String getObject()
	{
		return Application.get().getResourceSettings().getLocalizer().getString(resourceKey,
				component, defaultValue);
	}


	/**
	 * @see wicket.model.IAssignmentAware#wrapOnAssignment(wicket.Component)
	 */
	@SuppressWarnings("unchecked")
	public IWrapModel<String> wrapOnAssignment(final Component component)
	{
		return new AssignmentWrapper(resourceKey, defaultValue, component);
	}

	private class AssignmentWrapper extends ResourceModel implements IWrapModel<String>
	{

		private static final long serialVersionUID = 1L;

		/**
		 * Construct.
		 * 
		 * @param resourceKey
		 * @param defaultValue
		 * @param component
		 */
		public AssignmentWrapper(String resourceKey, String defaultValue, Component component)
		{
			super(resourceKey, defaultValue);
			setComponent(component);
		}

		public IModel getNestedModel()
		{
			return ResourceModel.this;
		}
	}

}
