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

import org.apache.wicket.request.Response;

/**
 * An {@link IResource} for byte arrays. The byte array can be static - passed to the constructor,
 * or dynamic - by overriding
 * {@link #getData(org.apache.wicket.request.resource.IResource.Attributes)}
 * 
 * @author Matej Knopp
 */
public class ByteArrayResource extends BaseDataResource<byte[]>
{
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a {@link ByteArrayResource} which will provide its data dynamically with
	 * {@link #getData(org.apache.wicket.request.resource.IResource.Attributes)}
	 * 
	 * @param contentType
	 *            The Content type of the array.
	 */
	public ByteArrayResource(final String contentType)
	{
		super(contentType);
	}

	/**
	 * Creates a Resource from the given byte array with its content type
	 * 
	 * @param contentType
	 *            The Content type of the array.
	 * @param array
	 *            The binary content
	 */
	public ByteArrayResource(final String contentType, final byte[] array)
	{
		super(contentType, array);
	}

	/**
	 * Creates a Resource from the given byte array with its content type
	 * 
	 * @param contentType
	 *            The Content type of the array.
	 * @param array
	 *            The binary content
	 * @param filename
	 *            The filename that will be set as the Content-Disposition header.
	 */
	public ByteArrayResource(final String contentType, final byte[] array, final String filename)
	{
		super(contentType, array, filename);
	}

	@Override
	protected void writeData(Response response, byte[] data)
	{
		response.write(data);
	}

	@Override
	protected Long getLength(byte[] data)
	{
		return (long) data.length;
	}
}
