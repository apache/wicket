/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package wicket.behavior;

import java.util.ArrayList;
import java.util.List;

import wicket.Application;
import wicket.RequestCycle;
import wicket.ResourceReference;
import wicket.markup.html.IHeaderContributor;
import wicket.markup.html.IHeaderResponse;
import wicket.markup.html.resources.CompressedResourceReference;
import wicket.protocol.http.WebRequestCycle;

/**
 * A {@link wicket.behavior.AbstractHeaderContributor} behavior that is
 * specialized on package resources. If you use this class, you have to
 * pre-register the resources you want to contribute. A shortcut for common
 * cases is to call {@link #forCss(Class, String)} to contribute a package css
 * file or {@link #forJavaScript(Class, String)} to contribute a packaged
 * javascript file. For instance:
 * 
 * <pre>
 * add(HeaderContributor.forCss(MyPanel.class, &quot;mystyle.css&quot;));
 * </pre>
 * 
 * @author Eelco Hillenius
 * @author Matej Knopp
 */
public class HeaderContributor extends AbstractHeaderContributor
{
	// adds the context path on the front of the location, if it's not
	// a fully-qualified URL.
	private static final String returnLocationWithContextPath(String location)
	{

		// WICKET-59 allow external URLs.
		if (location.startsWith("http://") || location.startsWith("https://"))
		{
			return location;
		}
		else
		{
			StringBuffer b = new StringBuffer();
			String contextPath = Application.get().getApplicationSettings().getContextPath();
			if (contextPath == null)
			{
				contextPath = ((WebRequestCycle)RequestCycle.get()).getWebRequest()
						.getContextPath();
				if (contextPath == null)
				{
					contextPath = "";
				}
			}
			b.append(contextPath);
			if (!contextPath.endsWith("/") && !location.startsWith("/"))
			{
				b.append("/");
			}
			b.append(location);
			return b.toString();
		}
	}

	private static final long serialVersionUID = 1L;

	/**
	 * Returns a new instance of {@link HeaderContributor} with a header
	 * contributor that references a CSS file that lives in the web application
	 * directory and that is addressed relative to the context path.
	 * 
	 * @param location
	 *            The location of the css file relative to the context path
	 * @param media
	 *            The media type for this CSS ("print", "screen", etc.)
	 * @return the new header contributor instance
	 */
	public static final HeaderContributor forCss(final String location, final String media)
	{
		return new HeaderContributor(new IHeaderContributor()
		{
			private static final long serialVersionUID = 1L;

			public void renderHead(IHeaderResponse response)
			{
				response.renderCSSReference(returnLocationWithContextPath(location), media);
			}
		});
	}

	/**
	 * Returns a new instance of {@link HeaderContributor} with a header
	 * contributor that references a CSS file that lives in the web application
	 * directory and that is addressed relative to the context path.
	 * 
	 * @param location
	 *            The location of the css file relative to the context path
	 * @return the new header contributor instance
	 */
	public static final HeaderContributor forCss(final String location)
	{
		return new HeaderContributor(new IHeaderContributor()
		{
			private static final long serialVersionUID = 1L;

			public void renderHead(IHeaderResponse response)
			{
				response.renderCSSReference(returnLocationWithContextPath(location));
			}
		});
	}

	/**
	 * Returns a new instance of {@link HeaderContributor} with a header
	 * contributor that references a CSS file that lives in a package.
	 * 
	 * @param reference
	 * @param media
	 *            The media type for this CSS ("print", "screen", etc.)
	 * @return the new header contributor instance
	 */
	public static final HeaderContributor forCss(final ResourceReference reference,
			final String media)
	{
		return new HeaderContributor(new IHeaderContributor()
		{
			private static final long serialVersionUID = 1L;

			public void renderHead(IHeaderResponse response)
			{
				response.renderCSSReference(reference, media);
			}
		});
	}

	/**
	 * Returns a new instance of {@link HeaderContributor} with a header
	 * contributor that references a CSS file that lives in a package.
	 * 
	 * @param reference
	 * 
	 * @return the new header contributor instance
	 */
	public static final HeaderContributor forCss(final ResourceReference reference)
	{
		return new HeaderContributor(new IHeaderContributor()
		{
			private static final long serialVersionUID = 1L;

			public void renderHead(IHeaderResponse response)
			{
				response.renderCSSReference(reference);
			}
		});
	}

	/**
	 * Returns a new instance of {@link HeaderContributor} with a header
	 * contributor that references a JavaScript file that lives in the web
	 * application directory and that is addressed relative to the context path.
	 * 
	 * @param location
	 *            The location of the css file relative to the context path
	 * @return the new header contributor instance
	 */
	public static final HeaderContributor forJavaScript(final String location)
	{
		return new HeaderContributor(new IHeaderContributor()
		{
			private static final long serialVersionUID = 1L;

			public void renderHead(IHeaderResponse response)
			{
				response.renderJavascriptReference(returnLocationWithContextPath(location));
			}
		});
	}

	/**
	 * Returns a new instance of {@link HeaderContributor} with a header
	 * contributor that references a CSS file that lives in a package.
	 * 
	 * @param scope
	 *            The scope of the package resource (typically the class of the
	 *            caller, or a class that lives in the package where the
	 *            resource lives).
	 * @param path
	 *            The path
	 * @param media
	 *            The media type for this CSS ("print", "screen", etc.)
	 * @return the new header contributor instance
	 */
	public static final HeaderContributor forCss(final Class scope, final String path,
			final String media)
	{
		return new HeaderContributor(new IHeaderContributor()
		{
			private static final long serialVersionUID = 1L;

			public void renderHead(IHeaderResponse response)
			{
				CompressedResourceReference reference = new CompressedResourceReference(scope, path);
				response.renderCSSReference(reference);
			}
		});
	}

