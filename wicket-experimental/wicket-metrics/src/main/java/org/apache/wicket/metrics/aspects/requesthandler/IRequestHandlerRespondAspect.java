package org.apache.wicket.metrics.aspects.requesthandler;

import org.apache.wicket.metrics.WicketMetrics;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

/**
 * Measures information about request handlers respond
 * 
 * @author Tobias Soloschenko
 *
 */
@Aspect
public class IRequestHandlerRespondAspect extends WicketMetrics
{
	/**
	 * Collects data how often a request handler processes its response
	 * 
	 * @param joinPoint
	 *            the join point (request handler) which processes the response
	 * @return the object returned from the join point
	 * @throws Throwable
	 *             might occur while respond
	 */
	@Around("execution(* org.apache.wicket.request.IRequestHandler.respond(..))")
	public Object aroundOnRender(ProceedingJoinPoint joinPoint) throws Throwable
	{
		return measureTime("core/requesthandler/respond", joinPoint);
	}
}
