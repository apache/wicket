package org.apache.wicket.ajax.json;

import org.apache.wicket.WicketRuntimeException;

@SuppressWarnings("javadoc")
public class HTTPTokener extends JSONTokener
{

	public HTTPTokener(String string)
	{
		super(string);
		throw new WicketRuntimeException(JsonConstants.OPEN_JSON_EXCEPTION);
	}

	public String nextToken() throws JSONException
	{
		throw new WicketRuntimeException(JsonConstants.OPEN_JSON_EXCEPTION);
	}
}
