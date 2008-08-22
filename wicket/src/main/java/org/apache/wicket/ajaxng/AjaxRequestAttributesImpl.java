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
package org.apache.wicket.ajaxng;

import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.markup.html.form.Form;

/**
 * Simple implementation of the {@link AjaxRequestAttributes} interface. This class supports
 * delegating the calls to another {@link AjaxRequestAttributes} instance if one is specified.
 * <p>
 * To extend attributes from behavior or component the following pattern can be used:
 * <pre>
 *      // add a precondition to super attirbutes
 *      class MyBehavior extends AjaxBehavior
 *      {
 *          public AjaxRequestAttributes getAttributes()
 *          {
 *              return new AjaxRequestAttributesImpl(super.getAttributes) 
 *              {
 *                  public FunctionList getPreconditions()
 *                  {
 *                      return super.getPreconditions().add("function(requestQueueItem) { return true; }";);
 *                  }
 *              }
 *          }
 *      }		
 * </pre>
 * 
 * @author Matej Knopp
 */
public class AjaxRequestAttributesImpl implements AjaxRequestAttributes
{
	private final AjaxRequestAttributes delegate;

	/**
	 * Construct.
	 * 
	 * @param delegate
	 */
	public AjaxRequestAttributesImpl(AjaxRequestAttributes delegate)
	{
		this.delegate = delegate;
	}

	/**
	 * 
	 * Construct.
	 */
	public AjaxRequestAttributesImpl()
	{
		this(null);
	}

	public FunctionList getBeforeHandlers()
	{
		FunctionList result = null;
		if (delegate != null)
		{
			result = delegate.getBeforeHandlers();
		}
		if (result == null)
		{
			result = new FunctionList();
		}
		return result;
	}

	public FunctionList getErrorHandlers()
	{
		FunctionList result = null;
		if (delegate != null)
		{
			result = delegate.getErrorHandlers();
		}
		if (result == null)
		{
			result = new FunctionList();
		}
		return result;
	}

	public Form<?> getForm()
	{
		if (delegate != null)
		{
			return delegate.getForm();
		}
		else
		{
			return null;
		}
	}

	public FunctionList getPreconditions()
	{
		FunctionList result = null;
		if (delegate != null)
		{
			result = delegate.getPreconditions();
		}
		if (result == null)
		{
			result = new FunctionList();
		}
		return result;
	}

	public Integer getProcessingTimeout()
	{
		if (delegate != null)
		{
			return delegate.getProcessingTimeout();
		}
		else
		{
			return null;
		}
	}

	public Integer getRequesTimeout()
	{
		if (delegate != null)
		{
			return delegate.getRequesTimeout();
		}
		else
		{
			return null;
		}
	}

	public FunctionList getSuccessHandlers()
	{
		FunctionList result = null;
		if (delegate != null)
		{
			result = delegate.getSuccessHandlers();
		}
		if (result == null)
		{
			result = new FunctionList();
		}
		return result;
	}

	public Integer getThrottle()
	{
		if (delegate != null)
		{
			return delegate.getThrottle();
		}
		else
		{
			return null;
		}
	}

	public String getToken()
	{
		if (delegate != null)
		{
			return delegate.getToken();
		}
		else
		{
			return null;
		}
	}

	public FunctionList getUrlArgumentMethods()
	{
		FunctionList result = null;
		if (delegate != null)
		{
			result = delegate.getUrlArgumentMethods();
		}
		if (result == null)
		{
			result = new FunctionList();
		}
		return result;
	}

	public Map<String, Object> getUrlArguments()
	{
		Map<String, Object> result = null;
		if (delegate != null)
		{
			result = delegate.getUrlArguments();
		}
		if (result == null)
		{
			result = new HashMap<String, Object>();
		}
		return result;
	}

	public Boolean isMultipart()
	{
		if (delegate != null)
		{
			return delegate.isMultipart();
		}
		else
		{
			return null;
		}
	}

	public Boolean isRemovePrevious()
	{
		if (delegate != null)
		{
			return delegate.isRemovePrevious();
		}
		else
		{
			return null;
		}
	}

	public Boolean isThrottlePostpone()
	{
		if (delegate != null)
		{
			return delegate.isThrottlePostpone();
		}
		else
		{
			return null;
		}
	}
}