	/**
	 * Returns a new instance of {@link HeaderContributor} with a header
	 * contributor that references a CSS file that lives in a package.
	 * 
	 * @param scope
	 *            The scope of the package resource (typically the class of the
	 *            caller, or a class that lives in the package where the
	 *            resource lives).
	 * @param path
	 *            The path
	 * @return the new header contributor instance
	 */
	public static final HeaderContributor forCss(final Class scope, final String path)
	{
		return new HeaderContributor(new IHeaderContributor()
		{
			private static final long serialVersionUID = 1L;

			public void renderHead(IHeaderResponse response)
			{
				CompressedResourceReference reference = new CompressedResourceReference(scope, path);
				response.renderCSSReference(reference);
			}
		});
	}

	/**
	 * Returns a new instance of {@link HeaderContributor} with a header
	 * contributor that references a java script file that lives in a package.
	 * 
	 * @param scope
	 *            The scope of the package resource (typically the class of the
	 *            caller, or a class that lives in the package where the
	 *            resource lives).
	 * @param path
	 *            The path
	 * @return the new header contributor instance
	 */
	public static final HeaderContributor forJavaScript(final Class scope, final String path)
	{
		return new HeaderContributor(new IHeaderContributor()
		{
			private static final long serialVersionUID = 1L;

			public void renderHead(IHeaderResponse response)
			{
				CompressedResourceReference reference = new CompressedResourceReference(scope, path);
				response.renderJavascriptReference(reference);
			}
		});
	}

	/**
	 * Returns a new instance of {@link HeaderContributor} with a header
	 * contributor that references a java script file that lives in a package.
	 * 
	 * @param reference
	 * 
	 * @return the new header contributor instance
	 */
	public static final HeaderContributor forJavaScript(final ResourceReference reference)
	{
		return new HeaderContributor(new IHeaderContributor()
		{
			private static final long serialVersionUID = 1L;

			public void renderHead(IHeaderResponse response)
			{
				response.renderJavascriptReference(reference);
			}
		});
	}

	/**
	 * set of resource references to contribute.
	 */
	private List headerContributors = null;

	/**
	 * Construct.
	 */
	public HeaderContributor()
	{
	}

	/**
	 * Construct with a single header contributor.
	 * 
	 * @param headerContributor
	 *            the header contributor
	 */
	public HeaderContributor(IHeaderContributor headerContributor)
	{
		headerContributors = new ArrayList(1);
		headerContributors.add(headerContributor);
	}

	/**
	 * Adds a custom header contributor.
	 * 
	 * @param headerContributor
	 *            instance of {@link IHeaderContributor}
	 */
	public final void addContributor(final IHeaderContributor headerContributor)
	{
		checkHeaderContributors();
		if (!headerContributors.contains(headerContributor))
		{
			headerContributors.add(headerContributor);
		}
	}

	/**
	 * Adds a custom header contributor at the given position.
	 * 
	 * @param index
	 *            the position where the contributor should be added (e.g. 0 to
	 *            put it in front of the rest).
	 * @param headerContributor
	 *            instance of {@link IHeaderContributor}
	 * 
	 * @throws IndexOutOfBoundsException
	 *             if the index is out of range (index &lt; 0 || index &gt;
	 *             size()).
	 */
	public final void addContributor(final int index, final IHeaderContributor headerContributor)
	{
		checkHeaderContributors();
		if (!headerContributors.contains(headerContributor))
		{
			headerContributors.add(index, headerContributor);
		}
	}

	/**
	 * Adds a reference to a css file that should be contributed to the page
	 * header.
	 * 
	 * @param scope
	 *            The scope of the package resource (typically the class of the
	 *            caller, or a class that lives in the package where the
	 *            resource lives).
	 * @param path
	 *            The path
	 */
	public final void addCssReference(final Class scope, final String path)
	{
		addContributor(new IHeaderContributor()
		{
			private static final long serialVersionUID = 1L;

			public void renderHead(IHeaderResponse response)
			{
				ResourceReference reference = new CompressedResourceReference(scope, path);
				response.renderCSSReference(reference);
			}
		});
	}

	/**
	 * Adds a reference to a javascript file that should be contributed to the
	 * page header.
	 * 
	 * @param scope
	 *            The scope of the package resource (typically the class of the
	 *            caller, or a class that lives in the package where the
	 *            resource lives).
	 * @param path
	 *            The path
	 */
	public final void addJavaScriptReference(final Class scope, final String path)
	{
		addContributor(new IHeaderContributor()
		{
			private static final long serialVersionUID = 1L;

			public void renderHead(IHeaderResponse response)
			{
				ResourceReference reference = new CompressedResourceReference(scope, path);
				response.renderJavascriptReference(reference);
			}
		});
	}

	/**
	 * @see wicket.behavior.AbstractHeaderContributor#getHeaderContributors()
	 */
	public final IHeaderContributor[] getHeaderContributors()
	{
		if (headerContributors != null)
		{
			return (IHeaderContributor[]) headerContributors.toArray(new IHeaderContributor[headerContributors.size()]);
		}
		return null;
	}

	/**
	 * Create lazily to save memory.
	 */
	private void checkHeaderContributors()
	{
		if (headerContributors == null)
		{
			headerContributors = new ArrayList(1);
		}
	}
}