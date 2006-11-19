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
package wicket.markup.html.resources;

import wicket.Application;
import wicket.AttributeModifier;
import wicket.Component;
import wicket.ResourceReference;
import wicket.markup.html.PackageResourceReference;
import wicket.markup.html.WebMarkupContainer;
import wicket.model.IModel;
import wicket.model.Model;

/**
 * Base class for components that render references to packaged resources.
 * 
 * @author Eelco Hillenius
 */
public class PackagedResourceReference extends WebMarkupContainer
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 * 
	 * @param id
	 *            component id
	 * @param referer
	 *            the class that is refering; is used as the relative root for
	 *            gettting the resource
	 * @param file
	 *            relative location of the packaged file
	 * @param attributeToReplace
	 *            the attribute to replace of the target tag
	 */
	public PackagedResourceReference(final String id, final Class referer, final String file,
			final String attributeToReplace)
	{
		this(id, referer, new Model(file), attributeToReplace);
	}

	/**
	 * Construct.
	 * 
	 * @param id
	 *            component id
	 * @param referer
	 *            the class that is refering; is used as the relative root for
	 *            gettting the resource
	 * @param file
	 *            model that supplies the relative location of the packaged
	 *            file. Must return an instance of {@link String}
	 * @param attributeToReplace
	 *            the attribute to replace of the target tag
	 */
	public PackagedResourceReference(final String id, final Class referer, final IModel file,
			final String attributeToReplace)
	{
		super(id);

		if (referer == null)
		{
			throw new IllegalArgumentException("Referer may not be null");
		}
		if (file == null)
		{
			throw new IllegalArgumentException("File may not be null");
		}
		if (attributeToReplace == null)
		{
			throw new IllegalArgumentException("AttributeToReplace may not be null");
		}

		IModel srcReplacement = new Model()
		{
			private static final long serialVersionUID = 1L;

			public Object getObject(Component component)
			{
				Object o = file.getObject(component);
				if (o == null)
				{
					throw new IllegalArgumentException(
							"The model must provide a non-null object (component == " + component
									+ ")");
				}
				if (!(o instanceof String))
				{
					throw new IllegalArgumentException(
							"The model must provide an instance of String");
				}
				String f = (String)component.getConverter().convert(file.getObject(component),
						String.class);
				ResourceReference ref = createPackageResourceReference(Application.get(), referer,
						f);
				return getRequestCycle().urlFor(ref);
			}
		};
		add(new AttributeModifier(attributeToReplace, true, srcReplacement));
	}

	/**
	 * Creates new package resource reference.
	 * 
	 * @param app
	 * @param scope
	 * @param name
	 * @return created resource reference
	 */
	protected PackageResourceReference createPackageResourceReference(Application app, Class scope,
			String name)
	{
		return new PackageResourceReference(app, scope, name);
	}

	/**
	 * Construct.
	 * 
	 * @param id
	 *            component id
	 * @param resourceReference
	 *            the reference to the resource
	 * @param attributeToReplace
	 *            the attribute to replace of the target tag
	 */
	public PackagedResourceReference(final String id, final ResourceReference resourceReference,
			final String attributeToReplace)
	{
		this(id, new Model(resourceReference), attributeToReplace);
	}

	/**
	 * Construct.
	 * 
	 * @param id
	 *            component id
	 * @param resourceReference
	 *            the reference to the resource. Must return an instance of
	 *            {@link ResourceReference}
	 * @param attributeToReplace
	 *            the attribute to replace of the target tag
	 */
	public PackagedResourceReference(final String id, final IModel resourceReference,
			final String attributeToReplace)
	{
		super(id);

		if (resourceReference == null)
		{
			throw new IllegalArgumentException("ResourceReference may not be null");
		}
		if (attributeToReplace == null)
		{
			throw new IllegalArgumentException("AttributeToReplace may not be null");
		}

		IModel srcReplacement = new Model()
		{
			private static final long serialVersionUID = 1L;

			public Object getObject(Component component)
			{
				Object o = resourceReference.getObject(component);
				if (o == null)
				{
					throw new IllegalArgumentException(
							"The model must provide a non-null object (component == " + component
									+ ")");
				}
				if (!(o instanceof ResourceReference))
				{
					throw new IllegalArgumentException(
							"The model must provide an instance of ResourceReference");
				}

				ResourceReference ref = (ResourceReference)o;
				return urlFor(ref);
			}
		};
		add(new AttributeModifier(attributeToReplace, true, srcReplacement));
	}
}
