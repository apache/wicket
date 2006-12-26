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
package wicket.markup.resolver;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.Component;
import wicket.MarkupContainer;
import wicket.Page;
import wicket.PageParameters;
import wicket.ResourceReference;
import wicket.WicketRuntimeException;
import wicket.application.IClassResolver;
import wicket.markup.ComponentTag;
import wicket.markup.MarkupStream;
import wicket.markup.html.PackageResource;
import wicket.markup.html.WebMarkupContainer;
import wicket.markup.html.link.BookmarkablePageLink;
import wicket.markup.html.link.ExternalLink;
import wicket.markup.parser.filter.WicketLinkTagHandler;
import wicket.util.lang.Packages;
import wicket.util.string.Strings;

/**
 * The AutoLinkResolver is responsible to handle automatic link resolution. Tags
 * are marked "autolink" by the MarkupParser for all tags with href attribute,
 * such as anchor and link tags with no explicit wicket id. E.g. &lt;a
 * href="Home.html"&gt;
 * <p>
 * If href points to a *.html file, a BookmarkablePageLink will automatically be
 * created, except for absolut paths, where an ExternalLink is created.
 * <p>
 * If href points to a *.html file, it resolves the given URL by searching for a
 * page class, either relative or absolute, specified by the href attribute of
 * the tag. If relative the href URL must be relative to the package containing
 * the associated page. An exception is thrown if no Page class was found.
 * <p>
 * If href is no *.html file a static reference to the resource is created.
 * 
 * @see wicket.markup.parser.filter.WicketLinkTagHandler
 * 
 * @author Juergen Donnerstag
 * @author Eelco Hillenius
 */
public final class AutoLinkResolver implements IComponentResolver
{
	/**
	 * Abstract implementation that has a helper method for creating a resource
	 * reference.
	 */
	public static abstract class AbstractAutolinkResolverDelegate
			implements
				IAutolinkResolverDelegate
	{
		/**
		 * Creates a new auto component that references a package resource.
		 * 
		 * @param container
		 *            the parent container
		 * @param autoId
		 *            the automatically generated id for the auto component
		 * @param pathInfo
		 *            the path info object that contains information about the
		 *            link reference
		 * @param attribute
		 *            the attribute to replace the value of
		 * @return a new auto component or null if the path was absolute
		 */
		protected final Component newResourceReferenceAutoComponent(
				final MarkupContainer container, final String autoId, final PathInfo pathInfo,
				final String attribute)
		{
			if (!pathInfo.absolute)
			{
				// Href is relative. Create a resource reference pointing at
				// this file

				MarkupStream markupStream = container.getMarkupStream();
				// <wicket:head> components are handled differently. We can
				// not use the container, because it is the container the
				// header has been added to (e.g. the Page). What we need
				// however, is the component (e.g. a Panel) which
				// contributed it.
				Class clazz = markupStream.getContainerClass();

				ComponentTag tag = markupStream.getTag();
				// However if the markup stream is a merged markup stream
				// (inheritance), than we need the class of the markup file
				// which contained the tag.
				if (tag.getMarkupClass() != null)
				{
					clazz = tag.getMarkupClass();
				}

				// Create the component implementing the link
				ResourceReferenceAutolink autoLink = new ResourceReferenceAutolink(container,
						autoId, clazz, pathInfo.reference, attribute);
				if (autoLink.resourceReference != null)
				{
					// if the resource reference is null, it means that it the
					// reference was not found as a package resource
					return autoLink;
				}
			}
			// else we can't have absolute resource references, at least not at
			// this time

			// fall back on default processing
			return null;
		}
	}

	/**
	 * Autolink components delegate component resolution to their parent
	 * components. Reason: autolink tags don't have wicket:id and users wouldn't
	 * know where to add the component to.
	 * 
	 * @author Juergen Donnerstag
	 */
	public final static class AutolinkBookmarkablePageLink extends BookmarkablePageLink
	{
		private static final long serialVersionUID = 1L;

		private final String anchor;

		/**
		 * Construct
		 * 
		 * @param parent
		 * @param id
		 * @param pageClass
		 * @param parameters
		 * @param anchor
		 * @see BookmarkablePageLink#BookmarkablePageLink(String, Class,
		 *      PageParameters)
		 */
		public AutolinkBookmarkablePageLink(MarkupContainer parent, final String id,
				final Class<? extends Page> pageClass, final PageParameters parameters,
				final String anchor)
		{
			super(parent, id, pageClass, parameters);
			this.anchor = anchor;
			setAutoEnable(true);
		}

