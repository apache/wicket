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
package org.apache.wicket.resource;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.resource.AbstractResource;
import org.apache.wicket.request.resource.PartWriterCallback;

/**
 * A nashorn resource to execute java script on server side
 * 
 * @author Tobias Soloschenko
 *
 */
public class NashornResource extends AbstractResource
{

	private static final long serialVersionUID = 1L;

	private Bindings bindings;

	private ScriptEngine scriptEngine;

	private SimpleScriptContext scriptContext;
	
	private static final String ENGINE_NAME = "nashorn";

	/**
	 * Creates a new nashorn resource
	 */
	public NashornResource()
	{
		scriptEngine = new ScriptEngineManager().getEngineByName(ENGINE_NAME);
		bindings = scriptEngine.createBindings();
		scriptContext = new SimpleScriptContext();
		scriptContext.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
	}

	/**
	 * Executes the java script code received from the client and returns the response
	 */
	@Override
	protected ResourceResponse newResourceResponse(Attributes attributes)
	{
		RequestCycle cycle = RequestCycle.get();
		Long startbyte = cycle.getMetaData(CONTENT_RANGE_STARTBYTE);
		Long endbyte = cycle.getMetaData(CONTENT_RANGE_ENDBYTE);
		setup(attributes, bindings);
		HttpServletRequest httpServletRequest = (HttpServletRequest)attributes.getRequest()
			.getContainerRequest();
		try (InputStream inputStream = httpServletRequest.getInputStream())
		{
			String script = IOUtils.toString(inputStream);
			Object eval = scriptEngine.eval(new StringReader(script), scriptContext);
			ResourceResponse resourceResponse = new ResourceResponse();
			resourceResponse.setContentType("text/plain");
			resourceResponse.setWriteCallback(
				new PartWriterCallback(IOUtils.toInputStream(eval != null ? eval.toString() : ""),
					Long.valueOf(eval != null ? eval.toString().length() : 0), startbyte, endbyte));
			return resourceResponse;
		}
		catch (IOException | ScriptException e)
		{
			ResourceResponse errorResourceResponse = processError(e);
			if (errorResourceResponse == null)
			{
				throw new WicketRuntimeException(
					"Error while reading / executing the script the script", e);
			}
			else
			{
				return errorResourceResponse;
			}

		}
	}

	/**
	 * Customize the error response sent to the client
	 * 
	 * @param e
	 *            the exception occurred
	 * @return the error response
	 */
	protected ResourceResponse processError(Exception e)
	{
		return null;
	}

	/**
	 * Setup the bindings and make information available to the scripting context
	 * 
	 * @param attributes
	 *            the attributes of the request
	 * @param bindings
	 *            the bindings to add java objects to
	 */
	protected void setup(Attributes attributes, Bindings bindings)
	{
		// NOOP
	}
}
