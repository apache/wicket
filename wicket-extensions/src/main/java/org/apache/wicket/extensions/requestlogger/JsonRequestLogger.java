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
package org.apache.wicket.extensions.requestlogger;

import java.io.IOException;
import java.io.Serializable;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import org.apache.wicket.protocol.http.AbstractRequestLogger;
import org.apache.wicket.protocol.http.RequestLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JsonRequestLogger uses Jackson to log requests in JSON-format. You will need jackson-mapper in
 * your classpath, ie. like:
 * 
 * <pre>
 * {@literal
 * <dependency>
 *     <groupId>com.fasterxml.jackson.core</groupId>
 *     <artifactId>jackson-databind</artifactId>
 *     <version>2.7.1</version>
 * </dependency>
 * }
 * </pre>
 * 
 * @author Emond Papegaaij
 */
public class JsonRequestLogger extends AbstractRequestLogger
{
	// Reusing the logger from RequestLogger
	private static final Logger LOG = LoggerFactory.getLogger(RequestLogger.class);

	/**
	 * Specify that the 'default' filter should be used for serialization. This filter will prevent
	 * jackson from serializing the request handlers.
	 */
	private static final class FilteredIntrospector extends JacksonAnnotationIntrospector
	{
		@Override
		public Object findFilterId(Annotated a)
		{
			return "default";
		}
	}

	/**
	 * A simple tuple for request and session.
	 */
	private static final class RequestSessionTuple implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private final RequestData request;
		private final SessionData session;

		public RequestSessionTuple(RequestData request, SessionData session)
		{
			this.request = request;
			this.session = session;
		}

		public RequestData getRequest()
		{
			return request;
		}

		public SessionData getSession()
		{
			return session;
		}
	}

	private final ObjectMapper mapper;

	/**
	 * Construct.
	 */
	public JsonRequestLogger()
	{
		mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		SimpleFilterProvider filters = new SimpleFilterProvider();
		filters.addFilter("default",
			SimpleBeanPropertyFilter.serializeAllExcept("eventTarget", "responseTarget"));
		mapper.setFilterProvider(filters);
		mapper.setAnnotationIntrospector(new FilteredIntrospector());
	}

	/**
	 * @return The mapper used to serialize the log data
	 */
	protected ObjectMapper getMapper()
	{
		return mapper;
	}

	@Override
	protected void log(RequestData rd, SessionData sd)
	{
		if (LOG.isInfoEnabled())
		{
			LOG.info(getLogString(rd, sd));
		}
	}

	protected String getLogString(RequestData rd, SessionData sd)
	{
		try
		{
			return getMapper().writeValueAsString(new RequestSessionTuple(rd, sd));
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}
}
