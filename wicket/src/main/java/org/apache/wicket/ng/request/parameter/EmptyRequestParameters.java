package org.apache.wicket.ng.request.parameter;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.wicket.ng.request.RequestParameters;
import org.apache.wicket.ng.util.string.StringValue;

public class EmptyRequestParameters implements RequestParameters
{

	private EmptyRequestParameters()
	{
	}
	
	public static EmptyRequestParameters INSTANCE = new EmptyRequestParameters();

	public Set<String> getParameterNames()
	{
		return Collections.emptySet();
	}

	public StringValue getParameterValue(String name)
	{
		return StringValue.valueOf((String)null);
	}

	public List<StringValue> getParameterValues(String name)
	{
		return null;
	}

}
