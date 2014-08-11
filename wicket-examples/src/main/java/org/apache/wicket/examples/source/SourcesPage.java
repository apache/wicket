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
package org.apache.wicket.examples.source;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxCallListener;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.authorization.UnauthorizedInstantiationException;
import org.apache.wicket.core.util.lang.WicketObjects;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.http.handler.ErrorCodeRequestHandler;
import org.apache.wicket.request.mapper.parameter.INamedParameters;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.io.IOUtils;
import org.apache.wicket.util.lang.PackageName;
import org.apache.wicket.util.string.AppendingStringBuffer;
import org.apache.wicket.util.string.StringValue;
import org.apache.wicket.util.string.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.uwyn.jhighlight.renderer.Renderer;
import com.uwyn.jhighlight.renderer.XhtmlRendererFactory;

/**
 * Displays the resources in a packages directory in a browsable format.
 * 
 * @author Martijn Dashorst
 */
public class SourcesPage extends WebPage
{
	private static final Logger log = LoggerFactory.getLogger(SourcesPage.class);

	/**
	 * Model for retrieving the source code from the classpath of a packaged resource.
	 */
	private class SourceModel extends AbstractReadOnlyModel<String>
	{
		/**
		 * Returns the contents of the file loaded from the classpath.
		 * 
		 * @return the contents of the file identified by name
		 */
		@Override
		public String getObject()
		{
			// name contains the name of the selected file
			StringValue sourceParam = getPageParameters().get(SOURCE);
			if (Strings.isEmpty(name) && sourceParam.isEmpty())
			{
				return "";
			}

			String source = null;
			InputStream resourceAsStream = null;
			try
			{
				source = (name != null) ? name : sourceParam.toString();
				resourceAsStream = getPageTargetClass().getResourceAsStream(source);
				if (resourceAsStream == null)
				{
					return "Unable to read the source for " + source;
				}

				int lastDot = source.lastIndexOf('.');
				if (lastDot != -1)
				{
					String type = source.substring(lastDot + 1);
					Renderer renderer = XhtmlRendererFactory.getRenderer(type);
					if (renderer != null)
					{
						ByteArrayOutputStream output = new ByteArrayOutputStream();
						renderer.highlight(source, resourceAsStream, output, "UTF-8", true);
						return output.toString("UTF-8");
					}
				}

				CharSequence escaped = Strings.escapeMarkup(IOUtils.toString(resourceAsStream),
					false, true);
				return Strings.replaceAll(escaped, "\n", "<br />").toString();
			}
			catch (IOException e)
			{
				log.error(
					"Unable to read resource stream for: " + source + "; Page=" + page.toString(),
					e);
				return "";
			}
			finally
			{
				IOUtils.closeQuietly(resourceAsStream);
			}
		}
	}

	/**
	 * Model for retrieving the contents of a package directory from the class path.
	 */
	public class PackagedResourcesModel extends LoadableDetachableModel<List<String>>
	{
		/**
		 * Returns the list of resources found in the package of the page.
		 * 
		 * @return the list of resources found in the package of the page.
		 */
		@Override
		protected List<String> load()
		{
			return get(getPageTargetClass());
		}

		private void addResources(final AppendingStringBuffer relativePath, final File dir,
			List<String> resources)
		{
			File[] files = dir.listFiles();
			for (File file : files)
			{
				if (file.isDirectory())
				{
					addResources(new AppendingStringBuffer(relativePath).append(file.getName())
						.append('/'), file, resources);
				}
				else
				{
					String name = file.getName();
					if (!name.endsWith("class"))
					{
						resources.add(relativePath + name);
					}
				}
			}
		}

