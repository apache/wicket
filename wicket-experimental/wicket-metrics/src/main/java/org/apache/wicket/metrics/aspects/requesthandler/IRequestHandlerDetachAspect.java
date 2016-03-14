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
package org.apache.wicket.metrics.aspects.requesthandler;

import org.apache.wicket.metrics.WicketMetrics;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

/**
 * Measures information about request handlers detach
 * 
 * @author Tobias Soloschenko
 *
 */
@Aspect
public class IRequestHandlerDetachAspect extends WicketMetrics
{
	/**
	 * Collects data how often a request handler calls detach
	 * 
	 * @param joinPoint
	 *            the join point (request handler) which processes the response
	 * @return the object returned from the join point
	 * @throws Throwable
	 *             might occur while detach
	 */
	@Around("execution(* org.apache.wicket.request.IRequestHandler.detach(..))")
	public Object aroundOnRender(ProceedingJoinPoint joinPoint) throws Throwable
	{
		return measureTime("core/requesthandler/detach", joinPoint);
	}
}
