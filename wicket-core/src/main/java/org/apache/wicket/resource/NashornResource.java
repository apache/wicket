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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.resource.AbstractResource;
import org.apache.wicket.request.resource.PartWriterCallback;

import jdk.nashorn.api.scripting.ClassFilter;
import jdk.nashorn.api.scripting.NashornScriptEngineFactory;

/**
 * A nashorn resource to execute java script on server side
 * 
 * @author Tobias Soloschenko
 *
 */
@SuppressWarnings("restriction")
public class NashornResource extends AbstractResource
{

	private static final long serialVersionUID = 1L;

	private ScheduledExecutorService scheduledExecutorService;

	private long delay;

	private TimeUnit unit;

	/**
	 * Creates a new nashorn resource
	 * 
	 * @param scheduledExecutorService
	 *            the scheduled executor service to run scripts
	 * @param delay
	 *            the delay until a script execution is going to be terminated
	 * @param unit
	 *            the unit until a script execution is going to be terminated
	 */
	public NashornResource(ScheduledExecutorService scheduledExecutorService, long delay,
		TimeUnit unit)
	{
		this.scheduledExecutorService = scheduledExecutorService;
		this.delay = delay;
		this.unit = unit;
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
		HttpServletRequest httpServletRequest = (HttpServletRequest)attributes.getRequest()
			.getContainerRequest();
		try (InputStream inputStream = httpServletRequest.getInputStream())
		{
			String script = IOUtils.toString(inputStream);
			Future<Object> scriptTask = scheduledExecutorService
				.submit(executeScript(script, attributes));
			scheduledExecutorService.schedule(() -> {
				scriptTask.cancel(true);
			} , this.delay, this.unit);
			Object scriptResult = scriptTask.get();
			ResourceResponse resourceResponse = new ResourceResponse();
			resourceResponse.setContentType("text/plain");
			resourceResponse.setWriteCallback(new PartWriterCallback(
				IOUtils.toInputStream(scriptResult != null ? scriptResult.toString() : ""),
				Long.valueOf(scriptResult != null ? scriptResult.toString().length() : 0),
				startbyte, endbyte));
			return resourceResponse;
		}
		catch (IOException | ScriptException | InterruptedException | ExecutionException e)
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
	 * The callable to execute the script
	 * 
	 * @author Tobias Soloschenko
	 *
	 */
	private class ScriptCallable implements Callable<Object>
	{

		private String script;

		private Attributes attributes;

		/**
		 * Creates a script result
		 * 
		 * @param script
		 *            the script to be executed
		 * @param attributes
		 *            the attributes to
		 */
		public ScriptCallable(String script, Attributes attributes)
		{
			this.script = script;
			this.attributes = attributes;
		}

		@Override
		public Object call() throws Exception
		{
			ScriptEngine scriptEngine = new NashornScriptEngineFactory().getScriptEngine(getClassFilter());
			Bindings bindings = scriptEngine.createBindings();
			SimpleScriptContext scriptContext = new SimpleScriptContext();
			NashornResource.this.setup(attributes, bindings);
			scriptContext.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
			jdk.nashorn.api.scripting.ClassFilter filter ;
			return scriptEngine.eval(new StringReader(script), scriptContext);
		}
	}

	/**
	 * Executes the given script
	 * 
	 * @param script
	 *            the script to be executed
	 * @param attributes
	 *            the attributes to be provided for the setup method
	 * @return the object the script returned
	 * @throws ScriptException
	 *             if something went wrong in the script
	 */
	public Callable<Object> executeScript(String script, Attributes attributes)
		throws ScriptException
	{
		return new ScriptCallable(script, attributes);
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

	/**
	 * Gets the class filter to apply to the scripting engine
	 * 
	 * @return the class filter to apply to the scripting engine
	 */
	protected ClassFilter getClassFilter()
	{
		// default is to allow nothing!
		return new ClassFilter()
		{
			@Override
			public boolean exposeToScripts(String name)
			{
				return false;
			}
		};
	}
}