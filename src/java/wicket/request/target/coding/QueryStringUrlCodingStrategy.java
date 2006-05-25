package wicket.request.target.coding;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import wicket.Page;
import wicket.PageMap;
import wicket.util.string.AppendingStringBuffer;
import wicket.util.string.Strings;
import wicket.util.value.ValueMap;

/**
 * Encodes and decodes mounts for a single bookmarkable page class, but with the
 * parameters appended in a URL query string rather than integrated into a URL
 * hierarchical path.
 * <p>
 * For example, whereas
 * {@link wicket.request.target.coding.BookmarkablePageRequestTargetUrlCodingStrategy BookmarkablePageRequestTargetUrlCodingStrategy}
 * might encode a request target as
 * "mywebapp/myservlet/admin/productmanagement/action/edit/product/4995",
 * <code>QueryStringRequestTargetUrlCodingStrategy</code> would encode the
 * same target as
 * "mywebapp/myservlet/admin/productmanagement?action=edit&amp;product=4995".
 * <p>
 * URLs encoded in this way can be bookmarked just as easily as those produced
 * by <code>BookmarkablePageRequestTargetUrlCodingStrategy</code>. For
 * example, Google searches produce bookmarkable links with query strings.
 * <p>
 * Whether <code>BookmarkablePageRequestTargetUrlCodingStrategy</code> or
 * <code>QueryStringRequestTargetUrlCodingStrategy</code> is appropriate for a
 * given mount depends on:
 * <ul>
 * <li>Aesthetic criteria
 * <li>Interpretations of <a
 * href="http://www.gbiv.com/protocols/uri/rfc/rfc3986.html">RFC 3986</a>. This
 * defines the URI standard, including query strings, and states that whereas
 * the "path component contains data, usually organized in hierarchical form
 * [divided by slashes]", the "query component [after the question mark]
 * contains non-hierarchical data".
 * <li>Findability. Public search engines prefer URLs with parameters stored
 * hierarchically or in a shorter query string. Google's <a
 * href="http://www.google.com/support/webmasters/bin/answer.py?answer=35770">Design
 * and Content Guidelines</a> (as of May 6 2006) state: "Make a site with a
 * clear hierarchy and text links. Every page should be reachable from at least
 * one static text link. &#8230; If you decide to use dynamic pages (i.e., the
 * URL contains a '?' character), be aware that not every search engine spider
 * crawls dynamic pages as well as static pages. It helps to keep the parameters
 * short and the number of them few."
 * <li>The complexity of the parameters being passed. More complex parameters
 * may make more sense expressed as a series of "key=value(s)" pairs in a query
 * string than shoehorned into a hierarchical structure.
 * </ul>
 * <p>
 * Regardless of which coding strategy is chosen for the mount,
 * {@link wicket.markup.html.link.BookmarkablePageLink BookmarkablePageLink} can
 * be used to insert a bookmarkable link to the request target.
 * <p>
 * This example demonstrates how to mount a path with
 * <code>QueryStringRequestTargetUrlCodingStrategy</code> within the
 * <code>init</code> method of a class implementing
 * {@link wicket.protocol.http.WebApplication WebApplication}:
 * <p>
 * <code>mount("/admin/productmanagement", new 
 * QueryStringRequestTargetUrlCodingStrategy("/admin/productmanagement",
 * admin.ProductManagement.class));</code>
 * <p>
 * Note that, as with the main BookmarkablePageRequestTargetUrlCodingStrategy,
 * if the output of this coding strategy is passed through
 * {@link javax.servlet.http.HttpServletResponse#encodeURL(java.lang.String) HttpServletResponse.encodeURL}
 * and the client has cookies turned off, the client's session ID will be stored
 * in a path parameter, like so:
 * "/mywebapp/myservlet/admin/productmanagement;jsessionid=730EC527564AF1C73F8C2FB19B604F55?action=edit&amp;product=4995".
 * 
 * @author Benjamin Hawkes-Lewis
 */
public class QueryStringUrlCodingStrategy extends BookmarkablePageRequestTargetUrlCodingStrategy
{

	/**
	 * Sole constructor.
	 * 
	 * @param mountPath
	 *            the relative reference URL on which the page is mounted
	 * @param bookmarkablePageClass
	 *            the class of the mounted page
	 */
	public QueryStringUrlCodingStrategy(final String mountPath,
			final Class<? extends Page> bookmarkablePageClass)
	{
		super(mountPath, bookmarkablePageClass, PageMap.DEFAULT_NAME);
	}

	/**
	 * Gets the encoded URL for the request target. Typically, the result will
	 * be prepended with a protocol specific prefix. In a servlet environment,
	 * the prefix concatenates the context path and the servlet path, for
	 * example "mywebapp/myservlet".
	 * 
	 * @param url
	 *            the relative reference URL
	 * @param parameters
	 *            parameter names mapped to parameter values
	 */
	@Override
	protected void appendParameters(AppendingStringBuffer url, Map parameters)
	{

		if (parameters != null && parameters.size() > 0)
		{
			boolean firstParam = url.indexOf("?") < 0;
			Iterator entries = parameters.entrySet().iterator();

			while (entries.hasNext())
			{
				Map.Entry entry = (Entry)entries.next();

				if (entry.getValue() != null)
				{

					String escapedValue = urlEncode(entry.getValue().toString());

					if (!Strings.isEmpty(escapedValue))
					{
						if (firstParam)
						{
							url.append("?"); /* Begin query string. */
							firstParam = false;
						}
						else
						{
							/*
							 * Separate new key=value(s) pair from previous pair
							 * with an ampersand.
							 */
							url.append("&");
						}

						/* Append key=value(s) pair. */
						url.append(entry.getKey());
						url.append("=");
						url.append(escapedValue);
					}
				}
			}
		}
	}

	/**
	 * Decodes parameters object from the provided query string
	 * 
	 * @param fragment
	 *            contains the query string
	 * @param passedParameters
	 *            paremeters decoded by wicket before this method - usually off
	 *            the query string
	 * 
	 * @return Parameters
	 */
	@Override
	protected ValueMap decodeParameters(String fragment,
			Map<String, ? extends Object> passedParameters)
	{
		ValueMap parameters = new ValueMap();

		if (passedParameters != null)
		{
			parameters.putAll(passedParameters);
		}

		return parameters;

	}

}
