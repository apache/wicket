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
package wicket.examples.linkomatic;

import wicket.Component;
import wicket.MarkupContainer;
import wicket.Page;
import wicket.PageMap;
import wicket.ResourceReference;
import wicket.examples.WicketExamplePage;
import wicket.markup.html.basic.Label;
import wicket.markup.html.form.Form;
import wicket.markup.html.form.TextField;
import wicket.markup.html.link.BookmarkablePageLink;
import wicket.markup.html.link.ExternalLink;
import wicket.markup.html.link.IPageLink;
import wicket.markup.html.link.ImageMap;
import wicket.markup.html.link.Link;
import wicket.markup.html.link.PageLink;
import wicket.markup.html.link.PopupSettings;
import wicket.markup.html.link.ResourceLink;
import wicket.markup.html.pages.RedirectPage;
import wicket.markup.html.panel.FeedbackPanel;
import wicket.model.CompoundPropertyModel;
import wicket.model.PropertyModel;

/**
 * Demonstrates different flavors of hyperlinks.
 * 
 * @author Jonathan Locke
 */
public class Home extends WicketExamplePage
{
	/** click count for Link. */
	private int linkClickCount = 0;

	/** click count for OnClickLink. */
	private int onClickLinkClickCount = 0;

	/**
	 * Constructor
	 */
	public Home()
	{
		// Action link counts link clicks
		final Link actionLink = new Link(this, "actionLink")
		{
			@Override
			public void onClick()
			{
				linkClickCount++;
			}
		};
		actionLink.getModel();


		new Label(actionLink, "linkClickCount", new PropertyModel(this, "linkClickCount"));

		// Action link counts link clicks on works with onclick handler
		new Link<String>(this, "actionOnClickLink")
		{
			@Override
			public void onClick()
			{
				onClickLinkClickCount++;
			}
		};

		new Label(this, "onClickLinkClickCount", new PropertyModel(this, "onClickLinkClickCount"));

		// Link to Page1 is a simple external page link
		new BookmarkablePageLink(this, "page1Link", Page1.class);

		// Link to Page2 is automaticLink, so no code
		// Link to Page3 is an external link which takes a parameter
		new BookmarkablePageLink(this, "page3Link", Page3.class).setParameter("bookmarkparameter",
				"3++2 & 5 € >< space + á");

		// Link to BookDetails page
		new PageLink(this, "bookDetailsLink", new IPageLink()
		{
			public Page getPage()
			{
				return new BookDetails(new Book("The Hobbit"));
			}

			public Class getPageIdentity()
			{
				return BookDetails.class;
			}
		});

		// Delayed link to BookDetails page
		new PageLink(this, "bookDetailsLink2", new IPageLink()
		{
			public Page getPage()
			{
				return new BookDetails(new Book("Inside The Matrix"));
			}

			public Class getPageIdentity()
			{
				return BookDetails.class;
			}
		});

		// FIXME image map doesn't work anymore!
		// Image map link example
		new ImageMap(this, "imageMap").addRectangleLink(0, 0, 100, 100,
				new BookmarkablePageLink(this, "page1", Page1.class)).addCircleLink(160, 50, 35,
				new BookmarkablePageLink(this, "page2", Page2.class)).addPolygonLink(
				new int[] { 212, 79, 241, 4, 279, 54, 212, 79 },
				new BookmarkablePageLink(this, "page3", Page3.class));

		// Popup example
		PopupSettings popupSettings = new PopupSettings(PageMap.forName("popuppagemap")).setHeight(
				500).setWidth(500);
		new BookmarkablePageLink(this, "popupLink", Popup.class).setPopupSettings(popupSettings);

		// Popup example
		new BookmarkablePageLink(this, "popupButtonLink", Popup.class)
				.setPopupSettings(popupSettings);

		// External site link
		new ExternalLink(this, "google", "http://www.google.com", "Click this link to go to Google");

		// And that link as a popup
		PopupSettings googlePopupSettings = new PopupSettings(PopupSettings.RESIZABLE
				| PopupSettings.SCROLLBARS).setHeight(500).setWidth(700);
		new ExternalLink(this, "googlePopup", "http://www.google.com",
				"Click this link to go to Google in a popup").setPopupSettings(googlePopupSettings);

		// Shared resource link
		new ResourceLink(this, "cancelButtonLink", new ResourceReference("cancelButton"));

		// redirect to external url form
		new FeedbackPanel(this, "feedback");
		new RedirectForm(this, "redirectForm");

		Link linkToAnchor = new Link(this, "linkToAnchor")
		{
			@Override
			public void onClick()
			{
			}
		};
		new Link(this, "anotherlinkToAnchor")
		{
			@Override
			public void onClick()
			{
			}
		};
		Component anchorLabel = new Label(this, "anchorLabel",
				"this label is here to function as an anchor for a link").setOutputMarkupId(true);
		linkToAnchor.setAnchor(anchorLabel);
	}

	/**
	 * Form that handles a redirect.
	 */
	private final class RedirectForm extends Form<RedirectForm>
	{
		/** receives form input. */
		private String redirectUrl = "http://www.theserverside.com";

		/**
		 * Construct.
		 * 
		 * @param parent
		 * @param id
		 *            component id
		 */
		public RedirectForm(MarkupContainer parent, String id)
		{
			super(parent, id);
			setModel(new CompoundPropertyModel<RedirectForm>(this));
			new TextField(this, "redirectUrl");
		}

		/**
		 * @see wicket.markup.html.form.Form#onSubmit()
		 */
		@Override
		protected void onSubmit()
		{
			setResponsePage(new RedirectPage(redirectUrl));
		}

		/**
		 * Gets the redirectUrl.
		 * 
		 * @return redirectUrl
		 */
		public String getRedirectUrl()
		{
			return redirectUrl;
		}

		/**
		 * Sets the redirectUrl.
		 * 
		 * @param redirectUrl
		 *            redirectUrl
		 */
		public void setRedirectUrl(String redirectUrl)
		{
			this.redirectUrl = redirectUrl;
		}
	}

	/**
	 * @return Returns the linkClickCount.
	 */
	public int getLinkClickCount()
	{
		return linkClickCount;
	}

	/**
	 * @param linkClickCount
	 *            The linkClickCount to set.
	 */
	public void setLinkClickCount(final int linkClickCount)
	{
		this.linkClickCount = linkClickCount;
	}

	/**
	 * Gets onClickLinkClickCount.
	 * 
	 * @return onClickLinkClickCount
	 */
	public int getOnClickLinkClickCount()
	{
		return onClickLinkClickCount;
	}

	/**
	 * Sets onClickLinkClickCount.
	 * 
	 * @param onClickLinkClickCount
	 *            onClickLinkClickCount
	 */
	public void setOnClickLinkClickCount(int onClickLinkClickCount)
	{
		this.onClickLinkClickCount = onClickLinkClickCount;
	}

	/**
	 * @see wicket.Component#isVersioned()
	 */
	@Override
	public boolean isVersioned()
	{
		return false;
	}
}
