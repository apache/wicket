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
package org.apache.wicket.markup.resolver;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Page;
import org.apache.wicket.application.IClassResolver;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.IMarkupFragment;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.protocol.http.RequestUtils;
import org.apache.wicket.request.handler.resource.ResourceReferenceRequestHandler;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.PackageResource;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.util.lang.Packages;
import org.apache.wicket.util.string.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The AutoLinkResolver is responsible to handle automatic link resolution. Tags are marked
 * "autolink" by the MarkupParser for all tags with href attribute, such as anchor and link tags
 * with no explicit wicket id. E.g. &lt;a href="Home.html"&gt;
 * <p>
 * If href points to a *.html file, a BookmarkablePageLink<?> will automatically be created, except
 * for absolute paths, where an ExternalLink is created.
 * <p>
 * If href points to a *.html file, it resolves the given URL by searching for a page class, either
 * relative or absolute, specified by the href attribute of the tag. If relative the href URL must
 * be relative to the package containing the associated page. An exception is thrown if no Page
 * class was found.
 * <p>
 * If href is no *.html file a static reference to the resource is created.
 * 
 * @see org.apache.wicket.markup.parser.filter.WicketLinkTagHandler
 * 
 * @author Juergen Donnerstag
 * @author Eelco Hillenius
 */
