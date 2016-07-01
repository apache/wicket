package org.apache.wicket.http2.markup.head;

import org.apache.wicket.Component;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 * The object to be pushed. See the urlFor methods of {@link Component} to know what can be used in
 * addition to {@link String}.
 * 
 * @author Tobias Soloschenko
 */
public class PushItem
{
	private Object object;

	private PageParameters pageParameters;

	/**
	 * Creates a push item
	 * 
	 * @param object
	 *            the object
	 * @param pageParameters
	 *            the page parameters
	 */
	public PushItem(Object object, PageParameters pageParameters)
	{
		this.object = object;
		this.pageParameters = pageParameters;
	}

	/**
	 * Creates a push item
	 * 
	 * @param object
	 *            the object
	 */
	public PushItem(Object object)
	{
		this.object = object;
	}

	/**
	 * Creates a push item
	 */
	public PushItem()
	{
	}

	/**
	 * Gets the object
	 * 
	 * @return the object
	 */
	public Object getObject()
	{
		return object;
	}

	/**
	 * Sets the object
	 * 
	 * @param object
	 *            the object
	 * @return the push item
	 */
	public PushItem setObject(Object object)
	{
		this.object = object;
		return this;
	}

	/**
	 * Gets the page parameters
	 * 
	 * @return the page parameters
	 */
	public PageParameters getPageParameters()
	{
		return pageParameters;
	}

	/**
	 * Sets the page parameters
	 * 
	 * @param pageParameters
	 *            the page parameters
	 * @return the push item
	 */
	public PushItem setPageParameters(PageParameters pageParameters)
	{
		this.pageParameters = pageParameters;
		return this;
	}
}
