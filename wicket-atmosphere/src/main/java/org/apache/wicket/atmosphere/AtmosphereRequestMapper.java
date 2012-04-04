package org.apache.wicket.atmosphere;

import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.IRequestMapper;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Url;

public class AtmosphereRequestMapper implements IRequestMapper
{
	@Override
	public IRequestHandler mapRequest(Request request)
	{
		if (request instanceof AtmosphereWebRequest)
		{
			AtmosphereWebRequest pushRequest = (AtmosphereWebRequest) request;
			return new AtmosphereRequestHandler(pushRequest.getPageKey(),
				pushRequest.getSubscriptions(), pushRequest.getEvent());
		}
		return null;
	}

	@Override
	public int getCompatibilityScore(Request request)
	{
		return request instanceof AtmosphereWebRequest ? Integer.MAX_VALUE : 0;
	}

	@Override
	public Url mapHandler(IRequestHandler requestHandler)
	{
		return null;
	}
}
