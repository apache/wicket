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
import java.util.regex.Pattern;

import wicket.Application;
import wicket.Component;
import wicket.RequestCycle;
import wicket.ResourceReference;
import wicket.Response;
import wicket.markup.html.IHeaderContributor;
import wicket.markup.html.IHeaderResponse;
import wicket.markup.html.PackageResource;
import wicket.model.AbstractReadOnlyModel;
import wicket.protocol.http.WebRequestCycle;
import wicket.util.string.AppendingStringBuffer;
import wicket.util.string.JavascriptUtils;

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
 */
// TODO Cache pattern results, at least the ones that were fetched by callin the
// javascript or css methods. The cache could be put in an application scoped
// meta data object to avoid the use of a static map
public class HeaderContributor extends AbstractHeaderContributor
{
	/**
	 * Returns a new instance of {@link HeaderContributor} with a set of header
	 * contributors that reference all CSS files with extension 'css' that live
	 * in a package.
	 * 
	 * @param scope
	 *            The scope of the package resource (typically the class of the
	 *            caller, or a class that lives in the package where the
	 *            resource lives).
	 * @return the new header contributor instance
	 * @deprecated Will be removed in 2.0; contribute resources one by one
	 *             instead
	 */
	public static final HeaderContributor forCss(final Class scope)
	{
		return forCss(scope, PackageResource.EXTENSION_CSS);
	}

	/**
	 * Returns a new instance of {@link HeaderContributor} with a set of header
	 * contributors that reference all javascript files with extension 'js' that
	 * live in a package.
	 * 
	 * @param scope
	 *            The scope of the package resource (typically the class of the
	 *            caller, or a class that lives in the package where the
	 *            resource lives).
	 * @return the new header contributor instance
	 * @deprecated Will be removed in 2.0; contribute resources one by one
	 *             instead
	 */
	public static final HeaderContributor forJavaScript(final Class scope)
	{
		return forJavaScript(scope, PackageResource.EXTENSION_JS);
	}

	/**
	 * Returns a new instance of {@link HeaderContributor} with a set of header
	 * contributors that reference CSS files that match the pattern and that
	 * live in a package.
	 * 
	 * @param scope
	 *            The scope of the package resource (typically the class of the
	 *            caller, or a class that lives in the package where the
	 *            resource lives).
	 * @param pattern
	 *            The regexp pattern to match resources on
	 * @return the new header contributor instance
	 * @deprecated Will be removed in 2.0; contribute resources one by one
	 *             instead
	 */
	public static final HeaderContributor forCss(final Class scope, final Pattern pattern)
	{
		return forCss(scope, pattern, false);
	}

	/**
	 * Returns a new instance of {@link HeaderContributor} with a set of header
	 * contributors that reference CSS files that match the pattern and that
	 * live in a package and its sub packages in case recurse is true.
	 * 
	 * @param scope
	 *            The scope of the package resource (typically the class of the
	 *            caller, or a class that lives in the package where the
	 *            resource lives).
	 * @param pattern
	 *            The regexp pattern to match resources on
	 * @param recurse
	 *            whether to recurse into sub packages
	 * @return the new header contributor instance
	 * @deprecated Will be removed in 2.0; contribute resources one by one
	 *             instead
	 */
	public static final HeaderContributor forCss(final Class scope, final Pattern pattern,
			boolean recurse)
	{
		PackageResource[] resources = PackageResource.get(scope, pattern, recurse);
		HeaderContributor contributor = new HeaderContributor();
		if (resources != null)
		{
			int len = resources.length;
			for (int i = 0; i < len; i++)
			{
				contributor.addContributor(new CSSReferenceHeaderContributor(scope, resources[i]
						.getPath(), null));
			}
		}
		return contributor;
	}

	/**
	 * Returns a new instance of {@link HeaderContributor} with a set of header
	 * contributors that references java script files that match the pattern and
	 * that live in a package.
	 * 
	 * @param scope
	 *            The scope of the package resource (typically the class of the
	 *            caller, or a class that lives in the package where the
	 *            resource lives).
	 * @param pattern
	 *            The regexp pattern to match resources on
	 * @return the new header contributor instance
	 * @deprecated Will be removed in 2.0; contribute resources one by one
	 *             instead
	 */
	public static final HeaderContributor forJavaScript(final Class scope, final Pattern pattern)
	{
		return forJavaScript(scope, pattern, false);
	}

