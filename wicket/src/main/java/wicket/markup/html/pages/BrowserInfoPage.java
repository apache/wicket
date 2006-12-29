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
package wicket.markup.html.pages;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import wicket.AttributeModifier;
import wicket.MarkupContainer;
import wicket.PageParameters;
import wicket.RequestCycle;
import wicket.markup.html.WebComponent;
import wicket.markup.html.WebMarkupContainer;
import wicket.markup.html.WebPage;
import wicket.markup.html.form.Form;
import wicket.markup.html.form.TextField;
import wicket.model.CompoundPropertyModel;
import wicket.model.Model;
import wicket.protocol.http.ClientProperties;
import wicket.protocol.http.WebRequestCycle;
import wicket.protocol.http.WebSession;
import wicket.protocol.http.request.WebClientInfo;
import wicket.request.ClientInfo;
import wicket.request.target.component.BookmarkablePageRequestTarget;
import wicket.settings.IRequestCycleSettings;

/**
 * <p>
 * This page uses a form post right after the page has loaded in the browser,
 * using JavaScript or alternative means to detect and pass on settings to the
 * embedded form. The form submit method updates this session's
 * {@link wicket.request.ClientInfo} object and then redirects to the original
 * location as was passed in as a URL argument in the constructor.
 * </p>
 * <p>
 * This page is being used by the default implementation of
 * {@link WebRequestCycle#newClientInfo}, which in turn uses
 * {@link IRequestCycleSettings#getGatherExtendedBrowserInfo() a setting} to
 * determine whether this page should be redirected to (it does when it is
 * true).
 * </p>
 * 
 * @author Eelco Hillenius
 */
public class BrowserInfoPage extends WebPage
{
	/**
	 * Holds properties of the client.
	 */
	public static class ClientPropertiesBean implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private String navigatorAppCodeName;
		private String navigatorAppName;
		private String navigatorAppVersion;
		private Boolean navigatorCookieEnabled;
		private Boolean navigatorJavaEnabled = Boolean.FALSE;
		private String navigatorLanguage;
		private String navigatorPlatform;
		private String navigatorUserAgent;
		private String screenColorDepth;
		private String screenHeight;
		private String screenWidth;
		private String utcOffset;

		/**
		 * Gets navigatorAppCodeName.
		 * 
		 * @return navigatorAppCodeName
		 */
		public String getNavigatorAppCodeName()
		{
			return navigatorAppCodeName;
		}

		/**
		 * Gets navigatorAppName.
		 * 
		 * @return navigatorAppName
		 */
		public String getNavigatorAppName()
		{
			return navigatorAppName;
		}

		/**
		 * Gets navigatorAppVersion.
		 * 
		 * @return navigatorAppVersion
		 */
		public String getNavigatorAppVersion()
		{
			return navigatorAppVersion;
		}

		/**
		 * Gets navigatorCookieEnabled.
		 * 
		 * @return navigatorCookieEnabled
		 */
		public Boolean getNavigatorCookieEnabled()
		{
			return navigatorCookieEnabled;
		}

		/**
		 * Gets navigatorJavaEnabled.
		 * 
		 * @return navigatorJavaEnabled
		 */
		public Boolean getNavigatorJavaEnabled()
		{
			return navigatorJavaEnabled;
		}

		/**
		 * Gets navigatorLanguage.
		 * 
		 * @return navigatorLanguage
		 */
		public String getNavigatorLanguage()
		{
			return navigatorLanguage;
		}

		/**
		 * Gets navigatorPlatform.
		 * 
		 * @return navigatorPlatform
		 */
		public String getNavigatorPlatform()
		{
			return navigatorPlatform;
		}

		/**
		 * Gets navigatorUserAgent.
		 * 
		 * @return navigatorUserAgent
		 */
		public String getNavigatorUserAgent()
		{
			return navigatorUserAgent;
		}

		/**
		 * Gets screenColorDepth.
		 * 
		 * @return screenColorDepth
		 */
		public String getScreenColorDepth()
		{
			return screenColorDepth;
		}

		/**
		 * Gets screenHeight.
		 * 
		 * @return screenHeight
		 */
		public String getScreenHeight()
		{
			return screenHeight;
		}

		/**
		 * Gets screenWidth.
		 * 
		 * @return screenWidth
		 */
		public String getScreenWidth()
		{
			return screenWidth;
		}

		/**
		 * Gets utcOffset.
		 * 
		 * @return utcOffset
		 */
		public String getUtcOffset()
		{
			return utcOffset;
		}

