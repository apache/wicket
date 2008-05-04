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
package org.apache.wicket.examples.linkomatic;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.PageMap;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.examples.WicketExamplePage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.link.IPageLink;
import org.apache.wicket.markup.html.link.ImageMap;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.link.PageLink;
import org.apache.wicket.markup.html.link.PopupSettings;
import org.apache.wicket.markup.html.link.ResourceLink;
import org.apache.wicket.markup.html.pages.RedirectPage;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.parser.filter.RelativePathPrefixHandler;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.PropertyModel;


/**
 * Demonstrates different flavors of hyperlinks.
 * 
 * @author Jonathan Locke
 */
public class Home extends WicketExamplePage<Void>
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
		final Link actionLink = new Link("actionLink")
		{
			@Override
			public void onClick()
			{
				linkClickCount++;
			}
		};
		actionLink.add(new Label("linkClickCount", new PropertyModel(this, "linkClickCount")));
		add(actionLink);

		// Action link counts link clicks on works with onclick handler
		final Link actionOnClickLink = new Link("actionOnClickLink")
		{
			@Override
			public void onClick()
			{
				onClickLinkClickCount++;
			}
		};

		add(actionOnClickLink);
		add(new Label("onClickLinkClickCount", new PropertyModel(this, "onClickLinkClickCount")));

		// Link to Page1 is a simple external page link
		add(new BookmarkablePageLink("page1Link", Page1.class));

		// Link to Page2 is automaticLink, so no code
		// Link to Page3 is an external link which takes a parameter
		add(new BookmarkablePageLink("page3Link", Page3.class).setParameter("bookmarkparameter",
			"3++2 & 5 � >< space + �"));

		// Link to BookDetails page
		add(new PageLink("bookDetailsLink", new IPageLink()
		{
			public Page getPage()
			{
				return new BookDetails(new Book("The Hobbit"));
			}

			public Class getPageIdentity()
			{
				return BookDetails.class;
			}
		}));

		// Delayed link to BookDetails page
		add(new PageLink("bookDetailsLink2", new IPageLink()
		{
			public Page getPage()
			{
				return new BookDetails(new Book("Inside The Matrix"));
			}

			public Class getPageIdentity()
			{
				return BookDetails.class;
			}
		}));

		// Image map link example
		add(new ImageMap("imageMap").addRectangleLink(0, 0, 100, 100,
			new BookmarkablePageLink("page1", Page1.class)).addCircleLink(160, 50, 35,
			new BookmarkablePageLink("page2", Page2.class)).addPolygonLink(
			new int[] { 212, 79, 241, 4, 279, 54, 212, 79 },
			new BookmarkablePageLink("page3", Page3.class)).add(
			RelativePathPrefixHandler.RELATIVE_PATH_BEHAVIOR));

		// Popup example
		PopupSettings popupSettings = new PopupSettings(PageMap.forName("popuppagemap")).setHeight(
			500).setWidth(500);
		add(new BookmarkablePageLink("popupLink", Popup.class).setPopupSettings(popupSettings));

		// Popup example
		add(new BookmarkablePageLink("popupButtonLink", Popup.class).setPopupSettings(popupSettings));

		// External site link
		add(new ExternalLink("google", "http://www.google.com", "Click this link to go to Google"));

		// And that link as a popup
		PopupSettings googlePopupSettings = new PopupSettings(PopupSettings.RESIZABLE |
			PopupSettings.SCROLLBARS).setHeight(500).setWidth(700);
		add(new ExternalLink("googlePopup", "http://www.google.com",
			"Click this link to go to Google in a popup").setPopupSettings(googlePopupSettings));

		// Shared resource link
		add(new ResourceLink("cancelButtonLink", new ResourceReference("cancelButton")));

		// redirect to external url form
		FeedbackPanel feedbackPanel = new FeedbackPanel("feedback");
		add(feedbackPanel);
		add(new RedirectForm("redirectForm"));

		Link linkToAnchor = new Link("linkToAnchor")
		{
			@Override
			public void onClick()
			{
			}
		};
		add(linkToAnchor);
		Link anotherlinkToAnchor = new Link("anotherlinkToAnchor")
		{
			@Override
			public void onClick()
			{
			}
		};
		add(anotherlinkToAnchor);
		Component anchorLabel = new Label("anchorLabel",
			"this label is here to function as an anchor for a link").setOutputMarkupId(true);
		add(anchorLabel);
		linkToAnchor.setAnchor(anchorLabel);
	}

	/**
	 * Form that handles a redirect.
	 */
	private final class RedirectForm extends Form
	{
		/** receives form input. */
		private String redirectUrl = "http://www.theserverside.com";

		/**
		 * Construct.
		 * 
		 * @param id
		 *            component id
		 */
		public RedirectForm(String id)
		{
			super(id);
			setModel(new CompoundPropertyModel(this));
			add(new TextField("redirectUrl"));
		}

		/**
		 * @see org.apache.wicket.markup.html.form.Form#onSubmit()
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
	 * @see org.apache.wicket.Component#isVersioned()
	 */
	@Override
	public boolean isVersioned()
	{
		return false;
	}
}
