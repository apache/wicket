package org.apache.wicket.ng.page;

import java.io.Serializable;

/**
 * Context object for {@link PageManager}. This decouples the {@link PageManager} from request
 * cycle and session.
 * 
 * @author Matej Knopp
 * 
 */
public interface PageManagerContext
{
	public void setRequestData(Object data);

	public Object getRequestData();

	public void setSessionAttribute(String key, Serializable value);

	public Serializable getSessionAttribute(String key);

	public void bind();
	
	public String getSessionId();
}