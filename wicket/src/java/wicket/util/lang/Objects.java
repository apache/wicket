/*
 * $Id$ $Revision:
 * 1.5 $ $Date$
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Object utilities.
 * 
 * @author Jonathan Locke
 */
public abstract class Objects
{
	/**
	 * Instantiation not allowed
	 */
	private Objects()
	{
	}

	/**
	 * Returns true if a and b are equal. Either object may be null.
	 * 
	 * @param a
	 *            Object a
	 * @param b
	 *            Object b
	 * @return True if the objects are equal
	 */
	public static boolean equal(final Object a, final Object b)
	{
		if (a == b)
		{
			return true;
		}

		if (a != null && b != null && a.equals(b))
		{
			return true;
		}

		return false;
	}

	/**
	 * Makes a deep clone of an object by serializing and deserializing it. The
	 * object must be fully serializable to be cloned.
	 * 
	 * @param object
	 *            The object to clone
	 * @return A deep copy of the object
	 */
	public static Object clone(final Object object)
	{
		if (object == null)
		{
			return null;
		}
		else
		{
			try
			{
				final ByteArrayOutputStream out = new ByteArrayOutputStream();
				new ObjectOutputStream(out).writeObject(object);
				return new ObjectInputStream(new ByteArrayInputStream(out.toByteArray()))
						.readObject();
			}
			catch (ClassNotFoundException e)
			{
				throw new RuntimeException("Internal error cloning object", e);
			}
			catch (IOException e)
			{
				throw new RuntimeException("Internal error cloning object", e);
			}
		}
	}
}
