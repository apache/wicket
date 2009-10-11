package org.apache.wicket.ng.request.handler;

import org.apache.wicket.ng.request.component.PageParameters;
import org.apache.wicket.ng.request.component.RequestableComponent;
import org.apache.wicket.ng.request.component.RequestablePage;
import org.apache.wicket.util.lang.Checks;

/**
 * Extension of {@link PageProvider} that is also capable of providing a Component belonging to the
 * page.
 * 
 * @see PageProvider
 * 
 * @author Matej Knopp
 */
public class PageAndComponentProvider extends PageProvider
{
	private RequestableComponent component;
	private String componentPath;

	/**
	 * @see PageProvider#PageProvider(RequestablePage)
	 * 
	 * @param page
	 * @param componentPath
	 */
	public PageAndComponentProvider(RequestablePage page, String componentPath)
	{
		super(page);
		setComponentPath(componentPath);
	}

	/**
	 * @see PageProvider#PageProvider(RequestablePage)
	 * 
	 * @param page
	 * @param component
	 */
	public PageAndComponentProvider(RequestablePage page, RequestableComponent component)
	{
		super(page);		
		
		Checks.argumentNotNull(component, "component");
		
		this.component = component;
	}

	/**
	 * @see PageProvider#PageProvider(Class, PageParameters)
	 * 
	 * @param pageClass
	 * @param pageParameters
	 * @param componentPath
	 */
	public PageAndComponentProvider(Class<? extends RequestablePage> pageClass,
			PageParameters pageParameters, String componentPath)
	{
		super(pageClass, pageParameters);
		setComponentPath(componentPath);
	}

	/**
	 * @see PageProvider#PageProvider(Class)
	 * 
	 * @param pageClass
	 * @param componentPath
	 */
	public PageAndComponentProvider(Class<? extends RequestablePage> pageClass, String componentPath)
	{
		super(pageClass);
		setComponentPath(componentPath);
	}

	/**
	 * @see PageProvider#PageProvider(int, Class, Integer)
	 * 
	 * @param pageId
	 * @param pageClass
	 * @param renderCount
	 * @param componentPath
	 */
	public PageAndComponentProvider(int pageId, Class<? extends RequestablePage> pageClass,
			Integer renderCount, String componentPath)
	{
		super(pageId, pageClass, renderCount);
		setComponentPath(componentPath);
	}

	/**
	 * @see PageProvider#PageProvider(int, Class, PageParameters, Integer)
	 * 
	 * @param pageId
	 * @param pageClass
	 * @param pageParameters
	 * @param renderCount
	 * @param componentPath
	 */
	public PageAndComponentProvider(int pageId, Class<? extends RequestablePage> pageClass,
			PageParameters pageParameters, Integer renderCount, String componentPath)
	{
		super(pageId, pageClass, pageParameters, renderCount);
		setComponentPath(componentPath);
	}

	/**
	 * @see PageProvider#PageProvider(int, Integer)
	 * 
	 * @param pageId
	 * @param renderCount
	 * @param componentPath
	 */
	public PageAndComponentProvider(int pageId, Integer renderCount, String componentPath)
	{
		super(pageId, renderCount);
		setComponentPath(componentPath);
	}

	@Override
	protected boolean prepareForRenderNewPage()
	{
		return true;
	}

	/**
	 * Returns component on specified page with given path.
	 * 
	 * @return component
	 */
	public RequestableComponent getComponent()
	{
		if (component == null)
		{
			RequestablePage page = getPageInstance();
			component = page.get(componentPath);
		}
		if (component == null)
		{
			throw new ComponentNotFoundException("Could not find component '" + componentPath + "' on page '"
					+ getPageClass());
		}
		return component;
	}

	/**
	 * Returns the component path.
	 * 
	 * @return
	 */
	public String getComponentPath()
	{
		if (componentPath != null)
		{
			return componentPath;
		}
		else
		{
			return component.getPath();
		}
	}

	private void setComponentPath(String componentPath)
	{
		Checks.argumentNotNull(componentPath, "componentPath");
				
		this.componentPath = componentPath;
	}
}
