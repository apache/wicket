package org.apache.wicket.ajax.json;

import org.apache.wicket.WicketRuntimeException;

/**
 * @deprecated since 6.27.0/7.7.0 for legal reasons.
 */
@Deprecated
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
