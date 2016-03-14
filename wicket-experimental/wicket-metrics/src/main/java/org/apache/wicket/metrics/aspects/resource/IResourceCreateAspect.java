package org.apache.wicket.metrics.aspects.resource;

import org.apache.wicket.metrics.WicketMetrics;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

/**
 * Measures how often a resource is created
 * @author Tobias Soloschenko
 *
 */
@Aspect
public class IResourceCreateAspect extends WicketMetrics
{
	/**
	 * Collects data how often a resource reference is created
	 * 
	 * @param joinPoint
	 *            the join point (resource reference) which is created
	 * @return the result of constructor
	 * @throws Throwable
	 *             might occur while creating a new resource reference
	 */
	@Around("execution(org.apache.wicket.request.resource.IResource.new(..))")
	public Object aroundNew(ProceedingJoinPoint joinPoint) throws Throwable
	{
		return mark("core/resource/resource/create", joinPoint);
	}
}
