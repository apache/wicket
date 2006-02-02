package wicket.request.target.coding;

import wicket.PageMap;
import wicket.PageParameters;
import wicket.WicketRuntimeException;
import wicket.request.target.coding.BookmarkablePageRequestTargetUrlCodingStrategy;

/**
 * Url coding strategy for bookmarkable pages that encodes index based
 * parameters.
 * 
 * Strategy looks for parameters whose name is an integer in an incremented
 * order starting with zero. Found parameters will be appended to the url in the
 * form /mount-path/paramvalue0/paramvalue1/paramvalue2
 * 
 * When decoded these parameters will once again be available under their index (
 * PageParameters.getString("0"); )
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public class IndexedParamUrlCodingStrategy extends BookmarkablePageRequestTargetUrlCodingStrategy
{

	/**
	 * Construct.
	 * 
	 * @param mountPath
	 *            mount path
	 * @param bookmarkablePageClass
	 *            class of mounted page
	 */
	public IndexedParamUrlCodingStrategy(String mountPath, Class bookmarkablePageClass)
	{
		super(mountPath, bookmarkablePageClass, PageMap.DEFAULT_PAGEMAP_NAME);
	}

	/**
	 * Construct.
	 * 
	 * @param mountPath
	 *            mount path
	 * @param bookmarkablePageClass
	 *            class of mounted page
	 * @param pageMapName
	 *            name of pagemap
	 */
	public IndexedParamUrlCodingStrategy(String mountPath, Class bookmarkablePageClass,
			String pageMapName)
	{
		super(mountPath, bookmarkablePageClass, pageMapName);
	}

	protected void appendPageParameters(StringBuffer url, PageParameters parameters)
	{
		int i = 0;
		while (parameters.containsKey(String.valueOf(i)))
		{
			String value = parameters.getString(String.valueOf(i));
			url.append("/").append(urlEncode(value));
			i++;
		}

		if (i != parameters.size())
		{
			throw new WicketRuntimeException(
					"Not all parameters were encoded. Make sure all parameter names are integers in consecutive order starting with zero. Current parameter names are: "
							+ parameters.keySet().toString());
		}
	}

	protected PageParameters decodePageParameters(String urlFragment)
	{
		PageParameters params = new PageParameters();
		if (urlFragment == null)
		{
			return params;
		}
		if (urlFragment.startsWith("/"))
		{
			urlFragment = urlFragment.substring(1);
		}

		String[] parts = urlFragment.split("/");
		for (int i = 0; i < parts.length; i++)
		{
			params.put(String.valueOf(i), parts[i]);
		}
		return params;
	}

}
