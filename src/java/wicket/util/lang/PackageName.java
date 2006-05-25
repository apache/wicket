/*
 * $Id: PackageName.java 5791 2006-05-20 00:32:57 +0000 (Sat, 20 May 2006)
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
package wicket.util.lang;

/**
 * A type-safe package name class since Package is unreliable (it's not a
 * Package object, but rather a sometimes-available holder for versioning
 * information).
 * 
 * @author Jonathan Locke
 */
public class PackageName
{
	// The name of this package
	private final String name;

	/**
	 * @param c
	 *            The class to get a PackageName object for
	 * @return The PackageName object
	 */
	public static PackageName forClass(final Class c)
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

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that)
	{
		if (that instanceof PackageName)
		{
			return ((PackageName)that).name.equals(this.name);
		}
		return false;
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
