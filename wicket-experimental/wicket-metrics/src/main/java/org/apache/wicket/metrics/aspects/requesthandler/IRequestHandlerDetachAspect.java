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
