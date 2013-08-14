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
package org.apache.wicket.settings.def;

import org.apache.wicket.ajax.strategies.IAjaxStrategy;
import org.apache.wicket.ajax.strategies.Wicket6AjaxStrategy;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.settings.IAjaxSettings;
import org.apache.wicket.util.lang.Args;

/**
 *
 */
public class AjaxSettings implements IAjaxSettings
{
	private IAjaxStrategy ajaxStrategy = new Wicket6AjaxStrategy();

	public AjaxSettings(WebApplication application)
	{
	}

	@Override
	public IAjaxStrategy getAjaxStrategy()
	{
		return ajaxStrategy;
	}

	@Override
	public void setAjaxStrategy(IAjaxStrategy ajaxStrategy)
	{
		this.ajaxStrategy = Args.notNull(ajaxStrategy, "ajaxStrategy");
	}
}
