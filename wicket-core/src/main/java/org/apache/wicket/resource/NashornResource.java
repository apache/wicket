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
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.SimpleScriptContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullWriter;
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
@SuppressWarnings({ "restriction" })
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
			String saveScript = ensureSavetyScript(script, attributes);
			Object scriptResult = executeScript(new ScriptCallable(saveScript, attributes));
			ResourceResponse resourceResponse = new ResourceResponse();
			resourceResponse.setContentType("text/plain");
			resourceResponse.setWriteCallback(new PartWriterCallback(
				IOUtils.toInputStream(scriptResult != null ? scriptResult.toString() : ""),
				Long.valueOf(scriptResult != null ? scriptResult.toString().length() : 0),
				startbyte, endbyte));
			return resourceResponse;
		}
		catch (Exception e)
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
	 * Ensure that the given script is going to be save. Save because of endless loops for example.
	 * 
	 * @param script
	 *            the script to be make save
	 * @param attributes
	 *            the attributes
	 * @return the save script
	 * @throws Exception
	 *             if an error occured while making the script save
	 */
	private String ensureSavetyScript(String script, Attributes attributes) throws Exception
	{
		ScriptCallable scriptCallable = new ScriptCallable(
			getScriptByName(NashornResource.class.getSimpleName() + ".js"), attributes);
		Map<String, Object> extraBindings = new HashMap<>();
		extraBindings.put("script", script);
		extraBindings.put("debug", isDebug());
		extraBindings.put("debug_log_prefix", NashornResource.class.getSimpleName() + " - ");
		scriptCallable.setExtraBindings(extraBindings);
		scriptCallable.setOverrideClassFilter(new ClassFilter()
		{
			@Override
			public boolean exposeToScripts(String arg0)
			{
				return true;
			}
		});
		return executeScript(scriptCallable).toString();
	}


	/**
	 * Gets a script by name - the scope is always the class NashornResource
	 * 
	 * @param name
	 *            the name of the script
	 * @return the script
	 * @throws IOException
	 *             if the script fail to load
	 */
	private String getScriptByName(String name) throws IOException
	{
		String script = "";

		try (InputStream scriptInputStream = NashornResource.class.getResourceAsStream(name))
		{
			script = IOUtils.toString(scriptInputStream);
		}
		return script;
	}

	/**
	 * Executes a given script callable and the corresponding script
	 * 
	 * @param executeScript
	 *            the script callable to execute
	 * @return the script result
	 * @throws Exception
	 */
	private Object executeScript(ScriptCallable executeScript) throws Exception
	{
		Future<Object> scriptTask = scheduledExecutorService.submit(executeScript);
		scheduledExecutorService.schedule(() -> {
			scriptTask.cancel(true);
		} , this.delay, this.unit);
		return scriptTask.get();
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

		private Map<? extends String, ? extends Object> extraBindings = new HashMap<>();

		private ClassFilter overrideClassFilter;

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
			ScriptEngine scriptEngine = new NashornScriptEngineFactory().getScriptEngine(
				overrideClassFilter != null ? overrideClassFilter : getClassFilter());
			Bindings bindings = scriptEngine.createBindings();
			bindings.putAll(extraBindings);
			SimpleScriptContext scriptContext = new SimpleScriptContext();
			scriptContext.setWriter(getWriter());
			scriptContext.setErrorWriter(getErrorWriter());
			NashornResource.this.setup(attributes, bindings);
			bindings.put("nashornResourceReferenceScriptExecutionThread", Thread.currentThread());
			scriptContext.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
			return scriptEngine.eval(new StringReader(script), scriptContext);
		}

		/**
		 * Gets the extra bindings to be used to execute the script
		 * 
		 * @return the extra bindings
		 */
		public Map<? extends String, ? extends Object> getExtraBindings()
		{
			return extraBindings;
		}

		/**
		 * Sets the extra bindings to be used to execute the script
		 * 
		 * @param extraBindings
		 *            the extra bindings to be used to execute the script
		 */
		public void setExtraBindings(Map<? extends String, ? extends Object> extraBindings)
		{
			this.extraBindings = extraBindings;
		}

		/**
		 * Gets the override class filter to be used to execute the script
		 * 
		 * @return the override class filter
		 */
		public ClassFilter getOverrideClassFilter()
		{
			return overrideClassFilter;
		}

		/**
		 * Sets the override class filter to be used to execute the script
		 * 
		 * @param overrideClassFilter
		 *            the override class filter
		 */
		public void setOverrideClassFilter(ClassFilter overrideClassFilter)
		{
			this.overrideClassFilter = overrideClassFilter;
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

	/**
	 * Gets the writer to which print outputs are going to be written to
	 * 
	 * the default is to use {@link NullWriter}
	 * 
	 * @return the writer for output
	 */
	protected Writer getWriter()
	{
		return new NullWriter();
	}

	/**
	 * Gets the writer to which error messages are going to be written to
	 * 
	 * the default is to use {@link NullWriter}
	 * 
	 * @return the error writer
	 */
	protected Writer getErrorWriter()
	{
		return new NullWriter();
	}

	/**
	 * If debug is enabled
	 * 
	 * @return if debug is enabled
	 */
	protected boolean isDebug()
	{
		return false;
	}
}