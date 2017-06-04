package org.apache.wicket.ajax.json;

import org.apache.wicket.WicketRuntimeException;

/**
 * @deprecated since 6.27.0/7.7.0 for legal reasons.
 */
@Deprecated
@SuppressWarnings({ "javadoc", "rawtypes", "unchecked" })
public class XMLTokener extends JSONTokener
{

	public static final java.util.HashMap entity;

	static
	{
		entity = new java.util.HashMap(8);
		entity.put("amp", XML.AMP);
		entity.put("apos", XML.APOS);
		entity.put("gt", XML.GT);
		entity.put("lt", XML.LT);
		entity.put("quot", XML.QUOT);
	}

	public XMLTokener(String s)
	{
		super(s);
		throw new WicketRuntimeException(JsonConstants.OPEN_JSON_EXCEPTION);
	}

	public String nextCDATA() throws JSONException
	{
		throw new WicketRuntimeException(JsonConstants.OPEN_JSON_EXCEPTION);
	}

	public Object nextContent() throws JSONException
	{
		throw new WicketRuntimeException(JsonConstants.OPEN_JSON_EXCEPTION);
	}

	public Object nextEntity(char ampersand) throws JSONException
	{
		throw new WicketRuntimeException(JsonConstants.OPEN_JSON_EXCEPTION);
	}

	public Object nextMeta() throws JSONException
	{
		throw new WicketRuntimeException(JsonConstants.OPEN_JSON_EXCEPTION);
	}

	public Object nextToken() throws JSONException
	{
		throw new WicketRuntimeException(JsonConstants.OPEN_JSON_EXCEPTION);
	}

	public boolean skipPast(String to) throws JSONException
	{
		throw new WicketRuntimeException(JsonConstants.OPEN_JSON_EXCEPTION);
	}
}
