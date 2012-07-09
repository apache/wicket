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
package org.apache.wicket.settings.def;

import java.util.List;

import org.apache.wicket.markup.resolver.IComponentResolver;
import org.apache.wicket.settings.IPageSettings;
import org.apache.wicket.util.lang.Generics;

/**
 * @author Jonathan Locke
 * @author Chris Turner
 * @author Eelco Hillenius
 * @author Juergen Donnerstag
 * @author Johan Compagner
 * @author Igor Vaynberg (ivaynberg)
 * @author Martijn Dashorst
 * @author James Carman
 */
public class PageSettings implements IPageSettings
{
	/** List of (static) ComponentResolvers */
	private final List<IComponentResolver> componentResolvers = Generics.newArrayList();

	/** Determines if pages should be managed by a version manager by default */
	private boolean versionPagesByDefault = true;

	/** determines if mounted pages should be recreated after expiry */
	private boolean recreateMountedPagesAfterExpiry = true;

	/**
	 * @see org.apache.wicket.settings.IPageSettings#addComponentResolver(org.apache.wicket.markup.resolver.IComponentResolver)
	 */
	@Override
	public void addComponentResolver(IComponentResolver resolver)
	{
		componentResolvers.add(resolver);
	}

	/**
	 * @see org.apache.wicket.settings.IPageSettings#getComponentResolvers()
	 */
	@Override
	public List<IComponentResolver> getComponentResolvers()
	{
		return componentResolvers;
	}

	/**
	 * @see org.apache.wicket.settings.IPageSettings#getVersionPagesByDefault()
	 */
	@Override
	public boolean getVersionPagesByDefault()
	{
		return versionPagesByDefault;
	}

	/**
	 * @see org.apache.wicket.settings.IPageSettings#setVersionPagesByDefault(boolean)
	 */
	@Override
	public void setVersionPagesByDefault(boolean pagesVersionedByDefault)
	{
		versionPagesByDefault = pagesVersionedByDefault;
	}

	@Override
	public boolean getRecreateMountedPagesAfterExpiry()
	{
		return recreateMountedPagesAfterExpiry;
	}

	@Override
	public void setRecreateMountedPagesAfterExpiry(boolean recreateMountedPagesAfterExpiry)
	{
		this.recreateMountedPagesAfterExpiry = recreateMountedPagesAfterExpiry;
	}
}
