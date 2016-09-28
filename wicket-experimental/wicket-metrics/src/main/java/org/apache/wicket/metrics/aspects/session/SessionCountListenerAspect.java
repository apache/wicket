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
package org.apache.wicket.metrics.aspects.session;

import org.apache.wicket.metrics.WicketMetrics;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

/**
 * The Session count listener aspect measures how many sessions are active
 * 
 * @author Tobias Soloschenko
 *
 */
@Aspect
public class SessionCountListenerAspect extends WicketMetrics
{

	/**
	 * Measures if a session is going to be activated
	 * 
	 * @param joinPoint
	 *            the join point
	 * @return void
	 * @throws Throwable
	 *             if an error occurred 
	 */
	@Around("execution(* org.apache.wicket.metrics.aspects.session.SessionCountListener.inc(..))")
	public Object aroundInc(ProceedingJoinPoint joinPoint) throws Throwable
	{
		Object count = joinPoint.proceed();
		count("core/session/count", null, CounterOperation.INC, 1L);
		return count;
	}
	
	/**
	 * Measures if a session is going to be destroyed
	 * 
	 * @param joinPoint
	 *            the join point
	 * @return void
	 * @throws Throwable
	 *             if an error occurred
	 */
	@Around("execution(* org.apache.wicket.metrics.aspects.session.SessionCountListener.dec(..))")
	public Object aroundDec(ProceedingJoinPoint joinPoint) throws Throwable
	{
		Object count = joinPoint.proceed();
		count("core/session/count", null, CounterOperation.DEC, 1L);
		return count;
	}

}
