package org.apache.wicket.request.mapper.parameter;

import java.util.Iterator;

import org.apache.wicket.request.Request;
import org.apache.wicket.request.Url;
import org.apache.wicket.util.lang.Args;


/**
 * Encodes page parameters into url path fragments instead of the query string like the default
 * {@link PageParametersEncoder}. The parameters are encoded in the following format:
 * {@code /param1Name/param1Value/param2Name/param2Value}.
 * <p>
 * This used to be the default way of encoding page parameters in 1.4.x applications. Newer 1.5.x+
 * applications use the query string, by default. This class faciliates backwards compatibility and
 * migrations of 1.4.x application to 1.5.x+ codebase.
 * <p>
 * Example usage:
 * {@code mount(new MountedMapper("/myPage", MyPage.class, new HybridPageParametersEncoder()); }
 * 
 * @author Chris Colman
 * @authour Luniv (on Stack Overflow)
 * @author ivaynberg
 */
public class UrlPathPageParametersEncoder implements IPageParametersEncoder
{

	public Url encodePageParameters(PageParameters params)
	{
		Args.notNull(params, "params");
		Args.isTrue(params.getIndexedCount() == 0,
			"This encoder does not support indexed page parameters. Specified parameters: %s",
			params);

		Url url = new Url();

		for (PageParameters.NamedPair pair : params.getAllNamed())
		{
			url.getSegments().add(pair.getKey());
			url.getSegments().add(pair.getValue());
		}

		return url;
	}

	public PageParameters decodePageParameters(Request request)
	{
		PageParameters params = new PageParameters();

		for (Iterator<String> segment = request.getUrl().getSegments().iterator(); segment.hasNext();)
		{
			String key = segment.next();
			String value = segment.next();

			params.add(key, value);
		}

		return params.isEmpty() ? null : params;
	}
}