public final class AutoLinkResolver implements IComponentResolver
{
	/**
	 * Abstract implementation that has a helper method for creating a resource reference.
	 */
	public static abstract class AbstractAutolinkResolverDelegate
		implements
			IAutolinkResolverDelegate
	{
		/**
		 * Creates a new auto component that references a package resource.
		 *
		 * @param autoId
		 *            the automatically generated id for the auto component
		 * @param pathInfo
		 *            the path info object that contains information about the link reference
		 * @param attribute
		 *            the attribute to replace the value of
		 * @return a new auto component or null if the path was absolute
		 */
		protected final Component newPackageResourceReferenceAutoComponent(
			final String autoId, final PathInfo pathInfo,
			final String attribute)
		{
			final MarkupContainer container = pathInfo.getContainer();

			if (!pathInfo.absolute && (pathInfo.path != null) && (pathInfo.path.length() > 0))
			{
				// Href is relative. Create a resource reference pointing at this file

				// <wicket:head> components are handled differently. We can
				// not use the container, because it is the container the
				// header has been added to (e.g. the Page). What we need
				// however, is the component (e.g. a Panel) which
				// contributed it.
				MarkupStream markupStream = pathInfo.getMarkupStream();
				Class<? extends Component> clazz = markupStream.getContainerClass();

				// However if the markup stream is a merged markup stream (inheritance), than we
				// need the class of the markup file which contained the tag.
				if ((markupStream.get() instanceof ComponentTag) &&
					(markupStream.getTag().getMarkupClass() != null))
				{
					clazz = markupStream.getTag().getMarkupClass();
				}

				// Create the component implementing the link
				ResourceReferenceAutolink autoLink = new ResourceReferenceAutolink(autoId, clazz,
					pathInfo.reference, attribute, container);
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
	 * Autolink components delegate component resolution to their parent components. Reason:
	 * autolink tags don't have wicket:id and users wouldn't know where to add the component to.
	 * 
	 * @author Juergen Donnerstag
	 * @param <T>
	 *            type of model object
	 */
	public final static class AutolinkBookmarkablePageLink<T> extends BookmarkablePageLink<T>
		implements
			IComponentResolver
	{
		private static final long serialVersionUID = 1L;

		private final String anchor;

		/**
		 * When using <wicket:link> to let Wicket lookup for pages and create the related links,
		 * it's not possible to change the "setAutoEnable" property, which defaults to true. This
		 * affects the prototype because, sometimes designers _want_ links to be enabled.
		 */
		public static boolean autoEnable = true;

		/**
		 * Construct
		 * 
		 * @param <C>
		 * 
		 * @see BookmarkablePageLink#BookmarkablePageLink(String, Class, PageParameters)
		 * 
		 * @param id
		 * @param pageClass
		 * @param parameters
		 * @param anchor
		 */
		public <C extends Page> AutolinkBookmarkablePageLink(final String id,
			final Class<C> pageClass, final PageParameters parameters, final String anchor)
		{
			super(id, pageClass, parameters);
			this.anchor = anchor;
			setAutoEnable(autoEnable);
		}

		/**
		 * 
		 * @see org.apache.wicket.markup.html.link.BookmarkablePageLink#getURL()
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

		/**
		 * @see org.apache.wicket.markup.resolver.IComponentResolver#resolve(org.apache.wicket.MarkupContainer,
		 *      org.apache.wicket.markup.MarkupStream, org.apache.wicket.markup.ComponentTag)
		 */
		@Override
		public Component resolve(final MarkupContainer container, final MarkupStream markupStream,
			ComponentTag tag)
		{
			return getParent().get(tag.getId());
		}
	}

	/**
	 * Interface to delegate the actual resolving of auto components to.
	 */
	public static interface IAutolinkResolverDelegate
	{
		/**
		 * Returns a new auto component based on the pathInfo object. The auto component must have
		 * the autoId assigned as it's id. Should return null in case the component could not be
		 * created as expected and the default resolving should take place.
		 * 
		 * @param autoId
		 *            the automatically generated id for the auto component
		 * @param pathInfo
		 *            the path info object that contains information about the link reference
		 * @return a new auto component or null in case this method couldn't resolve to a proper
		 *         auto component
		 */
		Component newAutoComponent(final String autoId, final PathInfo pathInfo);
	}

	/**
	 * Encapsulates different aspects of a path. For instance, the path
	 * <code>org.apache.wicket.markup.html.tree.Tree/tree.css</code> has extension <code>css</code>,
	 * is relative (absolute == true) and has no page parameters.
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

		/** The path excluding any parameters. */
		private final String path;

		/** The original reference (e.g the full value of a href attribute). */
		private final String reference;

		/** The container for this path */
		private final MarkupContainer container;

		/** Parent markup stream */
		private final MarkupStream markupStream;

		/**
		 * Construct.
		 * 
		 * @param reference
		 *            the original reference (e.g the full value of a href attribute)
		 */
		public PathInfo(final String reference, MarkupContainer container, MarkupStream markupStream)
		{
			this.reference = reference;
			this.container = container;
			this.markupStream = markupStream;
			// If href contains URL query parameters ..
			String infoPath;
			// get the query string
			int queryStringPos = reference.indexOf("?");
			if (queryStringPos != -1)
			{
				final String queryString = reference.substring(queryStringPos + 1);
				pageParameters = new PageParameters();
				RequestUtils.decodeParameters(queryString, pageParameters);
				infoPath = reference.substring(0, queryStringPos);
			}
			else
			{
				pageParameters = null;
				infoPath = reference;
			}

			absolute = (infoPath.startsWith("/") || infoPath.startsWith("\\"));

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

			// Anchors without path, e.g. "#link"
			if (anchor == null)
			{
				pos = infoPath.indexOf("#");
				if (pos != -1)
				{
					anchor = infoPath.substring(pos);
					infoPath = infoPath.substring(0, pos);
				}
			}

			path = infoPath;
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

		/**
		 * Gets container.
		 *
		 * @return container
		 */
		public MarkupContainer getContainer()
		{
			return container;
		}

		/**
		 * Gets markup stream
		 *
		 * @return markup stream
		 */
		public MarkupStream getMarkupStream()
		{
			return markupStream;
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
		 * Set of supported extensions for creating bookmarkable page links. Anything that is not in
		 * this list will be handled as a resource reference.
		 */
		private final Set<String> supportedPageExtensions = new HashSet<>(4);

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
		 * @see org.apache.wicket.markup.resolver.AutoLinkResolver.IAutolinkResolverDelegate#newAutoComponent(java.lang.String,
		 * org.apache.wicket.markup.resolver.AutoLinkResolver.PathInfo)
		 */
		@Override
		@SuppressWarnings("unchecked")
		public Component newAutoComponent(final String autoId, PathInfo pathInfo)
		{
			final MarkupContainer container = pathInfo.getContainer();

			if ((pathInfo.extension != null) &&
				supportedPageExtensions.contains(pathInfo.extension))
			{
				// Obviously a href like href="myPkg.MyLabel.html" will do as
				// well. Wicket will not throw an exception. It accepts it.

				Page page = container.getPage();
				final IClassResolver defaultClassResolver = page.getApplication()
					.getApplicationSettings()
					.getClassResolver();
				String className = Packages.absolutePath(page.getClass(), pathInfo.path);
				className = Strings.replaceAll(className, "/", ".").toString();
				if (className.startsWith("."))
				{
					className = className.substring(1);
				}

				try
				{
					final Class<? extends Page> clazz = (Class<? extends Page>)defaultClassResolver.resolveClass(className);
					return new AutolinkBookmarkablePageLink<Void>(autoId, clazz,
						pathInfo.pageParameters, pathInfo.anchor);
				}
				catch (ClassNotFoundException ex)
				{
					log.warn("Did not find corresponding java class: " + className);
					// fall through
				}

				// Make sure base markup pages (inheritance) are handled correct
				MarkupContainer parentWithContainer = container;
				if (container.getParent() != null)
				{
					parentWithContainer = container.findParentWithAssociatedMarkup();
				}
				if ((parentWithContainer instanceof Page) && !pathInfo.path.startsWith("/") &&
					new MarkupStream(page.getMarkup()).isMergedMarkup())
				{
					IMarkupFragment containerMarkup = container.getMarkup();
					MarkupStream containerMarkupStream = new MarkupStream(containerMarkup);
					if (containerMarkupStream.atTag())
					{
						ComponentTag tag = containerMarkupStream.getTag();
						Class<? extends Page> clazz = (Class<? extends Page>)tag.getMarkupClass();
						if (clazz != null)
						{
							// Href is relative. Resolve the url given relative to
							// the current page
							className = Packages.absolutePath(clazz, pathInfo.path);
							className = Strings.replaceAll(className, "/", ".").toString();
							if (className.startsWith("."))
							{
								className = className.substring(1);
							}

							try
							{
								clazz = (Class<? extends Page>)defaultClassResolver.resolveClass(className);
								return new AutolinkBookmarkablePageLink<Void>(autoId, clazz,
									pathInfo.getPageParameters(), pathInfo.anchor);
							}
							catch (ClassNotFoundException ex)
							{
								log.warn("Did not find corresponding java class: " + className);
								// fall through
							}
						}
					}
				}
			}
			else
			{
				// not a registered type for bookmarkable pages; create a link
				// to a resource instead
				return newPackageResourceReferenceAutoComponent(autoId, pathInfo, attribute);
			}

			// fallthrough
			return null;
		}
	}

	/**
	 * Autolink components delegate component resolution to their parent components. Reason:
	 * autolink tags don't have wicket:id and users wouldn't know where to add the component to.
	 * 
	 * @author Juergen Donnerstag
	 */
	private final static class AutolinkExternalLink extends ExternalLink
		implements
			IComponentResolver
	{
		private static final long serialVersionUID = 1L;

		/**
		 * Construct
		 * 
		 * @param id
		 * @param href
		 */
		public AutolinkExternalLink(final String id, final String href)
		{
			super(id, href);
		}

		/**
		 * @see org.apache.wicket.markup.resolver.IComponentResolver#resolve(org.apache.wicket.MarkupContainer,
		 *      org.apache.wicket.markup.MarkupStream, org.apache.wicket.markup.ComponentTag)
		 */
		@Override
		public Component resolve(MarkupContainer container, MarkupStream markupStream,
			ComponentTag tag)
		{
			return getParent().get(tag.getId());
		}
	}

	/**
	 * Resolver that returns the proper attribute value from a component tag reflecting a URL
	 * reference such as src or href.
	 */
	private static interface ITagReferenceResolver
	{
		/**
		 * Gets the reference attribute value of the tag depending on the type of the tag. For
		 * instance, anchors use the <code>href</code> attribute but script and image references use
		 * the <code>src</code> attribute.
		 * 
		 * @param tag
		 *            The component tag. Not for modification.
		 * @return the tag value that constitutes the reference
		 */
		String getReference(final ComponentTag tag);
	}

	/**
	 * Autolink component that points to a {@link ResourceReference}. Autolink component delegate
	 * component resolution to their parent components. Reason: autolink tags don't have wicket:id
	 * and users wouldn't know where to add the component to.
	 */
	private final static class ResourceReferenceAutolink extends WebMarkupContainer
		implements
			IComponentResolver
	{
		private static final long serialVersionUID = 1L;

		private final String attribute;

		/** Resource reference */
		private final ResourceReference resourceReference;

		private final MarkupContainer parent;

		/**
		 * @param id
		 * @param clazz
		 * @param href
		 * @param attribute
		 * @param parent
		 */
		public ResourceReferenceAutolink(final String id, final Class<?> clazz, final String href,
			final String attribute, MarkupContainer parent)
		{
			super(id);

			this.parent = parent;
			this.attribute = attribute;
			// Check whether it is a valid resource reference
			if (PackageResource.exists(clazz, href, getLocale(), getStyle(), getVariation()))
			{
				// Create the component implementing the link
				resourceReference = new PackageResourceReference(clazz, href, getLocale(),
					getStyle(), getVariation());
				}
				else
				{
					// The resource does not exist. Set to null and ignore when
					// rendering.
					resourceReference = null;
				}
		}

		/**
		 * @see org.apache.wicket.Component#getVariation()
		 */
		@Override
		public String getVariation()
		{
			if (parent != null)
			{
				return parent.getVariation();
			}

			return super.getVariation();
		}


		/**
		 * Handles this link's tag.
		 * 
		 * @param tag
		 *            the component tag
		 * @see org.apache.wicket.Component#onComponentTag(ComponentTag)
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

				ResourceReferenceRequestHandler handler = new ResourceReferenceRequestHandler(
					resourceReference);
				CharSequence url = getRequestCycle().urlFor(handler);

				// generate the href attribute
				tag.put(attribute, url);
			}
		}

		/**
		 * @see org.apache.wicket.markup.resolver.IComponentResolver#resolve(org.apache.wicket.MarkupContainer,
		 *      org.apache.wicket.markup.MarkupStream, org.apache.wicket.markup.ComponentTag)
		 */
		@Override
		public Component resolve(MarkupContainer container, MarkupStream markupStream,
			ComponentTag tag)
		{
			return getParent().get(tag.getId());
		}
	}

	/**
	 * Resolves to {@link ResourceReference} link components. Typically used for header
	 * contributions like javascript and css files.
	 */
	private static final class ResourceReferenceResolverDelegate extends
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
		 * @see org.apache.wicket.markup.resolver.AutoLinkResolver.IAutolinkResolverDelegate#newAutoComponent(java.lang.String,
		 * org.apache.wicket.markup.resolver.AutoLinkResolver.PathInfo)
		 */
		@Override
		public Component newAutoComponent(final String autoId, final PathInfo pathInfo)
		{
			return newPackageResourceReferenceAutoComponent(autoId, pathInfo, attribute);
		}
	}

	/**
	 * Resolver object that returns the proper attribute value from component tags.
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
		 * Gets the reference attribute value of the tag depending on the type of the tag. For
		 * instance, anchors use the <code>href</code> attribute but script and image references use
		 * the <code>src</code> attribute.
		 * 
		 * @param tag
		 *            The component tag. Not for modification.
		 * @return the tag value that constitutes the reference
		 */
		@Override
		public String getReference(final ComponentTag tag)
		{
			return tag.getAttributes().getString(attribute);
		}
	}

