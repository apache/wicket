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

import jakarta.servlet.FilterConfig;

import org.apache.wicket.metrics.WicketMetrics;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

/**
 * This aspect applies the application to wicket metrics
 * 
 * @author Tobias Soloschenko
 *
 */
@Aspect
public class WicketFilterInitAspect
{

	/**
	 * Gets the filter name during the initialization
	 * 
	 * @param joinPoint
	 *            the join point to be proceed
	 * @return the proceeded outcome of the join point
	 * @throws Throwable
	 *             if something went wrong
	 */
	@Around("execution(* org.apache.wicket.protocol.http.WicketFilter.init(..))")
	public Object aroundInit(ProceedingJoinPoint joinPoint) throws Throwable
	{
		Object[] args = joinPoint.getArgs();
		String filterName = null;
		if (args.length == 2)
		{
			FilterConfig filterConfig = (FilterConfig)args[1];
			filterName = filterConfig.getFilterName();
		}
		else
		{
			FilterConfig filterConfig = (FilterConfig)args[0];
			filterName = filterConfig.getFilterName();
		}
		WicketMetrics.setFilterName(filterName);
		return joinPoint.proceed();
	}
}
