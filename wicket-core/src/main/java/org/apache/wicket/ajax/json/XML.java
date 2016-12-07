package org.apache.wicket.ajax.json;

import org.apache.wicket.WicketRuntimeException;

@SuppressWarnings("javadoc")
public class XML
{

	public static final Character AMP = new Character('&');

	public static final Character APOS = new Character('\'');

	public static final Character BANG = new Character('!');

	public static final Character EQ = new Character('=');

	public static final Character GT = new Character('>');

	public static final Character LT = new Character('<');

	public static final Character QUEST = new Character('?');

	public static final Character QUOT = new Character('"');

	public static final Character SLASH = new Character('/');

	public static String escape(String string)
	{
		throw new WicketRuntimeException(JsonConstants.OPEN_JSON_EXCEPTION);
	}

	public static void noSpace(String string) throws JSONException
	{
		throw new WicketRuntimeException(JsonConstants.OPEN_JSON_EXCEPTION);
	}

	public static Object stringToValue(String string)
	{
		throw new WicketRuntimeException(JsonConstants.OPEN_JSON_EXCEPTION);
	}

	public static JSONObject toJSONObject(String string) throws JSONException
	{
		throw new WicketRuntimeException(JsonConstants.OPEN_JSON_EXCEPTION);
	}

	public static String toString(Object object) throws JSONException
	{
		throw new WicketRuntimeException(JsonConstants.OPEN_JSON_EXCEPTION);
	}

	public static String toString(Object object, String tagName) throws JSONException
	{
		throw new WicketRuntimeException(JsonConstants.OPEN_JSON_EXCEPTION);
	}
}