	/**
	 * If no specific resolver is found, always use the href attribute for references.
	 */
	private static final TagReferenceResolver DEFAULT_ATTRIBUTE_RESOLVER = new TagReferenceResolver(
		"href");

	/** Logging */
	private static final Logger log = LoggerFactory.getLogger(AutoLinkResolver.class);

	private static final long serialVersionUID = 1L;

	/**
	 * Autolink resolver delegates for constructing new autolinks reference keyed on tag name (such
	 * as &lt;script&gt; or &lt;a&gt;.
	 */
	private final Map<String, IAutolinkResolverDelegate> tagNameToAutolinkResolverDelegates = new HashMap<>();

	/**
	 * Resolver objects that know what attribute to read for getting the reference keyed on tag name
	 * (such as &lt;script&gt; or &lt;a&gt;.
	 */
	private final Map<String, ITagReferenceResolver> tagNameToTagReferenceResolvers = new HashMap<>();

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
		tagNameToTagReferenceResolvers.put("input", srcTagReferenceResolver);
		tagNameToTagReferenceResolvers.put("embed", srcTagReferenceResolver);

		// register autolink resolver delegates
		tagNameToAutolinkResolverDelegates.put("a", new AnchorResolverDelegate());
		tagNameToAutolinkResolverDelegates.put("link",
			new ResourceReferenceResolverDelegate("href"));
		ResourceReferenceResolverDelegate srcResRefResolver = new ResourceReferenceResolverDelegate(
			"src");
		tagNameToAutolinkResolverDelegates.put("script", srcResRefResolver);
		tagNameToAutolinkResolverDelegates.put("img", srcResRefResolver);
		tagNameToAutolinkResolverDelegates.put("input", srcResRefResolver);
		tagNameToAutolinkResolverDelegates.put("embed", srcResRefResolver);
	}

	/**
	 * Register (add or replace) a new resolver with the tagName and attributeName. The resolver
	 * will be invoked each time an appropriate tag and attribute is found.
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
	 * @see org.apache.wicket.markup.resolver.IComponentResolver#resolve(org.apache.wicket.MarkupContainer,
	 *      org.apache.wicket.markup.MarkupStream, org.apache.wicket.markup.ComponentTag)
	 */
	@Override
	public final Component resolve(final MarkupContainer container,
		final MarkupStream markupStream, final ComponentTag tag)
	{
		// Must be marked as autolink tag
		if (tag.isAutolinkEnabled())
		{
			// get the reference resolver
			ITagReferenceResolver referenceResolver = tagNameToTagReferenceResolvers.get(tag.getName());
			if (referenceResolver == null)
			{
				// fallback on default
				referenceResolver = DEFAULT_ATTRIBUTE_RESOLVER;
			}

			// get the reference, which is typically the value of e.g. a href or src
			// attribute
			String reference = referenceResolver.getReference(tag);

			// create the path info object
			PathInfo pathInfo = new PathInfo(reference, container, markupStream);

			// Try to find the Page matching the href
			// Note: to not use tag.getId() because it will be modified while
			// resolving the link and hence the 2nd render will fail.
			Component link = resolveAutomaticLink(pathInfo, tag);

			if (log.isDebugEnabled())
			{
				log.debug("Added autolink " + link);
			}

			// Tell the container, we resolved the id
			return link;
		}

		// We were not able to resolve the id
		return null;
	}

	/**
	 * Resolves the given tag's page class and page parameters by parsing the tag component name and
	 * then searching for a page class at the absolute or relative URL specified by the href
	 * attribute of the tag.
	 * <p>
	 * None html references are treated similar.
	 * 
	 * @param pathInfo
	 *            The container where the link is
	 * @param id
	 *            the name of the component
	 * @param tag
	 *            the component tag
	 * @return A BookmarkablePageLink<?> to handle the href
	 */
	private Component resolveAutomaticLink(final PathInfo pathInfo, final ComponentTag tag)
	{
		final MarkupContainer container = pathInfo.getContainer();
		final String componentId = tag.getId();

		// get the tag name, which is something like 'a' or 'script'
		final String tagName = tag.getName();

		// By setting the component name, the tag becomes a Wicket component
		// tag, which must have a associated Component.
		if (tag.getId() == null)
		{
			tag.setAutoComponentTag(true);
		}

		// now get the resolver delegate
		IAutolinkResolverDelegate autolinkResolverDelegate = tagNameToAutolinkResolverDelegates.get(tagName);
		Component autoComponent = null;
		if (autolinkResolverDelegate != null)
		{
			autoComponent = autolinkResolverDelegate.newAutoComponent(componentId, pathInfo);
		}

		if (autoComponent == null)
		{
			// resolving didn't have the desired result or there was no delegate
			// found; fallback on the default resolving which is a simple
			// component that leaves the tag unchanged
			autoComponent = new AutolinkExternalLink(componentId, pathInfo.reference);
		}

		return autoComponent;
	}
}
