/*
 * $Id$
 * $Revision$ $Date$
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.behavior;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import wicket.RequestCycle;
import wicket.Response;
import wicket.markup.html.IHeaderContributor;
import wicket.markup.html.PackageResource;
import wicket.markup.html.PackageResourceReference;

/**
 * A {@link wicket.behavior.AbstractHeaderContributor} behavior that is
 * specialized on package resources. If you use this class, you have to
 * pre-register the resources you want to contribute. A shortcut for common
 * cases is to call {@link #addCssReference(Class, String)} to contribute a
 * package css file or {@link #addJavaScriptReference(Class, String)} to
 * contribute a packaged javascript file. For instance:
 * 
 * <pre>
 * add(HeaderContributor.forCssReference(MyPanel.class, &quot;mystyle.css&quot;));
 * </pre>
 * 
 * @author Eelco Hillenius
 */
// TODO Cache pattern results, at least the ones that were fetched by callin the
// javascript or css methods. The cache could be put in an application scoped
// meta data object to avoid the use of a static map
public class HeaderContributor extends AbstractHeaderContributor
{
	private static final long serialVersionUID = 1L;

	/**
	 * Wraps a {@link PackageResourceReference} and knows how to print a header
	 * statement based on that resource. Default implementations are
	 * {@link JavaScriptReferenceHeaderContributor} and
	 * {@link CSSReferenceHeaderContributor}, which print out javascript
	 * statements and css ref statements respectively.
	 */
	public static abstract class PackageResourceReferenceHeaderContributor
			implements
				IHeaderContributor
	{
		private static final long serialVersionUID = 1L;

		/** the package resource reference. */
		private final PackageResourceReference packageResourceReference;

		/** the pre calculated hash code. */
		private final int hash;

		/**
		 * Construct.
		 * 
		 * @param scope
		 *            The scope of the reference (typically the calling class)
		 * @param name
		 *            The name of the reference (typically the name of the
		 *            packaged resource, like 'myscripts.js').
		 */
		public PackageResourceReferenceHeaderContributor(Class scope, String name)
		{
			this.packageResourceReference = new PackageResourceReference(scope, name);
			int result = 17;
			result = 37 * result + getClass().hashCode();
			result = 37 * result + packageResourceReference.hashCode();
			hash = result;
		}

		/**
		 * @see java.lang.Object#hashCode()
		 */
		public int hashCode()
		{
			return hash;
		}

		/**
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		public boolean equals(Object obj)
		{
			if (obj.getClass().equals(getClass()))
			{
				PackageResourceReferenceHeaderContributor that = (PackageResourceReferenceHeaderContributor)obj;
				return this.packageResourceReference.equals(that.packageResourceReference);
			}
			return false;
		}

		/**
		 * Gets the package resource reference.
		 * 
		 * @return the package resource reference
		 */
		protected final PackageResourceReference getPackageResourceReference()
		{
			return packageResourceReference;
		}
	}

	/** prints a javascript resource reference. */
	public static final class JavaScriptReferenceHeaderContributor
			extends
				PackageResourceReferenceHeaderContributor
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
		 * @see wicket.markup.html.IHeaderContributor#renderHead(wicket.Response)
		 */
		public void renderHead(Response response)
		{
			String url = RequestCycle.get().urlFor(getPackageResourceReference());
			StringBuffer b = new StringBuffer();
			b.append("<script type=\"text/javascript\" " + "src=\"").append(url).append("\"></script>\n");
			response.write(b.toString());
		}
	}

	/** prints a css resource reference. */
	public static final class CSSReferenceHeaderContributor
			extends
				PackageResourceReferenceHeaderContributor
	{
		private static final long serialVersionUID = 1L;

		/**
		 * Construct.
		 * 
		 * @param scope
		 *            The scope of the reference (typically the calling class)
		 * @param name
		 *            The name of the reference.
		 */
		public CSSReferenceHeaderContributor(Class scope, String name)
		{
			super(scope, name);
		}

		/**
		 * @see wicket.markup.html.IHeaderContributor#renderHead(wicket.Response)
		 */
		public void renderHead(Response response)
		{
			String url = RequestCycle.get().urlFor(getPackageResourceReference());
			StringBuffer b = new StringBuffer();
			b.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"").append(url).append(
					"\"></link>\n");
			response.write(b.toString());
		}
	}

	/** set of resource references to contribute. */
	private Set headerContributors = null;

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
		headerContributors = new HashSet(1);
		headerContributors.add(headerContributor);
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
		return new HeaderContributor(new CSSReferenceHeaderContributor(scope, path));
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
	 */
	public static final HeaderContributor forCss(final Class scope, final Pattern pattern)
	{
		PackageResource[] resources = PackageResource.get(scope, pattern);
		HeaderContributor contributor = new HeaderContributor();
		if (resources != null)
		{
			int len = resources.length;
			for (int i = 0; i < len; i++)
			{
				contributor.addContributor(new CSSReferenceHeaderContributor(scope, resources[i]
						.getPath()));
			}
		}
		return contributor;
	}

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
	 */
	public static final HeaderContributor forCss(final Class scope)
	{
		return forCss(scope, PackageResource.EXTENSION_CSS);
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
	 */
	public static final HeaderContributor forJavaScript(final Class scope, final Pattern pattern)
	{
		PackageResource[] resources = PackageResource.get(scope, pattern);
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
	 * Returns a new instance of {@link HeaderContributor} with a set of header
	 * contributors that references all java script files with extension 'js'
	 * that live in a package.
	 * 
	 * @param scope
	 *            The scope of the package resource (typically the class of the
	 *            caller, or a class that lives in the package where the
	 *            resource lives).
	 * @return the new header contributor instance
	 */
	public static final HeaderContributor forJavaScript(final Class scope)
	{
		return forJavaScript(scope, PackageResource.EXTENSION_JS);
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
	 * Adds a custom header contributor.
	 * 
	 * @param headerContributor
	 *            instance of {@link IHeaderContributor}
	 */
	public final void addContributor(final IHeaderContributor headerContributor)
	{
		checkHeaderContributors();
		headerContributors.add(headerContributor);
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
		checkHeaderContributors();
		headerContributors.add(new CSSReferenceHeaderContributor(scope, path));
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
		checkHeaderContributors();
		headerContributors.add(new JavaScriptReferenceHeaderContributor(scope, path));
	}

	/**
	 * Create lazily to save memory.
	 */
	private void checkHeaderContributors()
	{
		if (headerContributors == null)
		{
			headerContributors = new HashSet();
		}
	}
}
