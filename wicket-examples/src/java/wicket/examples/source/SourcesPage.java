package wicket.examples.source;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import wicket.Component;
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
import wicket.util.string.Strings;

/**
 * Displays the resources in a packages directory in a browsable format.
 * 
 * @author Martijn Dashorst
 */
public class SourcesPage extends WebPage
{
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
		 * @param component
		 *            ignored
		 * @return the contents of the file identified by name
		 */
		public Object getObject(Component component)
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

				br = new BufferedReader(new InputStreamReader(page.getResourceAsStream(name)));

				while (br.ready())
				{
					sb.append(br.readLine());
					sb.append("\n");
				}
				return sb.toString();
			}
			catch (IOException e)
			{
				e.printStackTrace();
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
	public class PackagedResourcesModel extends AbstractReadOnlyModel implements IDetachable
	{
		private final List resources = new ArrayList();

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
		 * @param component
		 *            ignored.
		 * @return the list of resources found in the package of the page.
		 */
		public Object getObject(Component component)
		{
			if (resources.isEmpty())
			{
				PackageName name = PackageName.forClass(page);
				ClassLoader loader = page.getClassLoader();
				String path = Strings.replaceAll(name.getName(), ".", "/");
				try
				{
					// gives the urls for each place where the package
					// path could be found. There could be multiple
					// jar files containing the same package, so each
					// jar file has its own url.

					Enumeration urls = loader.getResources(path);
					while (urls.hasMoreElements())
					{
						URL url = (URL)urls.nextElement();

						// the url points to the directory structure
						// embedded in the classpath.

						getPackageContents(url);
					}
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
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
				br = new BufferedReader(new InputStreamReader(packageListing.openStream()));

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
				e.printStackTrace(System.err);
			}
			finally
			{
				IOUtils.closeQuietly(br);
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
			ListView lv = new ListView("file", new PackagedResourcesModel())
			{
				protected void populateItem(ListItem item)
				{
					AjaxFallbackLink link = new AjaxFallbackLink("link", item.getModel())
					{
						public void onClick(AjaxRequestTarget target)
						{
							setName(getModelObjectAsString());
							target.addComponent(codePanel);
							target.addComponent(filename);
						}
					};
					link.add(new Label("name", item.getModelObjectAsString()));
					item.add(link);
				}
			};
			add(lv);
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
		 * @param id
		 *            the component id
		 */
		public CodePanel(String id)
		{
			super(id);
			Label code = new Label("code", new SourceModel());
			code.setEscapeModelStrings(true);
			code.setOutputMarkupId(true);
			add(code);
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

		filename = new Label("filename", new PropertyModel(this, "name"));
		filename.setOutputMarkupId(true);
		add(filename);
		codePanel = new CodePanel("codepanel").setOutputMarkupId(true);
		add(codePanel);
		add(new FilesBrowser("filespanel"));
		add(new PopupCloseLink("close"));
	}
}
