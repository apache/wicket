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
package org.apache.wicket.util.lang;

import org.apache.wicket.util.io.IClusterable;

/**
 * A type-safe package name class since Package is unreliable (it's not a Package object, but rather
 * a sometimes-available holder for versioning information).
 * 
 * @author Jonathan Locke
 */
public class PackageName implements IClusterable
{
	private static final long serialVersionUID = 1L;

	// The name of this package
	private final String name;

	/**
	 * @param c
	 *            The class to get a PackageName object for
	 * @return The PackageName object
	 */
	public static PackageName forClass(final Class<?> c)
	{
		return new PackageName(Packages.extractPackageName(c));
	}

	/**
	 * @param p
	 *            The package to get a PackageName object for
	 * @return The package name
	 */
	public static PackageName forPackage(final Package p)
	{
		return new PackageName(p.getName());
	}

	/**
	 * Constructor
	 * 
	 * @param name
	 *            The name of this package
	 */
	private PackageName(final String name)
	{
		this.name = name;
	}

	@Override
	public boolean equals(final Object that)
	{
		if (that instanceof PackageName)
		{
			return ((PackageName)that).name.equals(name);
		}
		return false;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	/**
	 * @return The fully qualified name of this package
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return name;
	}
}
