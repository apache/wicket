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
package org.apache.wicket.metrics.aspects;

import org.apache.wicket.metrics.WicketMetrics;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

/**
 * Aspect which checks ajax request targets
 * 
 * @author Tobias Soloschenko
 *
 */
@Aspect
public class IPartialPageRequestHandlerAspect extends WicketMetrics
{
	/**
	 * Collects data how often components calls add
	 * 
	 * @throws Throwable
	 *             might occur while invoking add
	 */
	@Before("call(* org.apache.wicket.core.request.handler.IPartialPageRequestHandler.add(..))")
	public void beforeAdd() throws Throwable
	{
		mark("core/ajax/add", null);
	}
	
	/**
	 * Collects data how often components calls prependJavaScript
	 * 
	 * @throws Throwable
	 *             might occur while invoking prependJavaScript
	 */
	@Before("call(* org.apache.wicket.core.request.handler.IPartialPageRequestHandler.prependJavaScript(..))")
	public void beforePrependJavaScript() throws Throwable
	{
		mark("core/ajax/prependJavaScript", null);
	}
	
	/**
	 * Collects data how often components calls appendJavaScript
	 * 
	 * @throws Throwable
	 *             might occur while invoking appendJavaScript
	 */
	@Before("call(* org.apache.wicket.core.request.handler.IPartialPageRequestHandler.appendJavaScript(..))")
	public void beforeAppendJavaScript() throws Throwable
	{
		mark("core/ajax/appendJavaScript", null);
	}

}
