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
package org.apache.wicket.metrics.aspects.component;

import org.apache.wicket.metrics.WicketMetrics;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

/**
 * Gets information how often different components are configured
 * 
 * @author Tobias Soloschenko
 */
@Aspect
public class ComponentOnConfigureAspect extends WicketMetrics
{

	/**
	 * Collects data how often components calls onConfigure
	 * 
	 * @param joinPoint
	 *            the join point (component) which is configured
	 * @return the object returned from the join point
	 * 
	 * @throws Throwable
	 *             might occur while invoking onConfigure
	 */
	@Around("execution(* org.apache.wicket.Component.onConfigure(..))")
	public Object aroundOnConfigure(ProceedingJoinPoint joinPoint) throws Throwable
	{
		return measureTime("core/component/configure", joinPoint);
	}
}