		private List<String> get(Class<?> scope)
		{
			List<String> resources = new ArrayList<String>();

			String packageRef = Strings.replaceAll(PackageName.forClass(scope).getName(), ".", "/")
				.toString();
			ClassLoader loader = scope.getClassLoader();
			try
			{
				// loop through the resources of the package
				Enumeration<URL> packageResources = loader.getResources(packageRef);
				while (packageResources.hasMoreElements())
				{
					URL resource = packageResources.nextElement();
					URLConnection connection = resource.openConnection();
					if (connection instanceof JarURLConnection)
					{
						JarFile jf = ((JarURLConnection)connection).getJarFile();
						scanJarFile(packageRef, jf, resources);
					}
					else
					{
						String absolutePath = scope.getResource("").toExternalForm();
						File basedir;
						URI uri;
						try
						{
							uri = new URI(absolutePath);
						}
						catch (URISyntaxException e)
						{
							throw new RuntimeException(e);
						}
						try
						{
							basedir = new File(uri);
						}
						catch (IllegalArgumentException e)
						{
							log.debug("Can't construct the uri as a file: " + absolutePath);
							// if this is throwen then the path is not really a
							// file. but could be a zip.
							String jarZipPart = uri.getSchemeSpecificPart();
							// lowercased for testing if jar/zip, but leave the
							// real filespec unchanged
							String lowerJarZipPart = jarZipPart.toLowerCase();
							int index = lowerJarZipPart.indexOf(".zip");
							if (index == -1)
								index = lowerJarZipPart.indexOf(".jar");
							if (index == -1)
								throw e;

							String filename = jarZipPart.substring(0, index + 4); // 4 =
							// len
							// of
							// ".jar"
							// or
							// ".zip"
							log.debug("trying the filename: " + filename + " to load as a zip/jar.");
							JarFile jarFile = new JarFile(filename, false);
							scanJarFile(packageRef, jarFile, resources);
							return resources;
						}
						if (!basedir.isDirectory())
						{
							throw new IllegalStateException(
								"unable to read resources from directory " + basedir);
						}
						addResources(new AppendingStringBuffer(), basedir, resources);
					}
				}
			}
			catch (IOException e)
			{
				throw new WicketRuntimeException(e);
			}
			Collections.sort(resources);

			return resources;
		}

		private void scanJarFile(String packageRef, JarFile jf, List<String> resources)
		{
			Enumeration<JarEntry> enumeration = jf.entries();
			while (enumeration.hasMoreElements())
			{
				JarEntry je = enumeration.nextElement();
				String name = je.getName();
				if (name.startsWith(packageRef))
				{
					name = name.substring(packageRef.length() + 1);
					if (!name.endsWith("class"))
					{
						resources.add(name);
					}
				}
			}
		}
	}

	/**
	 * Displays the resources embedded in a package in a list.
	 */
	public class FilesBrowser extends WebMarkupContainer
	{
		/**
		 * Constructor.
		 * 
		 * @param id
		 *            the component identifier
		 */
		public FilesBrowser(String id)
		{
			super(id);
			ListView<String> lv = new ListView<String>("file", new PackagedResourcesModel())
			{
				@Override
				protected void populateItem(final ListItem<String> item)
				{
					AjaxFallbackLink<String> link = new AjaxFallbackLink<String>("link",
						item.getModel())
					{
						@Override
						public void onClick(AjaxRequestTarget target)
						{
							setName(getDefaultModelObjectAsString());

							if (target != null)
							{
								target.add(codePanel);
								target.add(filename);
							}
						}

						@Override
						protected CharSequence getURL()
						{
							return urlFor(SourcesPage.class, SourcesPage.generatePageParameters(
								getPageTargetClass(), item.getModel().getObject()));
						}

						@Override
						protected void updateAjaxAttributes(AjaxRequestAttributes attributes)
						{
							super.updateAjaxAttributes(attributes);
							AjaxCallListener ajaxCallListener = new AjaxCallListener()
							{
								@Override
								public CharSequence getFailureHandler(Component component)
								{
									return "window.location=this.href;";
								}
							};
							attributes.getAjaxCallListeners().add(ajaxCallListener);
							attributes.getExtraParameters().put(PAGE_CLASS, "1");
						}
					};
					link.add(new Label("name", item.getDefaultModelObjectAsString()));
					item.add(link);
				}
			};
			add(lv);
		}
	}

