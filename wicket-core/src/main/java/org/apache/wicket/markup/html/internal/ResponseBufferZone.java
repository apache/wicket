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
package org.apache.wicket.markup.html.internal;

import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.response.StringResponse;

/**
 * Utility class that buffers output and maintains markup stream index
 * 
 * @author igor.vaynberg
 */
public abstract class ResponseBufferZone
{
	private final RequestCycle cycle;
	private final MarkupStream stream;

	public ResponseBufferZone(RequestCycle cycle, MarkupStream stream)
	{
		this.cycle = cycle;
		this.stream = stream;
	}

	public CharSequence execute()
	{
		final int originalStreamPos = stream.getCurrentIndex();

		final Response original = cycle.getResponse();

		final StringResponse buffer = new StringResponse();
		cycle.setResponse(buffer);
		try
		{
			executeInsideBufferedZone();
			return buffer.getBuffer();
		}
		finally
		{
			cycle.setResponse(original);
			stream.setCurrentIndex(originalStreamPos);
		}
	}

	protected abstract void executeInsideBufferedZone();
}
