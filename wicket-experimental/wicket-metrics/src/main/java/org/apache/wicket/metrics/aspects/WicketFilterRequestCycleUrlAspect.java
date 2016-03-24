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

import javax.servlet.http.HttpServletRequest;

import org.apache.wicket.metrics.WicketMetrics;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

/**
 * Aspect to measure request url time
 * 
 * @author Tobias Soloschenko
 */
@Aspect
public class WicketFilterRequestCycleUrlAspect extends WicketMetrics
{
	/**
	 * Collects data how often a request has been made against the webapp and counts the time how
	 * long the request took. Measures the information with the request url
	 * 
	 * @param joinPoint
	 *            the joinPoint to be proceed
	 * @return returns the boolean of the processRequest method
	 * 
	 * @throws Throwable
	 *             might occur while invoking process request
	 */
	@Around("execution(* org.apache.wicket.protocol.http.WicketFilter.processRequestCycle(..))")
	public Object aroundRequestProcessedWithURL(ProceedingJoinPoint joinPoint) throws Throwable
	{
		Object[] args = joinPoint.getArgs();
		if (args.length >= 3)
		{
			Object requestAsObject = args[2];
			if (requestAsObject != null && requestAsObject instanceof HttpServletRequest)
			{
				HttpServletRequest httpServletRequest = (HttpServletRequest)requestAsObject;
				String requestUrl = httpServletRequest.getRequestURL().toString();
				String replacedUrl = requestUrl.replace('/', '_');
				replacedUrl = replacedUrl.replace('.', '_');
				replacedUrl = replacedUrl.replaceAll(";jsessionid=.*?(?=\\?|$)", "");
				return measureTime("core/application/request/" + replacedUrl, joinPoint, false);
			}
		}
		return joinPoint.proceed();
	}
}
