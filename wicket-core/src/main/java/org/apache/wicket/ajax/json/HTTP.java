package org.apache.wicket.ajax.json;

import org.apache.wicket.WicketRuntimeException;

@SuppressWarnings("javadoc")
public class HTTP
{

	public static final String CRLF = "\r\n";

	public static JSONObject toJSONObject(String string) throws JSONException
	{
		throw new WicketRuntimeException(JsonConstants.OPEN_JSON_EXCEPTION);
	}

	public static String toString(JSONObject jo) throws JSONException
	{
		throw new WicketRuntimeException(JsonConstants.OPEN_JSON_EXCEPTION);
	}
}