		/**
		 * @see wicket.MarkupContainer#isTransparentResolver()
		 */
		@Override
		public boolean isTransparentResolver()
		{
			return true;
		}

		/**
		 * 
		 * @see wicket.markup.html.link.BookmarkablePageLink#getURL()
		 */
		@Override
		protected CharSequence getURL()
		{
			CharSequence url = super.getURL();
			if (anchor != null)
			{
				url = url + anchor;
			}

			return url;
		}
	}

	/**
	 * Interface to delegate the actual resolving of auto components to.
	 */
	public static interface IAutolinkResolverDelegate
	{
		/**
		 * Returns a new auto component based on the pathInfo object. The auto
		 * component must have the autoId assigned as it's id. Should return
		 * null in case the component could not be created as expected and the
		 * default resolving should take place.
		 * 
		 * @param container
		 *            the parent container
		 * @param autoId
		 *            the automatically generated id for the auto component
		 * @param pathInfo
		 *            the path info object that contains information about the
		 *            link reference
		 * @return a new auto component or null in case this method couldn't
		 *         resolve to a proper auto component
		 */
		Component newAutoComponent(final MarkupContainer container, final String autoId,
				final PathInfo pathInfo);
	}

	/**
	 * Encapsulates different aspects of a path. For instance, the path
	 * <code>wicket.markup.html.tree.Tree/tree.css</code> has extension
	 * <code>css</code>, is relative (absolute == true) and has no page
	 * parameters.
	 */
	public static final class PathInfo
	{
		/** whether the reference is absolute. */
		private final boolean absolute;

		/** An optional anchor like #top */
		private final String anchor;

		/** The extension if any. */
		private final String extension;

		/** The optional page parameters. */
		private final PageParameters pageParameters;

		/** The path exluding any parameters. */
		private final String path;

		/** The original reference (e.g the full value of a href attribute). */
		private final String reference;

		/**
		 * Construct.
		 * 
		 * @param reference
		 *            the original reference (e.g the full value of a href
		 *            attribute)
		 */
		public PathInfo(final String reference)
		{
			this.reference = reference;
			// If href contains URL query parameters ..
			String infoPath;
			// get the query string
			int queryStringPos = reference.indexOf("?");
			if (queryStringPos != -1)
			{
				final String queryString = reference.substring(queryStringPos + 1);
				pageParameters = new PageParameters(queryString, "&");
				infoPath = reference.substring(0, queryStringPos);
			}
			else
			{
				pageParameters = null;
				infoPath = reference;
			}

			this.absolute = (infoPath.startsWith("/") || infoPath.startsWith("\\"));

			// remove file extension, but remember it
			String extension = null;
			int pos = infoPath.lastIndexOf(".");
			if (pos != -1)
			{
				extension = infoPath.substring(pos + 1);
				infoPath = infoPath.substring(0, pos);
			}

			String anchor = null;
			if (extension != null)
			{
				pos = extension.indexOf('#');
				if (pos != -1)
				{
					anchor = extension.substring(pos);
					extension = extension.substring(0, pos);
				}
			}

			this.path = infoPath;
			this.extension = extension;
			this.anchor = anchor;
		}

		/**
		 * Gets the anchor (e.g. #top)
		 * 
		 * @return anchor
		 */
		public final String getAnchor()
		{
			return anchor;
		}

		/**
		 * Gets extension.
		 * 
		 * @return extension
		 */
		public final String getExtension()
		{
			return extension;
		}

		/**
		 * Gets pageParameters.
		 * 
		 * @return pageParameters
		 */
		public final PageParameters getPageParameters()
		{
			return pageParameters;
		}

		/**
		 * Gets path.
		 * 
		 * @return path
		 */
		public final String getPath()
		{
			return path;
		}

		/**
		 * Gets reference.
		 * 
		 * @return reference
		 */
		public final String getReference()
		{
			return reference;
		}

		/**
		 * Gets absolute.
		 * 
		 * @return absolute
		 */
		public final boolean isAbsolute()
		{
			return absolute;
		}
	}

	/**
	 * Resolves to anchor/ link components.
	 */
	private static final class AnchorResolverDelegate extends AbstractAutolinkResolverDelegate
	{
		/** the attribute to fetch. */
		private static final String attribute = "href";

