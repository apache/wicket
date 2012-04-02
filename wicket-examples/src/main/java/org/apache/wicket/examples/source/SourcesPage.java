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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
import org.apache.wicket.ajax.IAjaxCallDecorator;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.authorization.UnauthorizedInstantiationException;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.request.http.handler.ErrorCodeRequestHandler;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.io.IOUtils;
import org.apache.wicket.util.io.Streams;
import org.apache.wicket.util.lang.PackageName;
import org.apache.wicket.util.string.AppendingStringBuffer;
import org.apache.wicket.util.string.Strings;

import com.uwyn.jhighlight.renderer.Renderer;
import com.uwyn.jhighlight.renderer.XhtmlRendererFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	public class SourceModel extends AbstractReadOnlyModel<String>
	{
		/**
		 * Constructor.
		 */
		public SourceModel()
		{
		}

		/**
		 * Returns the contents of the file loaded from the classpath.
		 * 
		 * @return the contents of the file identified by name
		 */
		@Override
		public String getObject()
		{
			// name contains the name of the selected file
			if (Strings.isEmpty(name) &&
				Strings.isEmpty(getPage().getRequest()
					.getRequestParameters()
					.getParameterValue(SOURCE)
					.toOptionalString()))
			{
				return "";
			}
			BufferedReader br = null;
			String source = null;
			try
			{
			 StringBuilder sb = new StringBuilder();
				source = (name != null) ? name : getPage().getRequest()
					.getRequestParameters()
					.getParameterValue(SOURCE)
					.toOptionalString();
				InputStream resourceAsStream = getPageTargetClass().getResourceAsStream(source);
				if (resourceAsStream == null)
				{
					return "Unable to read the source for " + source;
				}
				br = new BufferedReader(new InputStreamReader(resourceAsStream));

				while (br.ready())
				{
					sb.append(br.readLine());
					sb.append("\n");
				}
				int lastDot = source.lastIndexOf('.');
				if (lastDot != -1)
				{
					String type = source.substring(lastDot + 1);
					Renderer renderer = XhtmlRendererFactory.getRenderer(type);
					if (renderer != null)
					{
						return renderer.highlight(source, sb.toString(), "UTF-8", true);
					}
				}
				return Strings.escapeMarkup(sb.toString(), false, true)
					.toString()
					.replaceAll("\n", "<br />");
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
				IOUtils.closeQuietly(br);
			}
		}
	}

	/**
	 * Model for retrieving the contents of a package directory from the class path.
	 */
	public class PackagedResourcesModel extends AbstractReadOnlyModel<List<String>>
	{
		private final List<String> resources = new ArrayList<String>();

		/**
		 * Constructor.
		 */
		public PackagedResourcesModel()
		{
		}

		/**
		 * Clears the list to save space.
		 */
		protected void onDetach()
		{
			resources.clear();
		}

		/**
		 * Returns the list of resources found in the package of the page.
		 * 
		 * @return the list of resources found in the package of the page.
		 */
		@Override
		public List<String> getObject()
		{
			if (resources.isEmpty())
			{
				get(getPageTargetClass());
// PackageName name = PackageName.forClass(page);
// ClassLoader loader = page.getClassLoader();
// String path = Strings.replaceAll(name.getName(), ".", "/").toString();
// try
// {
// // gives the urls for each place where the package
// // path could be found. There could be multiple
// // jar files containing the same package, so each
// // jar file has its own url.
//
// Enumeration<URL> urls = loader.getResources(path);
// while (urls.hasMoreElements())
// {
// URL url = urls.nextElement();
//
// // the url points to the directory structure
// // embedded in the classpath.
//
// getPackageContents(url);
// }
// }
// catch (IOException e)
// {
// log.error("Unable to read resource for: " + path, e);
// }
			}
			return resources;
		}

		/**
		 * Retrieves the package contents for the given URL.
		 * 
		 * @param packageListing
		 *            the url to list.
		 */
		private void getPackageContents(URL packageListing)
		{
			BufferedReader br = null;
			try
			{
				InputStream openStream = Streams.readNonCaching(packageListing);
				if (openStream == null)
				{
					return;
				}
				br = new BufferedReader(new InputStreamReader(openStream));

				while (br.ready())
				{
					String listing = br.readLine();
					String extension = Strings.afterLast(listing, '.');
					if (!listing.endsWith("class"))
					{
						resources.add(listing);
					}
				}
			}
			catch (IOException e)
			{
				log.error("Unable to get package content: " + packageListing.toString(), e);
			}
			finally
			{
				IOUtils.closeQuietly(br);
			}
		}

		private final void addResources(final Class<?> scope,
			final AppendingStringBuffer relativePath, final File dir)
		{
			File[] files = dir.listFiles();
			for (File file : files)
			{
				if (file.isDirectory())
				{
					addResources(scope,
						new AppendingStringBuffer(relativePath).append(file.getName()).append('/'),
						file);
				}
				else
				{
					String name = file.getName();
					String extension = Strings.afterLast(name, '.');
					if (!name.endsWith("class"))
					{
						resources.add(relativePath + name);
					}
				}
			}
		}

		private void get(Class<?> scope)
		{
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
						scanJarFile(scope, packageRef, jf);
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
							scanJarFile(scope, packageRef, jarFile);
							return;
						}
						if (!basedir.isDirectory())
						{
							throw new IllegalStateException(
								"unable to read resources from directory " + basedir);
						}
						addResources(scope, new AppendingStringBuffer(), basedir);
					}
				}
			}
			catch (IOException e)
			{
				throw new WicketRuntimeException(e);
			}
			Collections.sort(resources);
		}

		private void scanJarFile(Class<?> scope, String packageRef, JarFile jf)
		{
			Enumeration<JarEntry> enumeration = jf.entries();
			while (enumeration.hasMoreElements())
			{
				JarEntry je = enumeration.nextElement();
				String name = je.getName();
				if (name.startsWith(packageRef))
				{
					name = name.substring(packageRef.length() + 1);
					String extension = Strings.afterLast(name, '.');
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
							return urlFor(SourcesPage.class,
								          SourcesPage.generatePageParameters(getPageTargetClass(),
									      item.getModel().getObject()));
						}

						@Override
						protected IAjaxCallDecorator getAjaxCallDecorator()
						{
							return new IAjaxCallDecorator()
							{

								public CharSequence decorateOnFailureScript(Component c,
									CharSequence script)
								{
									return "window.location=this.href;";
									// return "alert('It\\'s ok!')";
								}

								public CharSequence decorateOnSuccessScript(Component c,
									CharSequence script)
								{
									if (script == null)
									{
										return "";
									}
									return script;
								}

								public CharSequence decorateScript(Component c, CharSequence script)
								{
									int index = script.toString().indexOf('?');
									if (index >= 0)
									{
										String test = script.subSequence(0, index + 1) +
											PAGE_CLASS + "=1" + "&" +
											script.subSequence(index + 1, script.length());
										return test;

									}
									return script;
								}

							};
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
	 * @param <C>
	 * @param params
	 */
	public <C extends Page> SourcesPage(final PageParameters params)
	{
		filename = new Label("filename", new AbstractReadOnlyModel<String>()
		{

			@Override
			public String getObject()
			{
				return name != null ? name : getPage().getRequest()
					.getRequestParameters()
					.getParameterValue(SOURCE)
					.toOptionalString();
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
	 * @return PageParamets for reconstructing the bookmarkable page.
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
		p.set(PAGE_CLASS, clazz.getName());
		if (fileName != null)
			p.set(SOURCE, fileName);
		return p;
	}

	private String getPageParam()
	{
		return getPage().getRequest()
			.getRequestParameters()
			.getParameterValue(PAGE_CLASS)
			.toOptionalString();
	}

	private Class<? extends Page> getPageTargetClass()
	{
		if (page == null)
		{
			try
			{
				String pageParam = getPageParam();
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
				if (!pageParam.startsWith("org.apache.wicket.examples"))
				{
					if (log.isErrorEnabled())
					{
						log.error("user is trying to access class: " + pageParam +
							" which is not in the scope of org.apache.wicket.examples");
					}
					throw new UnauthorizedInstantiationException(getClass());
				}
				page = (Class<? extends Page>)Class.forName(getPageParam());
			}
			catch (ClassNotFoundException e)
			{
				getRequestCycle().replaceAllRequestHandlers(
					new ErrorCodeRequestHandler(404,
						"Could not find sources for the page you requested"));
			}
		}
		return page;
	}
}
