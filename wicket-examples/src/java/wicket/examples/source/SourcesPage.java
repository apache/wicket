/*
 * $Id: SourcesPage.java 5838 2006-05-24 20:44:49 +0000 (Wed, 24 May 2006)
 * joco01 $ $Revision$ $Date: 2006-05-24 20:44:49 +0000 (Wed, 24 May
 * 2006) $
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
package wicket.examples.source;

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
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.Component;
import wicket.MarkupContainer;
import wicket.WicketRuntimeException;
import wicket.ajax.AjaxRequestTarget;
import wicket.ajax.markup.html.AjaxFallbackLink;
import wicket.markup.html.WebMarkupContainer;
import wicket.markup.html.WebPage;
import wicket.markup.html.basic.Label;
import wicket.markup.html.link.PopupCloseLink;
import wicket.markup.html.list.ListItem;
import wicket.markup.html.list.ListView;
import wicket.model.AbstractReadOnlyModel;
import wicket.model.IDetachable;
import wicket.model.PropertyModel;
import wicket.util.io.IOUtils;
import wicket.util.lang.PackageName;
import wicket.util.string.AppendingStringBuffer;
import wicket.util.string.Strings;

/**
 * Displays the resources in a packages directory in a browsable format.
 * 
 * @author Martijn Dashorst
 */
public class SourcesPage extends WebPage
{
	private static final Log log = LogFactory.getLog(SourcesPage.class);

	/**
	 * Model for retrieving the source code from the classpath of a packaged
	 * resource.
	 */
	public class SourceModel extends AbstractReadOnlyModel
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
		public Object getObject()
		{
			// name contains the name of the selected file
			if (Strings.isEmpty(name))
			{
				return "";
			}
			BufferedReader br = null;
			try
			{
				StringBuffer sb = new StringBuffer();

				InputStream resourceAsStream = page.getResourceAsStream(name);
				if (resourceAsStream == null)
				{
					return "Unable to read the source for " + name;
				}
				br = new BufferedReader(new InputStreamReader(resourceAsStream));

				while (br.ready())
				{
					sb.append(br.readLine());
					sb.append("\n");
				}
				return sb.toString();
			}
			catch (IOException e)
			{
				log.error("Unable to read resource stream for: " + name + "; Page="
						+ page.toString(), e);
				return "";
			}
			finally
			{
				IOUtils.closeQuietly(br);
			}
		}
	}

	/**
	 * Model for retrieving the contents of a package directory from the class
	 * path.
	 */
	public class PackagedResourcesModel extends AbstractReadOnlyModel<List<String>> implements IDetachable
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
				get(page);
			}
			return resources;
		}

		private final void addResources(final Class scope,
				final AppendingStringBuffer relativePath, final File dir)
		{
			File[] files = dir.listFiles();
			for (File file : files)
			{
				if (file.isDirectory())
				{
					addResources(scope, new AppendingStringBuffer(relativePath).append(
							file.getName()).append('/'), file);
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

		private void get(Class scope)
		{
			String packageRef = Strings.replaceAll(PackageName.forClass(scope).getName(), ".", "/")
					.toString();
			ClassLoader loader = scope.getClassLoader();
			try
			{
				// loop through the resources of the package
				Enumeration packageResources = loader.getResources(packageRef);
				while (packageResources.hasMoreElements())
				{
					URL resource = (URL)packageResources.nextElement();
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
							{
								index = lowerJarZipPart.indexOf(".jar");
							}
							if (index == -1)
							{
								throw e;
							}

							String filename = jarZipPart.substring(0, index + 4); // 4 =
							// len
							// of
							// ".jar"
							// or
							// ".zip"
							log
									.debug("trying the filename: " + filename
											+ " to load as a zip/jar.");
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

			return;
		}

		private void scanJarFile(Class scope, String packageRef, JarFile jf)
		{
			Enumeration enumeration = jf.entries();
			while (enumeration.hasMoreElements())
			{
				JarEntry je = (JarEntry)enumeration.nextElement();
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
		 * @param parent
		 *            The parent of this component The parent of this component.
		 * @param id
		 *            the component identifier
		 */
		public FilesBrowser(MarkupContainer parent, String id)
		{
			super(parent, id);
			new ListView<String>(this, "file", new PackagedResourcesModel())
			{
				@Override
				protected void populateItem(ListItem<String> item)
				{
					AjaxFallbackLink link = new AjaxFallbackLink<String>(item, "link", item.getModel())
					{
						@Override
						public void onClick(AjaxRequestTarget target)
						{
							setName(getModelObjectAsString());
							target.addComponent(codePanel);
							target.addComponent(filename);
						}
					};
					new Label(link, "name", item.getModelObjectAsString());
				}
			};
		}
	}

	/**
	 * Container for displaying the source of the selected page, resource or
	 * other element from the package.
	 */
	public class CodePanel extends WebMarkupContainer
	{
		/**
		 * Constructor.
		 * 
		 * @param parent
		 *            The parent of this component The parent of this component.
		 * @param id
		 *            the component id
		 */
		public CodePanel(MarkupContainer parent, String id)
		{
			super(parent, id);
			Label code = new Label(this, "code", new SourceModel());
			code.setEscapeModelStrings(true);
			code.setOutputMarkupId(true);
		}
	}

	/**
	 * The selected name of the packaged resource to display.
	 */
	private String name;

	/**
	 * The class of the page of which the sources need to be displayed.
	 */
	private Class page;

	/**
	 * The panel for setting the ajax calls.
	 */
	private Component codePanel;

	private Label filename;

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
	 * Default constructor, only used for test purposes.
	 */
	public SourcesPage()
	{
		this(SourcesPage.class);
	}

	/**
	 * Constructor.
	 * 
	 * @param page
	 *            the page where the sources need to be shown from.
	 */
	public SourcesPage(Class page)
	{
		this.page = page;

		filename = new Label(this, "filename", new PropertyModel(this, "name"));
		filename.setOutputMarkupId(true);
		codePanel = new CodePanel(this, "codepanel").setOutputMarkupId(true);
		new FilesBrowser(this, "filespanel");
		new PopupCloseLink(this, "close");
	}
}
