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

import java.io.Serializable;

import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.reference.ClassReference;

/**
 * A reference to a property that can be validated.
 * 
 * @author igor
 */
public final class Property implements Serializable
{
	private final ClassReference<?> owner;
	private final String name;

	public Property(ClassReference<?> owner, String name)
	{
		Args.notNull(owner, "owner");
		Args.notEmpty(name, "name");

		this.owner = owner;
		this.name = name;
	}

	public Property(Class<?> owner, String name)
	{
		this(ClassReference.of(owner), name);
	}

	public Class<?> getOwner()
	{
		return owner.get();
	}

	public String getName()
	{
		return name;
	}
}
