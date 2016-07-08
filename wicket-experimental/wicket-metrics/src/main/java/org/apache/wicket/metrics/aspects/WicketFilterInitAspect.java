package org.apache.wicket.metrics.aspects;

import javax.servlet.FilterConfig;

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
