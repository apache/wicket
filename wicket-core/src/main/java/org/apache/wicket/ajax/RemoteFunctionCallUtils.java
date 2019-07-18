package org.apache.wicket.ajax;

import com.github.openjson.JSONArray;
import com.github.openjson.JSONObject;
import org.apache.wicket.util.string.Strings;

import java.util.Arrays;
import java.util.stream.Collectors;

public class RemoteFunctionCallUtils
{
	public static String createFunctionJsonString(CharSequence functionName, Object... args)
	{
		return "{\"" +
				"func\":" + wrapAndEscape(functionName) + "," +
				"\"args\":[" +
				Arrays.stream(args)
				      .map(RemoteFunctionCallUtils::wrapAndEscape)
				      .collect(Collectors.joining(",")) +
				"]" +
				"}";
	}

	/**
	 * Currently works only with primitives
	 *
	 * @param arg
	 * @return
	 */
	public static String wrapAndEscape(Object arg)
	{
		if (arg instanceof Number || arg instanceof Boolean)
		{
			return String.valueOf(arg);
		}
		else if (arg instanceof JSONObject || arg instanceof JSONArray)
		{
			return arg.toString();
		}
		else
		{
			return "\"" + Strings.escapeMarkup(String.valueOf(arg)) + "\"";
		}
	}
}
