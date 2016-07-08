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
package org.apache.wicket.metrics.aspects.markup;

import org.apache.wicket.metrics.WicketMetrics;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

/**
 * Measures information about wicket tags
 * 
 * @author Tobias Soloschenko
 *
 */
@Aspect
public class WicketTagCreateAspect extends WicketMetrics
{
	/**
	 * Collects data how often components redirect to another page
	 * 
	 * @throws Throwable
	 *             might occur while invoking setResponsePage
	 */
	@Before("call(org.apache.wicket.markup.WicketTag.new(..))")
	public void beforeResponsePage() throws Throwable
	{
		mark("core/tags/wicket/create", null);
	}
}
