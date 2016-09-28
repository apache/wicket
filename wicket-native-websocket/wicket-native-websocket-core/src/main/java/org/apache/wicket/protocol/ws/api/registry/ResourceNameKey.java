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
package org.apache.wicket.protocol.ws.api.registry;

import org.apache.wicket.util.lang.Args;

/**
 * A key based on shared resource's name
 */
public class ResourceNameKey implements IKey
{
	private final String resourceName;

	public ResourceNameKey(String resourceName)
	{
		this.resourceName = Args.notNull(resourceName, "resourceName");
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		ResourceNameKey that = (ResourceNameKey) o;

		if (!resourceName.equals(that.resourceName)) return false;

		return true;
	}

	@Override
	public int hashCode()
	{
		return resourceName.hashCode();
	}
}
