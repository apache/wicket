package org.apache.wicket.ng.request.parameter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.wicket.ng.request.RequestParameters;
import org.apache.wicket.ng.util.string.StringValue;

public class CombinedRequestParametersAdapter implements RequestParameters
{
	private final RequestParameters parameters[];

	public CombinedRequestParametersAdapter(RequestParameters ... parameters)
	{
		if (parameters == null)
		{
			throw new IllegalStateException("Argument 'parameters' may not be null");
		}
		this.parameters = parameters;
	}

	public Set<String> getParameterNames()
	{
		Set<String> result = new HashSet<String>();
		for (RequestParameters p : parameters)
		{
			result.addAll(p.getParameterNames());
		}
		return Collections.unmodifiableSet(result);
	}

	public StringValue getParameterValue(String name)
	{
		for (RequestParameters p : parameters)
		{
			StringValue value = p.getParameterValue(name);
			if (!value.isNull())
			{
				return value;
			}
		}
		return StringValue.valueOf((String)null);
	}

	public List<StringValue> getParameterValues(String name)
	{
		List<StringValue> result = new ArrayList<StringValue>();
		for (RequestParameters p : parameters)
		{
			List<StringValue> values = p.getParameterValues(name);
			if (values != null)
			{
				for (StringValue v : values)
				{
					if (!result.contains(v))
					{
						result.add(v);
					}
				}
			}
		}
		
		if (result.isEmpty())
		{
			return null;
		}
		else
		{
			return Collections.unmodifiableList(result);
		}
	}

}