		/**
		 * Set of supported extensions for creating bookmarkable page links.
		 * Anything that is not in this list will be handled as a resource
		 * reference.
		 */
		private final Set<String> supportedPageExtensions = new HashSet<String>(4);

		/**
		 * Construct.
		 */
		public AnchorResolverDelegate()
		{

			// Initialize supported list of file name extension which'll create
			// bookmarkable pages
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
		@SuppressWarnings("unchecked")
		public Component newAutoComponent(final MarkupContainer container, final String autoId,
				PathInfo pathInfo)
		{
			if ((pathInfo.extension != null)
					&& supportedPageExtensions.contains(pathInfo.extension))
			{
				// Obviously a href like href="myPkg.MyLabel.html" will do as
				// well. Wicket will not throw an exception. It accepts it.
				String infoPath = Strings.replaceAll(pathInfo.path, "/", ".").toString();

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
					return new AutolinkBookmarkablePageLink(container, autoId, clazz,
							pathInfo.pageParameters, pathInfo.anchor);
				}
				catch (WicketRuntimeException ex)
				{
					log.warn("Did not find corresponding java class: " + className);
					// fall through
				}
			}
			else
			{
				// not a registered type for bookmarkable pages; create a link
				// to a resource instead
				return newResourceReferenceAutoComponent(container, autoId, pathInfo, attribute);
			}

			// fallthrough
			return null;
		}
	}

	/**
	 * Autolink components delegate component resolution to their parent
	 * components. Reason: autolink tags don't have wicket:id and users wouldn't
	 * know where to add the component to.
	 * 
	 * @author Juergen Donnerstag
	 */
	private final static class AutolinkExternalLink extends ExternalLink
	{
		private static final long serialVersionUID = 1L;

		/**
		 * Construct
		 * 
		 * @param parent
		 * @param id
		 * @param href
		 */
		public AutolinkExternalLink(MarkupContainer parent, final String id, final String href)
		{
			super(parent, id, href);
		}

		/**
		 * @see wicket.MarkupContainer#isTransparentResolver()
		 */
		@Override
		public boolean isTransparentResolver()
		{
			return true;
		}
	}

	/**
	 * Resolver that returns the proper attribute value from a component tag
	 * reflecting a URL reference such as src or href.
	 */
	private static interface ITagReferenceResolver
	{
		/**
		 * Gets the reference attribute value of the tag depending on the type
		 * of the tag. For instance, anchors use the <code>href</code>
		 * attribute but script and image references use the <code>src</code>
		 * attribute.
		 * 
		 * @param tag
		 *            The component tag. Not for modifcation.
		 * @return the tag value that constitutes the reference
		 */
		String getReference(final ComponentTag tag);
	}

	/**
	 * Autolink component that points to a {@link ResourceReference}. Autolink
	 * component delegate component resolution to their parent components.
	 * Reason: autolink tags don't have wicket:id and users wouldn't know where
	 * to add the component to.
	 * 
	 * @param <T>
	 *            The type
	 */
	private final static class ResourceReferenceAutolink<T> extends WebMarkupContainer<T>
	{
		private static final long serialVersionUID = 1L;

		private final String attribute;

		/** Resource reference */
		private final ResourceReference resourceReference;

		/**
		 * Construct.
		 * 
		 * @param parent
		 *            The parent component
		 * @param id
		 *            The component id
		 * @param clazz
		 *            The relative class of the resource
		 * @param ref
		 *            The url to the resource
		 * @param attribute
		 *            The attribute to replace
		 */
		public ResourceReferenceAutolink(MarkupContainer parent, final String id,
				final Class clazz, final String ref, final String attribute)
		{
			super(parent, id);

			this.attribute = attribute;
			// Check whether it is a valid resource reference
			if (PackageResource.exists(clazz, ref, getLocale(), getStyle()))
			{
				// Create the component implementing the link
				resourceReference = new ResourceReference(clazz, ref, getLocale(), getStyle());
			}
			else
			{
				// The resource does not exist. Set to null and ignore when
				// rendering.
				resourceReference = null;
			}
		}

		/**
		 * @see wicket.MarkupContainer#isTransparentResolver()
		 */
		@Override
		public boolean isTransparentResolver()
		{
			return true;
		}

