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
package org.apache.wicket;


import org.apache.wicket.util.io.IClusterable;

/**
 * A key to a piece of metadata associated with a {@link Component}, {@link Session} or
 * {@link Application} at runtime. The key contains type information that can be used to check the
 * type of any metadata value for the key when the value is set. MetaDataKey is abstract in order to
 * force the creation of a subtype. That subtype is used to test for identity when looking for the
 * metadata because actual object identity would suffer from problems under serialization. So, the
 * correct way to declare a MetaDataKey is like this:
 * 
 * <pre>
 * <code>
 * public static MetaDataKey&lt;Role&gt; ROLE = new MetaDataKey&lt;Role&gt;() { };
 * </code>
 * </pre>
 * 
 * @author Jonathan Locke
 * 
 * @param <T>
 *            The type of the object which is stored
 * 
 * @see Session#getMetaData(MetaDataKey)
 * @see Component#getMetaData(MetaDataKey)
 * @see Application#getMetaData(MetaDataKey)
 */
public abstract class MetaDataKey<T> implements IClusterable
{
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor.
	 */
	public MetaDataKey()
	{
	}

	@Override
	public int hashCode()
	{
		return getClass().hashCode();
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj)
	{
		return obj != null && getClass().equals(obj.getClass());
	}

	/**
	 * @param metaData
	 *            Array of metadata to search
	 * @return The entry value
	 */
	@SuppressWarnings("unchecked")
	public T get(MetaDataEntry<?>[] metaData)
	{
		if (metaData != null)
		{
			for (MetaDataEntry<?> entry : metaData)
			{
				if (equals(entry.key))
				{
					return (T)entry.object;
				}
			}
		}
		return null;
	}

	/**
	 * @param metaData
	 *            The array of metadata
	 * @param object
	 *            The object to set, null to remove
	 * @return Any new metadata array (if it was reallocated)
	 */
	public MetaDataEntry<?>[] set(MetaDataEntry<?>[] metaData, final Object object)
	{
		boolean set = false;
		if (metaData != null)
		{
			for (int i = 0; i < metaData.length; i++)
			{
				MetaDataEntry<?> m = metaData[i];
				if (equals(m.key))
				{
					if (object != null)
					{
						// set new value
						m.object = object;
					}
					else
					{
						// remove value and shrink or null array
						if (metaData.length > 1)
						{
							int l = metaData.length - 1;
							MetaDataEntry<?>[] newMetaData = new MetaDataEntry[l];
							System.arraycopy(metaData, 0, newMetaData, 0, i);
							System.arraycopy(metaData, i + 1, newMetaData, i, l - i);
							metaData = newMetaData;
						}
						else
						{
							metaData = null;
						}
					}
					set = true;
					break;
				}
			}
		}
		if (!set && object != null)
		{
			MetaDataEntry<T> m = new MetaDataEntry<T>(this, object);
			if (metaData == null)
			{
				metaData = new MetaDataEntry[1];
				metaData[0] = m;
			}
			else
			{
				final MetaDataEntry<?>[] newMetaData = new MetaDataEntry[metaData.length + 1];
				System.arraycopy(metaData, 0, newMetaData, 0, metaData.length);
				newMetaData[metaData.length] = m;
				metaData = newMetaData;
			}
		}
		return metaData;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return getClass().toString();
	}
}
