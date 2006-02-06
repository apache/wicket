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
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import wicket.util.io.ByteArrayOutputStream;

public class Objects2
{
	private Objects2()
	{
	}

	/**
	 * De-serializes an object from a byte array.
	 * 
	 * @param data
	 *            The serialized object
	 * @return The object
	 */
	// FIXME General: Belongs in Objects.java
	public static Object byteArrayToObject(final byte[] data)
	{
		try
		{
			final ByteArrayInputStream in = new ByteArrayInputStream(data);
			try
			{
				return new ObjectInputStream(in).readObject();				
			}
			finally
			{
				in.close();
			}
		}
		catch (ClassNotFoundException e)
		{
			e.printStackTrace();
			return null;
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Serializes an object into a byte array.
	 * 
	 * @param object
	 *            The object
	 * @return The serialized object
	 */
	// FIXME General: Belongs in Objects.java
	public static byte[] objectToByteArray(Object object)
	{
		try
		{
			final ByteArrayOutputStream out = new ByteArrayOutputStream();
			try
			{
				new ObjectOutputStream(out).writeObject(object);
			}
			finally
			{
				out.close();
			}
			return out.toByteArray();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return null;
		}
	}
}
