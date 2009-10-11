package org.apache.wicket.ng.mock;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;

import org.apache.wicket.ng.Session;
import org.apache.wicket.ng.request.Request;
import org.apache.wicket.ng.session.SessionStore;

/**
 * Session store that holds one session.
 * 
 * @author Matej Knopp
 */
public class MockSessionStore implements SessionStore
{

	public MockSessionStore()
	{
		
	}		
	
	private String sessionId;	
	private Map<String, Serializable> attributes = new HashMap<String, Serializable>();
	private Session session;
	
	public void bind(Request request, Session newSession)
	{
		session = newSession;	
	}

	public void destroy()
	{
		cleanup();
	}

	public Serializable getAttribute(Request request, String name)
	{
		return attributes.get(name);
	}

	public Set<String> getAttributeNames(Request request)
	{
		return Collections.unmodifiableSet(attributes.keySet());
	}

	public String getSessionId(Request request, boolean create)
	{
		if (create && sessionId == null)
		{
			sessionId = UUID.randomUUID().toString();
		}
		return sessionId;
	}

	private void cleanup()
	{
		sessionId = null;
		attributes.clear();
		session = null;	
	}
	
	public void invalidate(Request request)
	{
		for (UnboundListener l : unboundListeners)
		{
			l.sessionUnbound(sessionId);
		}
		
		cleanup();
	}

	public Session lookup(Request request)
	{		
		return session;
	}
	
	private Set<UnboundListener> unboundListeners = new CopyOnWriteArraySet<UnboundListener>();

	public void registerUnboundListener(UnboundListener listener)
	{
		unboundListeners.add(listener);				
	}

	public void removeAttribute(Request request, String name)
	{
		attributes.remove(name);
	}

	public void setAttribute(Request request, String name, Serializable value)
	{		
		attributes.put(name, value);
	}

	public void unregisterUnboundListener(UnboundListener listener)
	{
		unboundListeners.remove(listener);
	}

}
