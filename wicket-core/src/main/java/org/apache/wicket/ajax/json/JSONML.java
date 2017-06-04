package org.apache.wicket.ajax.json;

import org.apache.wicket.WicketRuntimeException;

/**
 * @deprecated since 6.27.0/7.7.0 for legal reasons.
 */
@Deprecated
@SuppressWarnings("javadoc")
public class JSONML
{

	public static JSONArray toJSONArray(String string) throws JSONException
	{
		throw new WicketRuntimeException(JsonConstants.OPEN_JSON_EXCEPTION);
	}

	public static JSONArray toJSONArray(XMLTokener x) throws JSONException
	{
		throw new WicketRuntimeException(JsonConstants.OPEN_JSON_EXCEPTION);
	}

	public static JSONObject toJSONObject(XMLTokener x) throws JSONException
	{
		throw new WicketRuntimeException(JsonConstants.OPEN_JSON_EXCEPTION);
	}

	public static JSONObject toJSONObject(String string) throws JSONException
	{
		throw new WicketRuntimeException(JsonConstants.OPEN_JSON_EXCEPTION);
	}

	public static String toString(JSONArray ja) throws JSONException
	{
		throw new WicketRuntimeException(JsonConstants.OPEN_JSON_EXCEPTION);
	}

	public static String toString(JSONObject jo) throws JSONException
	{
		throw new WicketRuntimeException(JsonConstants.OPEN_JSON_EXCEPTION);
	}
}
