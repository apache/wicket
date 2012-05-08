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
package org.apache.wicket.ajax.attributes;

import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;

/**
 * {@code CallbackParameter} is a specification of a parameter that is used in an
 * {@linkplain AbstractDefaultAjaxBehavior#getCallbackFunction(CallbackParameter...) AJAX callback
 * function}. It specifies if and how the parameter is added to the function declaration, if and how
 * it is added to the AJAX callback and what code to use to generate the contents.
 * 
 * @author papegaaij
 */
public class CallbackParameter
{
	/**
	 * Add a parameter to the function declaration. This parameter will not be passed to the AJAX
	 * callback. For example, the following code:
	 * 
	 * <pre>
	 * {@literal
	 * 	getCallbackFunction(context("event"), context("ui"));
	 * }
	 * </pre>
	 * 
	 * generates a function with two parameters, like <code>function(event, ui) {...}</code>.
	 * 
	 * @param name
	 * @return The parameter
	 */
	public static CallbackParameter context(String name)
	{
		return new CallbackParameter(name, null, null);
	}

	/**
	 * Add a parameter to the function declaration that is also passed to the AJAX callback. For
	 * example, the following code:
	 * 
	 * <pre>
	 * {@literal
	 * 	getCallbackFunction(explicit("param"));
	 * }
	 * </pre>
	 * 
	 * generates a function with one parameter, like <code>function(param) {...}</code> where
	 * 'param' is passed literally as extra parameter to the AJAX callback.
	 * 
	 * @param name
	 * @return The parameter
	 */
	public static CallbackParameter explicit(String name)
	{
		return new CallbackParameter(name, name, name);
	}

	/**
	 * Add a parameter to the AJAX callback that is resolved inside the function, it will not be
	 * added as function parameter. For example, the following code:
	 * 
	 * <pre>
	 * {@literal
	 * 	getCallbackFunction(resolved("param", "global.substring(0, 3)"));
	 * }
	 * </pre>
	 * 
	 * generates a function without parameters, like <code>function() {...}</code> where the first 3
	 * characters of the global variable 'global' are passed as extra parameter to the AJAX callback
	 * under the name 'param'.
	 * 
	 * @param name
	 * @param code
	 * @return The parameter
	 */
	public static CallbackParameter resolved(String name, String code)
	{
		return new CallbackParameter(null, name, code);
	}

	/**
	 * Add a parameter to the function declaration that is also passed to the AJAX callback, but
	 * converted. For example, the following code:
	 * 
	 * <pre>
	 * {@literal
	 * 	getCallbackFunction(converted("param", "param.substring(0, 3)"));
	 * }
	 * </pre>
	 * 
	 * generates a function with one parameter, like <code>function(param) {...}</code> where the
	 * first 3 characters of 'param' are passed as extra parameter to the AJAX callback.
	 * 
	 * @param name
	 * @param code
	 * @return The parameter
	 */
	public static CallbackParameter converted(String name, String code)
	{
		return new CallbackParameter(name, name, code);
	}

	private String functionParameterName;
	private String ajaxParameterName;
	private String ajaxParameterCode;

	private CallbackParameter(String functionParameterName, String ajaxParameterName,
		String ajaxParameterCode)
	{
		this.functionParameterName = functionParameterName;
		this.ajaxParameterName = ajaxParameterName;
		this.ajaxParameterCode = ajaxParameterCode;
	}

	/**
	 * @return the name of the parameter to add to the function declaration, or null if no parameter
	 *         should be added.
	 */
	public String getFunctionParameterName()
	{
		return functionParameterName;
	}

	/**
	 * @return the name of the parameter to add to the AJAX callback, or null if no parameter should
	 *         be added.
	 */
	public String getAjaxParameterName()
	{
		return ajaxParameterName;
	}

	/**
	 * @return the javascript code to use to fill the parameter for the AJAX callback.
	 */
	public String getAjaxParameterCode()
	{
		return ajaxParameterCode;
	}
}
