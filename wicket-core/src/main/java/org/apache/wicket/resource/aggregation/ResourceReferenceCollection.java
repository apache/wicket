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
package org.apache.wicket.resource.aggregation;

import java.util.Collection;
import java.util.LinkedHashSet;

/**
 * Just a LinkedHashSet that stores a collection of resource references. You can create additional
 * methods that your header response might need.
 * 
 * @author Jeremy Thomerson
 */
public class ResourceReferenceCollection extends LinkedHashSet<ResourceReferenceAndStringData>
{
	private static final long serialVersionUID = 1L;

	/**
	 * Just chains to super constructor.
	 */
	public ResourceReferenceCollection()
	{
		super();
	}

	/**
	 * Just chains to super constructor.
	 * 
	 * @param c
	 */
	public ResourceReferenceCollection(Collection<? extends ResourceReferenceAndStringData> c)
	{
		super(c);
	}

}
