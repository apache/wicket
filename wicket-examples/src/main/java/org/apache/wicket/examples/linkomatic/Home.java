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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.wicket.Component;
import org.apache.wicket.examples.WicketExamplePage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.ClientSideImageMap;
import org.apache.wicket.markup.html.link.DownloadLink;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.link.PopupSettings;
import org.apache.wicket.markup.html.link.ResourceLink;
import org.apache.wicket.markup.html.pages.RedirectPage;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.parser.filter.RelativePathPrefixHandler;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.SharedResourceReference;
import org.apache.wicket.util.file.Files;
import org.apache.wicket.util.time.Duration;


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
		final Link actionLink = new Link("actionLink")
		{
			@Override
			public void onClick()
			{
				linkClickCount++;
			}
		};
		actionLink.add(new Label("linkClickCount", new PropertyModel<Integer>(this,
			"linkClickCount")));
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
		add(new Label("onClickLinkClickCount", new PropertyModel<Integer>(this,
			"onClickLinkClickCount")));

		// Link to Page1 is a simple external page link
		add(new BookmarkablePageLink<>("page1Link", Page1.class));

		// Link to Page2 is automaticLink, so no code
		// Link to Page3 is an external link which takes a parameter
		BookmarkablePageLink<Void> page3Link = new BookmarkablePageLink<>("page3Link", Page3.class);
		page3Link.getPageParameters()
				.add("bookmarkparameter", "3++2 & 5 � >< space + �");
		add(page3Link);

		// Link to BookDetails page
		add(new Link<Void>("bookDetailsLink")
		{
			@Override
			public void onClick()
			{
				setResponsePage(new BookDetails(new Book("The Hobbit")));
			}
		});

		// Delayed link to BookDetails page
		add(new Link<Void>("bookDetailsLink2")
		{
			@Override
			public void onClick()
			{
				setResponsePage(new BookDetails(new Book("Inside The Matrix")));
			}
		});

		// Image map link example
		Image imageForMap = new Image("imageForMap", new PackageResourceReference(Home.class,
			"ImageMap.gif"));
		add(imageForMap);
		add(new ClientSideImageMap("imageMap", imageForMap).addRectangleArea(
			new BookmarkablePageLink<Page1>("page1", Page1.class), 0, 0, 100, 100)
			.addCircleArea(new BookmarkablePageLink<Page2>("page2", Page2.class), 160, 50, 35)
			.addPolygonArea(new BookmarkablePageLink<Page3>("page3", Page3.class), 212, 79, 241, 4,
				279, 54, 212, 79)
			.add(RelativePathPrefixHandler.RELATIVE_PATH_BEHAVIOR));

		// Popup example
		PopupSettings popupSettings = new PopupSettings("popuppagemap").setHeight(500)
			.setWidth(500);
		add(new BookmarkablePageLink<>("popupLink", Popup.class).setPopupSettings(popupSettings));

		// Popup example
		add(new BookmarkablePageLink<>("popupButtonLink", Popup.class).setPopupSettings(popupSettings));

		// External site link
		add(new ExternalLink("google", "http://www.google.com", "Click this link to go to Google"));

		// And that link as a popup
		PopupSettings googlePopupSettings = new PopupSettings(PopupSettings.RESIZABLE |
			PopupSettings.SCROLLBARS).setHeight(500).setWidth(700);
		add(new ExternalLink("googlePopup", "http://www.google.com",
			"Click this link to go to Google in a popup").setPopupSettings(googlePopupSettings));

		// Shared resource link
		add(new ResourceLink<>("cancelButtonLink", new SharedResourceReference("cancelButton")));

		add(new DownloadLink("downloadLink", new AbstractReadOnlyModel<File>()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public File getObject()
			{
				File tempFile;
				try
				{
					tempFile = File.createTempFile("wicket-examples-download-link--", ".tmp");

					InputStream data = new ByteArrayInputStream("some data".getBytes());
					Files.writeTo(tempFile, data);

				}
				catch (IOException e)
				{
					throw new RuntimeException(e);
				}

				return tempFile;
			}
		}, "Downlöad\"here now.tmp").setCacheDuration(Duration.NONE).setDeleteAfterDownload(true));

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

		Link<Void> linkWithLabel = new Link<Void>("linkWithLabel")
		{

			@Override
			public void onClick()
			{
			}
		};
		linkWithLabel.setBody(Model.of("A link that provides its body with Link.setBody(someModel)"));
		add(linkWithLabel);
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
		 * @param id
		 *            component id
		 */
		public RedirectForm(String id)
		{
			super(id);
			setDefaultModel(new CompoundPropertyModel<>(this));
			add(new TextField<>("redirectUrl"));
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
