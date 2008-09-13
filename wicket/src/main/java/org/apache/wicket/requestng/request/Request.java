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
package org.apache.wicket.requestng.request;

import org.apache.wicket.requestng.RequestParameters;
import org.apache.wicket.requestng.Url;

public abstract class Request
{
	public abstract Url getUrl();
	
	public abstract RequestParameters getRequestParameters();
	
	public static final String PARAM_AJAX = "wicket:ajax";
	
	public boolean isAjax()
	{
		return getRequestParameters().getParameterValue(PARAM_AJAX).toBoolean(false); 
	}
}
