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
package org.apache.wicket.request.resource;

import org.apache.wicket.Application;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.resource.ResourceUtils;

/**
 * This is a ResourceReference to handle context-relative resources such as js, css and 
 * picture files placed in a folder on the context root (ex: '/css/coolTheme.css'). 
 * The class has a flag (see {@link #isMinifyIt()}) to decide if referenced resource can be 
 * minified (ex: '/css/coolTheme.min.css') or not.
 *
 * @author Andrea Del Bene
 */
public class ContextRelativeResourceReference extends ResourceReference
{
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	/** Says if the resource name can be minified or not. */
	private final boolean minifyIt;
	
	/** The minfied postfix. */
	private final String minPostfix;	
	
	/** The context relative resource. */
	private final ContextRelativeResource contextRelativeResource;
	
	/**
	 * Instantiates a new context relative resource reference for the given name. The resource
	 * will be minified in DEPLOYMENT mode and "min" will be used as postfix.
	 * 
	 * @param name
	 * 				the resource name
	 */
	public ContextRelativeResourceReference(final String name)
	{
		this(name, ResourceUtils.MIN_POSTFIX_DEFAULT, true);
	}
	
	
	/**
	 * Instantiates a new context relative resource reference for the given name.
	 * Parameter {@code minifyIt} says if the resource can be minified (true) or not (false). 
	 *
	 * @param name 
	 * 				the resource name
	 * @param minifyIt 
	 * 				says if the resource name can be minified or not
	 */
	public ContextRelativeResourceReference(final String name, final boolean minifyIt)
	{
		this(name, ResourceUtils.MIN_POSTFIX_DEFAULT, minifyIt);
	}
	
	/**
	 * Instantiates a new context relative resource reference for the given name.  We can
	 * specify which postfix we want to use for minification with parameter @code minPostfix}
	 * 
	 * @param name
	 * 				the resource name
	 * @param minPostfix
	 *  			the minfied postfix
	 */
	public ContextRelativeResourceReference(final String name, final String minPostfix)
	{
		this(name, minPostfix, true);
	}
	
	/**
	 * Instantiates a new context relative resource reference for the given name. We can
	 * specify which postfix we want to use for minification with parameter @code minPostfix}
	 * while parameter {@code minifyIt} says if the resource can be minified (true) or not (false). 
	 * @param name 
	 * 				the resource name
	 * @param minPostfix 
	 * 				the minfied postfix
	 * @param minifyIt 
	 * 				says if the resource name can be minified or not
	 */
	public ContextRelativeResourceReference(final String name, final String minPostfix, final boolean minifyIt)
	{
		super(name);
		
		Args.notNull(minPostfix, "minPostfix");
		
		this.minPostfix = minPostfix;
		this.minifyIt = minifyIt;
		this.contextRelativeResource = buildContextRelativeResource(name, minPostfix);
	}
	
	/**
	 * Build the context-relative resource for this resource reference.
	 * 
	 * @param name
	 * 				the resource name
	 * @param minPostfix
	 * 				the postfix to use to minify the resource name (typically "min")
	 * @return the context-relative resource 
	 */
	protected ContextRelativeResource buildContextRelativeResource(final String name, final String minPostfix)
	{
		String minifiedName = name;
		
		if (canBeMinified()) 
		{
			minifiedName = ResourceUtils.getMinifiedName(name, minPostfix);
		}
		
		return new ContextRelativeResource(minifiedName);
	}

	/**
	 * Says if the referenced resource can be minified. It returns {@code true} if 
	 * both flag {@link #minifyIt} and application's resource settings method
	 * {@link org.apache.wicket.settings.ResourceSettings#getUseMinifiedResources()}} 
	 * are true.
	 * 
	 * @return {@code true} if resource can be minified, {@code false} otherwise
	 */
	protected boolean canBeMinified()
	{
		return minifyIt && Application.exists()
            && Application.get().getResourceSettings().getUseMinifiedResources();
	}

	/* (non-Javadoc)
	 * @see org.apache.wicket.request.resource.ResourceReference#getResource()
	 */
	@Override
	public final ContextRelativeResource getResource()
	{
		return contextRelativeResource;
	}
	
	/**
	 * Returns the flag that says if the resource can be minified (true) or not (false).
	 *
	 * @return true, if resource can be minified
	 */
	public final boolean isMinifyIt()
	{
		return minifyIt;
	}
	
	
	/**
	 * Gets the minified postfix we use for this resource.
	 *
	 * @return the minified postfix
	 */
	public final String getMinPostfix()
	{
		return minPostfix;
	}
}