	/**
	 * Container for displaying the source of the selected page, resource or other element from the
	 * package.
	 */
	public class CodePanel extends WebMarkupContainer
	{
		/**
		 * Constructor.
		 * 
		 * @param id
		 *            the component id
		 */
		public CodePanel(String id)
		{
			super(id);
			Label code = new Label("code", new SourceModel());
			code.setEscapeModelStrings(false);
			code.setOutputMarkupId(true);
			add(code);
		}
	}

	/**
	 * Parameter key for identifying the page class. UUID generated.
	 */
	public static final String PAGE_CLASS = SourcesPage.class.getSimpleName() + "_class";

	/**
	 * Parameter key for identify the name of the source file in the package.
	 */
	public static final String SOURCE = "source";

	/**
	 * The selected name of the packaged resource to display.
	 */
	private String name;

	private transient Class<? extends Page> page;

	/**
	 * The panel for setting the ajax calls.
	 */
	private final Component codePanel;

	private final Label filename;

	/**
	 * Sets the name.
	 * 
	 * @param name
	 *            the name to set.
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * Gets the name.
	 * 
	 * @return the name.
	 */
	public String getName()
	{
		return name;
	}


	/**
	 * 
	 * Construct.
	 * 
	 * @param params
	 */
	public SourcesPage(final PageParameters params)
	{
		super(params);

		filename = new Label("filename", new AbstractReadOnlyModel<String>()
		{

			@Override
			public String getObject()
			{
				return name != null ? name : getPage().getRequest().getRequestParameters()
					.getParameterValue(SOURCE).toOptionalString();
			}

		});
		filename.setOutputMarkupId(true);
		add(filename);
		codePanel = new CodePanel("codepanel").setOutputMarkupId(true);
		add(codePanel);
		add(new FilesBrowser("filespanel"));
	}

	/**
	 * 
	 * @param page
	 * @return PageParameters for reconstructing the bookmarkable page.
	 */
	public static PageParameters generatePageParameters(Page page)
	{
		return generatePageParameters(page.getClass(), null);
	}

	/**
	 * 
	 * @param clazz
	 * @param fileName
	 * @return PageParameters for reconstructing the bookmarkable page.
	 */
	public static PageParameters generatePageParameters(Class<? extends Page> clazz, String fileName)
	{
		PageParameters p = new PageParameters();
		p.set(PAGE_CLASS, clazz.getName(), INamedParameters.Type.MANUAL);
		if (fileName != null)
		{
			p.set(SOURCE, fileName, INamedParameters.Type.MANUAL);
		}
		return p;
	}

	private Class<? extends Page> getPageTargetClass()
	{
		if (page == null)
		{
			String pageParam = getPageParameters().get(PAGE_CLASS).toOptionalString();
			if (pageParam == null)
			{
				if (log.isErrorEnabled())
				{
					log.error("key: " + PAGE_CLASS + " is null.");
				}
				getRequestCycle().replaceAllRequestHandlers(
					new ErrorCodeRequestHandler(404,
						"Could not find sources for the page you requested"));
			}
			else if (!pageParam.startsWith("org.apache.wicket.examples"))
			{
				if (log.isErrorEnabled())
				{
					log.error("user is trying to access class: " + pageParam
						+ " which is not in the scope of org.apache.wicket.examples");
				}
				throw new UnauthorizedInstantiationException(getClass());
			}
			page = WicketObjects.resolveClass(pageParam);

			if (page == null)
			{
				getRequestCycle().replaceAllRequestHandlers(
					new ErrorCodeRequestHandler(404,
						"Could not find sources for the page you requested"));
			}
		}
		return page;
	}
}