		/**
		 * Merge this with the given properties object.
		 * 
		 * @param properties
		 *            the properties object to merge with
		 */
		public void merge(ClientProperties properties)
		{
			properties.setNavigatorAppName(navigatorAppName);
			properties.setNavigatorAppVersion(navigatorAppVersion);
			properties.setNavigatorAppCodeName(navigatorAppCodeName);
			properties.setCookiesEnabled(navigatorCookieEnabled.booleanValue());
			properties.setJavaEnabled(navigatorJavaEnabled.booleanValue());
			properties.setNavigatorLanguage(navigatorLanguage);
			properties.setNavigatorPlatform(navigatorPlatform);
			properties.setNavigatorUserAgent(navigatorUserAgent);
			properties.setScreenWidth(getInt(screenWidth));
			properties.setScreenHeight(getInt(screenHeight));
			properties.setScreenColorDepth(getInt(screenColorDepth));
			properties.setUtcOffset(utcOffset);
		}
		
		private int getInt(String value) 
		{
			int intValue = -1;
			try 
			{
				intValue = Integer.parseInt(value);
			}
			catch (NumberFormatException e)
			{
				// Do nothing
			}
			return intValue;
		}

		/**
		 * Sets navigatorAppCodeName.
		 * 
		 * @param navigatorAppCodeName
		 *            navigatorAppCodeName
		 */
		public void setNavigatorAppCodeName(String navigatorAppCodeName)
		{
			this.navigatorAppCodeName = navigatorAppCodeName;
		}

		/**
		 * Sets navigatorAppName.
		 * 
		 * @param navigatorAppName
		 *            navigatorAppName
		 */
		public void setNavigatorAppName(String navigatorAppName)
		{
			this.navigatorAppName = navigatorAppName;
		}

		/**
		 * Sets navigatorAppVersion.
		 * 
		 * @param navigatorAppVersion
		 *            navigatorAppVersion
		 */
		public void setNavigatorAppVersion(String navigatorAppVersion)
		{
			this.navigatorAppVersion = navigatorAppVersion;
		}

		/**
		 * Sets navigatorCookieEnabled.
		 * 
		 * @param navigatorCookieEnabled
		 *            navigatorCookieEnabled
		 */
		public void setNavigatorCookieEnabled(Boolean navigatorCookieEnabled)
		{
			this.navigatorCookieEnabled = navigatorCookieEnabled;
		}

		/**
		 * Sets navigatorJavaEnabled.
		 * 
		 * @param navigatorJavaEnabled
		 *            navigatorJavaEnabled
		 */
		public void setNavigatorJavaEnabled(Boolean navigatorJavaEnabled)
		{
			this.navigatorJavaEnabled = navigatorJavaEnabled;
		}

		/**
		 * Sets navigatorLanguage.
		 * 
		 * @param navigatorLanguage
		 *            navigatorLanguage
		 */
		public void setNavigatorLanguage(String navigatorLanguage)
		{
			this.navigatorLanguage = navigatorLanguage;
		}

		/**
		 * Sets navigatorPlatform.
		 * 
		 * @param navigatorPlatform
		 *            navigatorPlatform
		 */
		public void setNavigatorPlatform(String navigatorPlatform)
		{
			this.navigatorPlatform = navigatorPlatform;
		}

		/**
		 * Sets navigatorUserAgent.
		 * 
		 * @param navigatorUserAgent
		 *            navigatorUserAgent
		 */
		public void setNavigatorUserAgent(String navigatorUserAgent)
		{
			this.navigatorUserAgent = navigatorUserAgent;
		}

		/**
		 * Sets screenColorDepth.
		 * 
		 * @param screenColorDepth
		 *            screenColorDepth
		 */
		public void setScreenColorDepth(String screenColorDepth)
		{
			this.screenColorDepth = screenColorDepth;
		}

		/**
		 * Sets screenHeight.
		 * 
		 * @param screenHeight
		 *            screenHeight
		 */
		public void setScreenHeight(String screenHeight)
		{
			this.screenHeight = screenHeight;
		}

		/**
		 * Sets screenWidth.
		 * 
		 * @param screenWidth
		 *            screenWidth
		 */
		public void setScreenWidth(String screenWidth)
		{
			this.screenWidth = screenWidth;
		}

