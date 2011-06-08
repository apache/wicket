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
package org.apache.wicket.serialize.java;

import org.apache.wicket.application.IClassResolver;
import org.apache.wicket.serialize.ISerializer;
import org.apache.wicket.util.io.IObjectStreamFactory;
import org.apache.wicket.util.lang.WicketObjects;

/**
 * An implementation of {@link ISerializer} based on Java Serialization (ObjectOutputStream,
 * ObjectInputStream)
 * 
 * @see IObjectStreamFactory
 */
public class JavaSerializer implements ISerializer
{
	/**
	 * The key of the application which can be used later to find the proper {@link IClassResolver}
	 */
	private final String applicationKey;

	/**
	 * Construct.
	 * 
	 * @param applicationKey
	 */
	public JavaSerializer(final String applicationKey)
	{
		this.applicationKey = applicationKey;
	}

	public byte[] serialize(final Object page)
	{
		return WicketObjects.objectToByteArray(page, applicationKey);
	}

	public Object deserialize(final byte[] data)
	{
		return WicketObjects.byteArrayToObject(data);
	}

}