	/**
	 * Returns a new instance of {@link HeaderContributor} with a set of header
	 * contributors that references java script files that match the pattern and
	 * that live in a package and sub packages in case recurse is true.
	 * 
	 * @param scope
	 *            The scope of the package resource (typically the class of the
	 *            caller, or a class that lives in the package where the
	 *            resource lives).
	 * @param pattern
	 *            The regexp pattern to match resources on
	 * @param recurse
	 *            whether to recurse into sub packages
	 * @return the new header contributor instance
	 * @deprecated Will be removed in 2.0; contribute resources one by one
	 *             instead
	 */
	public static final HeaderContributor forJavaScript(final Class scope, final Pattern pattern,
			boolean recurse)
	{
		PackageResource[] resources = PackageResource.get(scope, pattern, recurse);
		HeaderContributor contributor = new HeaderContributor();
		if (resources != null)
		{
			int len = resources.length;
			for (int i = 0; i < len; i++)
			{
				contributor.addContributor(new JavaScriptReferenceHeaderContributor(scope,
						resources[i].getPath()));
			}
		}
		return contributor;
	}

	/**
	 * Contributes a reference to a css file relative to the context path.
	 */
	public static final class CSSHeaderContributor extends StringHeaderContributor
	{
		private static final long serialVersionUID = 1L;

		/**
		 * Construct.
		 * 
		 * @param location
		 *            the location of the CSS file relative to the context path.
		 */
		public CSSHeaderContributor(final String location)
		{
			this(location, null);
		}
		
