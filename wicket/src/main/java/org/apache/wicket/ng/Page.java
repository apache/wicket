package org.apache.wicket.ng;

import org.apache.wicket.ng.request.component.PageParameters;
import org.apache.wicket.ng.request.component.RequestablePage;
import org.apache.wicket.ng.request.cycle.RequestCycle;
import org.apache.wicket.ng.request.response.Response;

public class Page extends Component implements RequestablePage
{
	private static final long serialVersionUID = 1L;

	int pageId;
	
	static int pageIdCounter;
	
	public Page()
	{
		this(null);
	}
	
	public Page(PageParameters parameters)
	{
		super("");
		pageId = pageIdCounter++;
		if (parameters == null)
		{
			this.pageParameters = new PageParameters();
		}
		else
		{
			this.pageParameters = parameters;
		}
	}
	
	public static Page get(int id)
	{
		Application app = Application.get();
		return (Page) app.getPageManager().getPage(id);
	}

	public int getPageId()
	{
		return pageId;
	}

	private final PageParameters pageParameters;
	
	public PageParameters getPageParameters()
	{
		return pageParameters;
	}
	
	private int renderCount = 0;

	public int getRenderCount()
	{
		return renderCount;
	}

	public boolean isBookmarkable()
	{
		Boolean bookmarkable = null;
		if (bookmarkable == null)
		{
			try
			{

				if (getClass().getConstructor(new Class[] {}) != null)
				{
					bookmarkable = Boolean.TRUE;
				}

			}
			catch (Exception ignore)
			{
				try
				{
					if (getClass().getConstructor(new Class[] { PageParameters.class }) != null)
					{
						bookmarkable = Boolean.TRUE;
					}
				}
				catch (Exception ignore2)
				{
				}
			}
			if (bookmarkable == null)
			{
				bookmarkable = Boolean.FALSE;
			}
		}
		return bookmarkable.booleanValue();

	}

	public boolean isPageStateless()
	{
		return false;
	}

	public void renderPage()
	{
		++renderCount;
		
		System.out.println("Rendering");
		
		Response response = RequestCycle.get().getResponse();
		response.write("<html>\n");
		
		response.write("<body>\n");
		
		response.write("<p>This is a " + getClass().getName() + "</p>\n");

		for (Component c : getChildren())
		{
			c.renderComponent();
		}
		
		response.write("</body>\n");
		response.write("</html>\n");
	}

	private boolean wasCreatedBookmarkable;
	
	public void setWasCreatedBookmarkable(boolean wasCreatedBookmarkable)
	{
		this.wasCreatedBookmarkable = wasCreatedBookmarkable;
	}
	
	public boolean wasCreatedBookmarkable()
	{
		return wasCreatedBookmarkable;
	}

	public Page getPage()
	{
		return this;
	}
	
	private int markupIdConter = 0;
	
	public int getMarkupIdConterNextValue()
	{
		return markupIdConter++;
	}

	@Override
	public boolean canCallListenerInterface()
	{
		return true;
	}
}