		/**
		 * Handles this link's tag.
		 * 
		 * @param tag
		 *            the component tag
		 * @see wicket.Component#onComponentTag(ComponentTag)
		 */
		@Override
		protected final void onComponentTag(final ComponentTag tag)
		{
			// Default handling for tag
			super.onComponentTag(tag);

			// only set the href attribute when the resource exists
			if (resourceReference != null)
			{
				// Set href to link to this link's linkClicked method
				CharSequence url = getRequestCycle().urlFor(resourceReference);

				// generate the href attribute
				tag.put(attribute, Strings.replaceAll(url, "&", "&amp;"));
			}
		}
	}

	/**
	 * Resolves to {@link ResourceReference} link components. Typcically used
	 * for header contributions like javascript and css files.
	 */
	private static final class ResourceReferenceResolverDelegate
			extends
				AbstractAutolinkResolverDelegate
	{
		private final String attribute;

		/**
		 * Construct.
		 * 
		 * @param attribute
		 */
		public ResourceReferenceResolverDelegate(final String attribute)
		{
			this.attribute = attribute;
		}

		/**
		 * @see wicket.markup.resolver.AutoLinkResolver.IAutolinkResolverDelegate#newAutoComponent(wicket.MarkupContainer,
		 *      java.lang.String,
		 *      wicket.markup.resolver.AutoLinkResolver.PathInfo)
		 */
		public Component newAutoComponent(final MarkupContainer container, final String autoId,
				final PathInfo pathInfo)
		{
			return newResourceReferenceAutoComponent(container, autoId, pathInfo, attribute);
		}
	}

	/**
	 * Resolver object that returns the proper attribute value from component
	 * tags.
	 */
	private static final class TagReferenceResolver implements ITagReferenceResolver
	{
		/** the attribute to fetch. */
		private final String attribute;

		/**
		 * Construct.
		 * 
		 * @param attribute
		 *            the attribute to fetch
		 */
		public TagReferenceResolver(final String attribute)
		{
			this.attribute = attribute;
		}

		/**
		 * Gets the reference attribute value of the tag depending on the type
		 * of the tag. For instance, anchors use the <code>href</code>
		 * attribute but script and image references use the <code>src</code>
		 * attribute.
		 * 
		 * @param tag
		 *            The component tag. Not for modifcation.
		 * @return the tag value that constitutes the reference
		 */
		public String getReference(final ComponentTag tag)
		{
			return tag.getAttributes().getString(attribute);
		}
	}

	/**
	 * If no specific resolver is found, always use the href attribute for
	 * references.
	 */
	private static final TagReferenceResolver DEFAULT_ATTRIBUTE_RESOLVER = new TagReferenceResolver(
			"href");

	/** Logging */
	private static final Log log = LogFactory.getLog(AutoLinkResolver.class);

	private static final long serialVersionUID = 1L;

	/**
	 * Autolink resolver delegates for constructing new autolinks reference
	 * keyed on tag name (such as &lt;script&gt; or &lt;a&gt;.
	 */
	private final Map<String, IAutolinkResolverDelegate> tagNameToAutolinkResolverDelegates = new HashMap<String, IAutolinkResolverDelegate>();

	/**
	 * Resolver objects that know what attribute to read for getting the
	 * reference keyed on tag name (such as &lt;script&gt; or &lt;a&gt;.
	 */
	private final Map<String, TagReferenceResolver> tagNameToTagReferenceResolvers = new HashMap<String, TagReferenceResolver>();

	/**
	 * Construct.
	 */
	public AutoLinkResolver()
	{

		// register tag reference resolvers
		TagReferenceResolver hrefTagReferenceResolver = new TagReferenceResolver("href");
		TagReferenceResolver srcTagReferenceResolver = new TagReferenceResolver("src");
		tagNameToTagReferenceResolvers.put("a", hrefTagReferenceResolver);
		tagNameToTagReferenceResolvers.put("link", hrefTagReferenceResolver);
		tagNameToTagReferenceResolvers.put("script", srcTagReferenceResolver);
		tagNameToTagReferenceResolvers.put("img", srcTagReferenceResolver);

		// register autolink resolver delegates
		tagNameToAutolinkResolverDelegates.put("a", new AnchorResolverDelegate());
		tagNameToAutolinkResolverDelegates.put("link",
				new ResourceReferenceResolverDelegate("href"));
		tagNameToAutolinkResolverDelegates.put("script", new ResourceReferenceResolverDelegate(
				"src"));
		tagNameToAutolinkResolverDelegates.put("img", new ResourceReferenceResolverDelegate("src"));
	}

