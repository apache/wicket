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
package org.apache.wicket.core.request.handler.logger;

import org.apache.wicket.request.handler.resource.ResourceLogData;
import org.apache.wicket.request.handler.resource.ResourceStreamRequestHandler;
import org.apache.wicket.request.resource.ContentDisposition;
import org.apache.wicket.util.resource.IResourceStream;

/**
 * Contains logging data for resource stream requests, handled by
 * {@link ResourceStreamRequestHandler}.
 *
 * @author Emond Papegaaij
 */
public class ResourceStreamLogData extends ResourceLogData
{
	private static final long serialVersionUID = 1L;

	private final Class<? extends IResourceStream> resourceStreamClass;
	private final ContentDisposition contentDisposition;
	private final String contentType;

	/**
	 * Construct.
	 *
	 * @param streamHandler
	 */
	public ResourceStreamLogData(ResourceStreamRequestHandler streamHandler)
	{
		super(streamHandler.getFileName(), null, null, null);
		contentDisposition = streamHandler.getContentDisposition();
		resourceStreamClass = null;
		contentType = null;
	}

	/**
	 * Construct.
	 *
	 * @param streamHandler
	 * @param stream
	 */
	public ResourceStreamLogData(ResourceStreamRequestHandler streamHandler, IResourceStream stream)
	{
		super(streamHandler.getFileName(), stream.getLocale(), stream.getStyle(),
			stream.getVariation());
		contentDisposition = streamHandler.getContentDisposition();
		resourceStreamClass = stream.getClass();
		contentType = stream.getContentType();
	}

	/**
	 * Returns the class of the resource stream.
	 *
	 * @return The class of the resource stream.
	 */
	public final Class<? extends IResourceStream> getResourceStreamClass()
	{
		return resourceStreamClass;
	}

	/**
	 * @return contentDisposition.
	 */
	public final ContentDisposition getContentDisposition()
	{
		return contentDisposition;
	}

	/**
	 * @return contentType
	 */
	public final String getContentType()
	{
		return contentType;
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder("{");
		fillToString(sb);
		sb.append(",contentDisposition=");
		sb.append(getContentDisposition());
		if (getResourceStreamClass() != null)
		{
			sb.append(",resourceStreamClass=");
			sb.append(getResourceStreamClass().getName());
			sb.append(",contentType=");
			sb.append(getContentType());
		}
		sb.append("}");
		return sb.toString();
	}
}