		/**
		 * Sets utcOffset.
		 * 
		 * @param utcOffset
		 *            utcOffset
		 */
		public void setUtcOffset(String utcOffset)
		{
			this.utcOffset = utcOffset;
		}
	}

	/**
	 * Form for posting JavaScript properties.
	 */
	private final class PostBackForm extends Form<ClientPropertiesBean>
	{
		private static final long serialVersionUID = 1L;

		/**
		 * Construct.
		 * 
		 * @param parent
		 *            The parent of this component parent component
		 * 
		 * @param id
		 *            component id
		 */
		public PostBackForm(MarkupContainer parent, String id)
		{
			super(parent, id, new CompoundPropertyModel<ClientPropertiesBean>(
					new ClientPropertiesBean()));

			new TextField(this, "navigatorAppName");
			new TextField(this, "navigatorAppVersion");
			new TextField(this, "navigatorAppCodeName");
			new TextField(this, "navigatorCookieEnabled");
			new TextField(this, "navigatorJavaEnabled");
			new TextField(this, "navigatorLanguage");
			new TextField(this, "navigatorPlatform");
			new TextField(this, "navigatorUserAgent");
			new TextField(this, "screenWidth");
			new TextField(this, "screenHeight");
			new TextField(this, "screenColorDepth");
			new TextField(this, "utcOffset");
		}

		/**
		 * @see wicket.markup.html.form.Form#onSubmit()
		 */
		@Override
		protected void onSubmit()
		{
			ClientPropertiesBean propertiesBean = getModelObject();

			WebRequestCycle requestCycle = (WebRequestCycle)getRequestCycle();
			WebSession session = (WebSession)getSession();
			ClientInfo clientInfo = session.getClientInfo();

			if (clientInfo == null)
			{
				clientInfo = new WebClientInfo(requestCycle);
				getSession().setClientInfo(clientInfo);
			}

			if (clientInfo instanceof WebClientInfo)
			{
				WebClientInfo info = (WebClientInfo)clientInfo;
				ClientProperties properties = info.getProperties();
				propertiesBean.merge(properties);
			}
			else
			{
				warnNotUsingWebClientInfo(clientInfo);
			}

			continueToPrevious();
		}
	}

	/** log. */
	private static final Logger log = LoggerFactory.getLogger(BrowserInfoPage.class);

	private static final long serialVersionUID = 1L;

	/** the url to continue to after this page. */
	private String continueTo;

	/**
	 * Bookmarkable constructor. This is not for normal framework client use. It
	 * will be called whenever Javascript is not supported, and the browser info
	 * page's meta refresh fires to this page. Prior to this, the other
	 * constructor should already have been called.
	 * 
	 * @param parameters
	 *            page parameters with the original url in it
	 */
	public BrowserInfoPage(PageParameters parameters)
	{
		String to = (String)parameters.get("cto");
		if (to == null)
		{
			throw new IllegalArgumentException("parameter cto must be provided!");
		}
		setContinueTo(to);
		initComps();
		WebRequestCycle requestCycle = (WebRequestCycle)getRequestCycle();
		WebSession session = (WebSession)getSession();
		ClientInfo clientInfo = session.getClientInfo();
		if (clientInfo == null)
		{
			clientInfo = new WebClientInfo(requestCycle);
			getSession().setClientInfo(clientInfo);
		}
		else if (clientInfo instanceof WebClientInfo)
		{
			WebClientInfo info = (WebClientInfo)clientInfo;
			ClientProperties properties = info.getProperties();
			properties.setJavaEnabled(false);
		}
		else
		{
			warnNotUsingWebClientInfo(clientInfo);
		}
		continueToPrevious();
	}

	/**
	 * Constructor. The page will redirect to the given url after waiting for
	 * the given number of seconds.
	 * 
	 * @param continueTo
	 *            the url to redirect to when the browser info is handled
	 */
	public BrowserInfoPage(final String continueTo)
	{
		if (continueTo == null)
		{
			throw new IllegalArgumentException("Argument continueTo must be not null");
		}
		setContinueTo(continueTo);
		initComps();
	}

	/**
	 * @see wicket.Component#isVersioned()
	 */
	@Override
	public boolean isVersioned()
	{
		return false;
	}

	/**
	 * Continue to the location previous to this interception.
	 */
	protected final void continueToPrevious()
	{
		// continue to original distination
		RequestCycle requestCycle = getRequestCycle();
		// Since we are explicitly redirecting to a page already, we do not
		// want a second redirect to occur automatically
		requestCycle.setRedirect(false);
		// Redirect there
		requestCycle.getResponse().redirect(continueTo);
	}

	/**
	 * Set the url to continue to after this page.
	 * 
	 * @param continueTo
	 *            the url
	 */
	protected final void setContinueTo(String continueTo)
	{
		this.continueTo = continueTo;
	}

	/**
	 * Adds components.
	 */
	private final void initComps()
	{
		WebComponent meta = new WebComponent<Object>(this, "meta");
		PageParameters parameters = new PageParameters();
		parameters.put("cto", continueTo);
		CharSequence url = urlFor(new BookmarkablePageRequestTarget(BrowserInfoPage.class,
				parameters));
		meta.add(new AttributeModifier("content", true, new Model<String>("0; url=" + url)));
		WebMarkupContainer link = new WebMarkupContainer<Object>(this, "link");
		link.add(new AttributeModifier("href", true, new Model<CharSequence>(url)));
		new PostBackForm(this, "postback");
	}

	/**
	 * Log a warning that for in order to use this page, you should really be
	 * using {@link WebClientInfo}.
	 * 
	 * @param clientInfo
	 *            the actual client info object
	 */
	private void warnNotUsingWebClientInfo(ClientInfo clientInfo)
	{
		log.warn("using " + getClass().getName() + " makes no sense if you are not using "
				+ WebClientInfo.class.getName() + " (you are using "
				+ clientInfo.getClass().getName() + " instead)");
	}
}