	/**
	 * Register (add or replace) a new resolver with the tagName and
	 * attributeName. The resolver will be invoked each time an appropriate tag
	 * and attribute is found.
	 * 
	 * @param tagName
	 *            The tag name
	 * @param attributeName
	 *            The attribute name
	 * @param resolver
	 *            Implements what to do based on the tag and the attribute
	 */
	public final void addTagReferenceResolver(final String tagName, final String attributeName,
			final IAutolinkResolverDelegate resolver)
	{
		TagReferenceResolver tagReferenceResolver = new TagReferenceResolver(attributeName);
		tagNameToTagReferenceResolvers.put(tagName, tagReferenceResolver);

		tagNameToAutolinkResolverDelegates.put(tagName, resolver);
	}

	/**
	 * Get the resolver registered for 'tagName'
	 * 
	 * @param tagName
	 *            The tag's name
	 * @return The resolver found. Null, if none registered
	 */
	public final IAutolinkResolverDelegate getAutolinkResolverDelegate(final String tagName)
	{
		return tagNameToAutolinkResolverDelegates.get(tagName);
	}

	/**
	 * Automatically creates a BookmarkablePageLink component.
	 * 
	 * @see wicket.markup.resolver.IComponentResolver#resolve(MarkupContainer,
	 *      MarkupStream, ComponentTag)
	 * 
	 * @param markupStream
	 *            The current markupStream
	 * @param tag
	 *            The current component tag while parsing the markup
	 * @param container
	 *            The container parsing its markup
	 * @return true, if componentId was handle by the resolver. False, otherwise
	 */
	public final boolean resolve(final MarkupContainer container, final MarkupStream markupStream,
			final ComponentTag tag)
	{
		// Must be marked as autolink tag
		if (tag.isAutolinkEnabled())
		{
			// Try to find the Page matching the href
			// Note: to not use tag.getId() because it will be modified while
			// resolving the link and hence the 2nd render will fail.
			final Component link = resolveAutomaticLink(container,
					WicketLinkTagHandler.AUTOLINK_ID, tag);

			// Render the link to the container
			link.render(markupStream);
			if (log.isDebugEnabled())
			{
				log.debug("Added autolink " + link);
			}

			// Tell the container, we resolved the id
			return true;
		}

		// We were not able to resolve the id
		return false;
	}

	/**
	 * Resolves the given tag's page class and page parameters by parsing the
	 * tag component name and then searching for a page class at the absolute or
	 * relative URL specified by the href attribute of the tag.
	 * <p>
	 * None html references are treated similar.
	 * 
	 * @param container
	 *            The container where the link is
	 * @param id
	 *            the name of the component
	 * @param tag
	 *            the component tag
	 * @return A BookmarkablePageLink to handle the href
	 */
	private final Component resolveAutomaticLink(final MarkupContainer container, final String id,
			final ComponentTag tag)
	{
		final Page page = container.getPage();

		// Make the id (page-)unique
		final String autoId = id + page.getAutoIndex();

		// get the tag name, which is something like 'a' or 'script'
		final String tagName = tag.getName();

		// get the reference resolver
		ITagReferenceResolver referenceResolver = tagNameToTagReferenceResolvers.get(tagName);
		if (referenceResolver == null)
		{
			// fallback on default
			referenceResolver = DEFAULT_ATTRIBUTE_RESOLVER;
		}

		// get the reference, which is typically the value of e.g. a href or src
		// attribute
		String reference = referenceResolver.getReference(tag);

		// create the path info object
		PathInfo pathInfo = new PathInfo(reference);
		// now get the resolver delegate
		IAutolinkResolverDelegate autolinkResolverDelegate = tagNameToAutolinkResolverDelegates
				.get(tagName);
		Component autoComponent = null;
		if (autolinkResolverDelegate != null)
		{
			autoComponent = autolinkResolverDelegate.newAutoComponent(container, autoId, pathInfo);
		}

		if (autoComponent == null)
		{
			// resolving didn't have the desired result or there was no delegate
			// found; fallback on the default resolving which is a simple
			// component that leaves the tag unchanged
			autoComponent = new AutolinkExternalLink(container, autoId, pathInfo.reference);
		}

		return autoComponent;
	}
}