/*
 * $Id: ContainerWithAssociatedMarkupHelper.java,v 1.1 2006/03/10 22:20:42
 * jdonnerstag Exp $ $Revision$ $Date: 2006/03/10 22:20:42 $
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
package wicket.markup.html.autolink;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.Component;
import wicket.MarkupContainer;
import wicket.Page;
import wicket.WicketRuntimeException;
import wicket.WicketTestCase;
import wicket.application.IClassResolver;
import wicket.markup.resolver.AutoLinkResolver;
import wicket.markup.resolver.AutoLinkResolver.AbstractAutolinkResolverDelegate;
import wicket.markup.resolver.AutoLinkResolver.AutolinkBookmarkablePageLink;
import wicket.markup.resolver.AutoLinkResolver.PathInfo;
import wicket.util.lang.Packages;
import wicket.util.string.Strings;

/**
 * 
 */
public class AutolinkTest extends WicketTestCase
{
	/** Logging */
	private static final Log log = LogFactory.getLog(AutoLinkResolver.class);
	
	/**
	 * Construct.
	 * @param name
	 */
	public AutolinkTest(String name)
	{
		super(name);
	}

	/**
	 * TODO Autolink: see https://sourceforge.net/tracker/index.php?func=detail&aid=1448200&group_id=119783&atid=684975
	 * The AnchorResolverDelegate implementation solves the problem. In that
	 * context it actually is not a test, rather than a solution which has not yet
	 * found its way into the core.
	 * 
	 * @throws Exception
	 */
	public void test_1() throws Exception
	{
		AutoLinkResolver.addTagReferenceResolver("a", "href", new AnchorResolverDelegate());
		executeTest(MyPage.class, "MyPageExpectedResult.html");
	}

	/** 
	 * This is copied from AutoLinkResolver with some minor extensions
	 * to handle wicket:link in page base markup correct.
	 * 
	 */
	private static final class AnchorResolverDelegate extends AbstractAutolinkResolverDelegate
	{
		/**
		 * Set of supported extensions for creating bookmarkable page links.
		 * Anything that is not in this list will be handled as a resource
		 * reference.
		 */
		private static final Set supportedPageExtensions = new HashSet(4);

		static
		{
			/**
			 * Initialize supported list of file name extension which'll create
			 * bookmarkable pages
			 */
			supportedPageExtensions.add("html");
			supportedPageExtensions.add("xml");
			supportedPageExtensions.add("wml");
			supportedPageExtensions.add("svg");
		}

		/**
		 * @see wicket.markup.resolver.AutoLinkResolver.IAutolinkResolverDelegate#newAutoComponent(wicket.MarkupContainer,
		 *      java.lang.String,
		 *      wicket.markup.resolver.AutoLinkResolver.PathInfo)
		 */
		public Component newAutoComponent(final MarkupContainer container, final String autoId,
				PathInfo pathInfo)
		{
			if (pathInfo.getExtension() != null && supportedPageExtensions.contains(pathInfo.getExtension()))
			{
				// Obviously a href like href="myPkg.MyLabel.html" will do as
				// well. Wicket will not throw an exception. It accepts it.
				String infoPath = Strings.replaceAll(pathInfo.getPath(), "/", ".");

				Page page = container.getPage();
				final IClassResolver defaultClassResolver = page.getApplication()
						.getApplicationSettings().getClassResolver();

				String className;
				if (!infoPath.startsWith("."))
				{
					// Href is relative. Resolve the url given relative to the
					// current page
					className = Packages.extractPackageName(page.getClass()) + "." + infoPath;
				}
				else
				{
					// Href is absolute. If class with the same absolute path
					// exists, use it. Else don't change the href.
					className = infoPath.substring(1);
				}

				try
				{
					final Class clazz = defaultClassResolver.resolveClass(className);
					return new AutolinkBookmarkablePageLink(autoId, clazz, pathInfo.getPageParameters());
				}
				catch (WicketRuntimeException ex)
				{
					log.warn("Did not find corresponding java class: " + className);
					// fall through
				}

				// >>>>>>>>> Start
				MarkupContainer parentWithContainer = container.findParentWithAssociatedMarkup();
				if ((parentWithContainer instanceof Page) && !infoPath.startsWith("."))
				{
					Class clazz = page.getClass();
					if (page.getMarkupStream().isMergedMarkup())
					{
						clazz = container.getMarkupStream().getTag().getMarkupClass();
					}
					// Href is relative. Resolve the url given relative to the
					// current page
					className = Packages.extractPackageName(clazz) + "." + infoPath;
	
					try
					{
						clazz = defaultClassResolver.resolveClass(className);
						return new AutolinkBookmarkablePageLink(autoId, clazz, pathInfo.getPageParameters());
					}
					catch (WicketRuntimeException ex)
					{
						log.warn("Did not find corresponding java class: " + className);
						// fall through
					}
				}
				// <<<<<<<<< End
			}
			else
			{
				// not a registered type for bookmarkable pages; create a link
				// to a resource instead
				return newPackageResourceReferenceAutoComponent(container, autoId, pathInfo);
			}

			// fallthrough
			return null;
		}
	}
}
