package org.apache.wicket.metrics.aspects;

import org.apache.wicket.metrics.WicketMetrics;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

/**
 * Collects basic information about pages
 * 
 * @author Tobias Soloschenko
 *
 */
@Aspect
public class PageAspect extends WicketMetrics
{

	/**
	 * Collects data how often a pages has been rendered
	 */
	@Before("target(org.apache.wicket.Page+) && call(* onRender(..))")
	public void beforeRequestProcessed()
	{
		mark("core/page/render");
	}
}