		/**
		 * Construct.
		 * 
		 * @param location
		 *            the location of the CSS file relative to the context path.
		 * @param media
		 *            the media type for this CSS ("print", "screen", etc.)
		 */
		public CSSHeaderContributor(final String location, final String media)
		{
			super(new AbstractReadOnlyModel()
			{

				private static final long serialVersionUID = 1L;
				private String path = null;

				public Object getObject(Component component)
				{
					if (path == null)
					{
						String contextPath = Application.get().getApplicationSettings()
								.getContextPath();
						if (contextPath == null)
						{
							contextPath = ((WebRequestCycle)RequestCycle.get()).getWebRequest()
									.getContextPath();
							if (contextPath == null)
							{
								contextPath = "";
							}
						}
						AppendingStringBuffer b = new AppendingStringBuffer();
						b.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"");
						b.append(contextPath);
						if (!contextPath.endsWith("/") && !location.startsWith("/"))
						{
							b.append("/");
						}
						b.append((location != null) ? location : "");
						if (media != null)
						{
							b.append("\" media=\"");
							b.append(media);
						}
						b.append("\" />");
						path = b.toString();
					}
					return path;
				}
			});
		}
	}

	/**
	 * prints a css resource reference.
	 */
	private static final class CSSReferenceHeaderContributor
			extends
				ResourceReferenceHeaderContributor
	{
		private static final long serialVersionUID = 1L;

		private String media;

		/**
		 * Construct.
		 * 
		 * @param scope
		 *            The scope of the reference (typically the calling class)
		 * @param name
		 *            The name of the reference.
		 * @param media
		 *            The media type for this CSS ("print", "screen", etc.)
		 */
		public CSSReferenceHeaderContributor(Class scope, String name, String media)
		{
			super(scope, name);
			this.media = media;
		}

		/**
		 * Construct.
		 * 
		 * @param reference
		 * @param media
		 *            The media type for this CSS ("print", "screen", etc.)
		 */
		public CSSReferenceHeaderContributor(ResourceReference reference, String media)
		{
			super(reference);
			this.media = media;
		}

		/**
		 * @see wicket.markup.html.IHeaderContributor#renderHead(wicket.Response)
		 */
		public void renderHead(IHeaderResponse headerResponse)
		{
			Response response = headerResponse.getResponse();
			final CharSequence url = RequestCycle.get().urlFor(getResourceReference());
			response.write("<link rel=\"stylesheet\" type=\"text/css\" href=\"");
			response.write(url);
			if (media != null)
			{
				response.write("\" media=\"");
				response.write(media);
			}
			response.println("\" />");
		}
	}

	/**
	 * Contributes a reference to a javascript file relative to the context
	 * path.
	 */
	public static final class JavaScriptHeaderContributor extends StringHeaderContributor
	{
		private static final long serialVersionUID = 1L;

		/**
		 * Construct.
		 * 
		 * @param location
		 *            the location of the CSS file relative to the context path.
		 */
		public JavaScriptHeaderContributor(final String location)
		{
			super(new AbstractReadOnlyModel()
			{

				private static final long serialVersionUID = 1L;
				private String path = null;

				public Object getObject(Component component)
				{
					if (path == null)
					{
						String contextPath = Application.get().getApplicationSettings()
								.getContextPath();
						if (contextPath == null)
						{
							contextPath = ((WebRequestCycle)RequestCycle.get()).getWebRequest()
									.getContextPath();
							if (contextPath == null)
							{
								contextPath = "";
							}
						}
						AppendingStringBuffer b = new AppendingStringBuffer();
						b.append("<script type=\"text/javascript\" src=\"");
						b.append(contextPath);
						if (!contextPath.endsWith("/") && !location.startsWith("/"))
						{
							b.append("/");
						}
						b.append((location != null) ? location : "");
						b.append("\"></script>");
						path = b.toString();
					}
					return path;
				}
			});
		}
	}

	/**
	 * prints a javascript resource reference.
	 */
	public static final class JavaScriptReferenceHeaderContributor
			extends
				ResourceReferenceHeaderContributor
	{
		private static final long serialVersionUID = 1L;

		/**
		 * Construct.
		 * 
		 * @param scope
		 *            The scope of the reference (typically the calling class)
		 * @param name
		 *            The name .
		 */
		public JavaScriptReferenceHeaderContributor(Class scope, String name)
		{
			super(scope, name);
		}

		/**
		 * Construct.
		 * 
		 * @param reference
		 */
		public JavaScriptReferenceHeaderContributor(ResourceReference reference)
		{
			super(reference);
		}

		/**
		 * @see wicket.markup.html.IHeaderContributor#renderHead(wicket.Response)
		 */
		public void renderHead(IHeaderResponse response)
		{
			final CharSequence url = RequestCycle.get().urlFor(getResourceReference());
			JavascriptUtils.writeJavascriptUrl(response.getResponse(), url);
		}
	}

	/**
	 * Wraps a {@link ResourceReference} and knows how to print a header
	 * statement based on that resource. Default implementations are
	 * {@link JavaScriptReferenceHeaderContributor} and
	 * {@link CSSReferenceHeaderContributor}, which print out javascript
	 * statements and css ref statements respectively.
	 */
	public static abstract class ResourceReferenceHeaderContributor implements IHeaderContributor
	{
		private static final long serialVersionUID = 1L;

		/** the pre calculated hash code. */
		private final int hash;

		/** the package resource reference. */
		private final ResourceReference resourceReference;

		/**
		 * Construct.
		 * 
		 * @param scope
		 *            The scope of the reference (typically the calling class)
		 * @param name
		 *            The name of the reference (typically the name of the
		 *            packaged resource, like 'myscripts.js').
		 */
		public ResourceReferenceHeaderContributor(Class scope, String name)
		{
			this(new ResourceReference(scope, name));
		}

		/**
		 * Construct.
		 * 
		 * @param reference
		 */
		public ResourceReferenceHeaderContributor(ResourceReference reference)
		{
			this.resourceReference = reference;
			int result = 17;
			result = 37 * result + getClass().hashCode();
			result = 37 * result + resourceReference.hashCode();
			hash = result;
		}

		/**
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		public boolean equals(Object obj)
		{
			if (obj.getClass().equals(getClass()))
			{
				ResourceReferenceHeaderContributor that = (ResourceReferenceHeaderContributor)obj;
				return this.resourceReference.equals(that.resourceReference);
			}
			return false;
		}

		/**
		 * @see java.lang.Object#hashCode()
		 */
		public int hashCode()
		{
			return hash;
		}

		/**
		 * Gets the package resource reference.
		 * 
		 * @return the package resource reference
		 */
		protected final ResourceReference getResourceReference()
		{
			return resourceReference;
		}
	}

	private static final long serialVersionUID = 1L;

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
	public static final HeaderContributor forCss(final Class scope, final String path, final String media)
	{
		return new HeaderContributor(new CSSReferenceHeaderContributor(scope, path, media));
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
		return new HeaderContributor(new CSSReferenceHeaderContributor(scope, path, null));
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
	public static final HeaderContributor forCss(ResourceReference reference, final String media)
	{
		return new HeaderContributor(new CSSReferenceHeaderContributor(reference, media));
	}
	
	/**
	 * Returns a new instance of {@link HeaderContributor} with a header
	 * contributor that references a CSS file that lives in a package.
	 * 
	 * @param reference
	 * 
	 * @return the new header contributor instance
	 */
	public static final HeaderContributor forCss(ResourceReference reference)
	{
		return new HeaderContributor(new CSSReferenceHeaderContributor(reference, null));
	}

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
		return new HeaderContributor(new CSSHeaderContributor(location, media));		
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
		return new HeaderContributor(new CSSHeaderContributor(location, null));
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
		return new HeaderContributor(new JavaScriptReferenceHeaderContributor(scope, path));
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
		return new HeaderContributor(new JavaScriptReferenceHeaderContributor(reference));
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
		return new HeaderContributor(new JavaScriptHeaderContributor(location));
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
		addContributor(new CSSReferenceHeaderContributor(scope, path, null));
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
		addContributor(new JavaScriptReferenceHeaderContributor(scope, path));
	}

	/**
	 * @see wicket.behavior.AbstractHeaderContributor#getHeaderContributors()
	 */
	public final IHeaderContributor[] getHeaderContributors()
	{
		if (headerContributors != null)
		{
			return (IHeaderContributor[])headerContributors
					.toArray(new IHeaderContributor[headerContributors.size()]);
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