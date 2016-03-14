package org.apache.wicket.metrics.aspects.model;

import org.apache.wicket.metrics.WicketMetrics;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

/**
 * Measures information about how long the loading process of the ldm take
 * 
 * @author Tobias Soloschenko
 *
 */
@Aspect
public class LoadableDetachableModelLoadAspect extends WicketMetrics
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
	@Around("execution(* org.apache.wicket.model.LoadableDetachableModel.load())")
	public Object aroundOnRender(ProceedingJoinPoint joinPoint) throws Throwable
	{
		return measureTime("core/model/loadabledetachablemodel/load", joinPoint);
	}
}
