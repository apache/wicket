/*
 * $Id$ $Revision:
 * 1.1 $ $Date$
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
package wicket.markup.html.pages;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.AttributeModifier;
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

/**
 * <p>
 * This page uses posts a form right after the page has loaded in the browser,
 * using JavaScript to detect and pass on settings to the embedded form. The
 * form submit method updates this session's {@link wicket.request.ClientInfo}
 * object and then redirects to the original location as was passed in as a URL
 * argument in the constructor.
 * </p>
 * <p>
 * Typical use of this page is to provide a custom WebRequestCycle, and override
 * method newClientInfo() like this:
 * 
 * <pre>
 * protected ClientInfo newClientInfo()
 * {
 * 	setRequestTarget(new PageRequestTarget(new BrowserInfoPage(getRequest().getURL())));
 * 	return new WebClientInfo(this);
 * }
 * </pre>
 * 
 * This will result in initially using WebClientInfo but immediately letting
 * this page poll for further results and merge them when they are found. Also
 * note that you don't do browser detection until it is actually needed, which
 * is probably a good idea in many cases.
 * </p>
 * <p>
 * 
 * </p>
 * 
 * @author Eelco Hillenius
 */
public class BrowserInfoPage extends WebPage
{
	private static final long serialVersionUID = 1L;

	/** log. */
	private static final Log log = LogFactory.getLog(BrowserInfoPage.class);

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
			properties.setProperty(ClientProperties.NAVIGATOR_JAVA_ENABLED, Boolean.FALSE);
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
	 * Adds components.
	 */
	private final void initComps()
	{
		WebComponent meta = new WebComponent("meta");
		PageParameters<String,Object> parameters = new PageParameters<String,Object>();
		parameters.put("cto", continueTo);
		CharSequence url = urlFor(new BookmarkablePageRequestTarget(BrowserInfoPage.class, parameters));
		meta.add(new AttributeModifier("content", true, new Model("0; url=" + url)));
		add(meta);
		WebMarkupContainer link = new WebMarkupContainer("link");
		link.add(new AttributeModifier("href", true, new Model((Serializable)url)));
		add(link);
		add(new PostBackForm("postback"));
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
	 * Form for posting JavaScript properties.
	 */
	private final class PostBackForm extends Form
	{
		private static final long serialVersionUID = 1L;

		/**
		 * Construct.
		 * 
		 * @param id
		 *            component id
		 */
		public PostBackForm(String id)
		{
			super(id, new CompoundPropertyModel(new ClientPropertiesBean()));

			add(new TextField("navigatorAppName"));
			add(new TextField("navigatorAppVersion"));
			add(new TextField("navigatorAppCodeName"));
			add(new TextField("navigatorCookieEnabled"));
			add(new TextField("navigatorJavaEnabled"));
			add(new TextField("navigatorLanguage"));
			add(new TextField("navigatorPlatform"));
			add(new TextField("navigatorUserAgent"));
			add(new TextField("screenWidth"));
			add(new TextField("screenHeight"));
			add(new TextField("screenColorDepth"));
			add(new TextField("utcOffset"));
		}

		/**
		 * @see wicket.markup.html.form.Form#onSubmit()
		 */
		protected void onSubmit()
		{
			ClientPropertiesBean propertiesBean = (ClientPropertiesBean)getModelObject();

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
	 * @see wicket.Component#isVersioned()
	 */
	public boolean isVersioned()
	{
		return false;
	}

	/**
	 * Holds properties of the client.
	 */
	private static final class ClientPropertiesBean implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private String navigatorAppName;
		private String navigatorAppVersion;
		private String navigatorAppCodeName;
		private Boolean navigatorCookieEnabled;
		private Boolean navigatorJavaEnabled = Boolean.FALSE;
		private String navigatorLanguage;
		private String navigatorPlatform;
		private String navigatorUserAgent;
		private String screenWidth;
		private String screenHeight;
		private String screenColorDepth;
		private String utcOffset;

		/**
		 * Merge this with the given properties object.
		 * 
		 * @param properties
		 *            the properties object to merge with
		 */
		public void merge(ClientProperties properties)
		{
			properties.setProperty(ClientProperties.NAVIGATOR_APP_NAME, navigatorAppName);
			properties.setProperty(ClientProperties.NAVIGATOR_APP_VERSION, navigatorAppVersion);
			properties.setProperty(ClientProperties.NAVIGATOR_APP_CODE_NAME, navigatorAppCodeName);
			properties.setProperty(ClientProperties.NAVIGATOR_COOKIE_ENABLED, navigatorAppCodeName);
			properties.setProperty(ClientProperties.NAVIGATOR_JAVA_ENABLED, navigatorJavaEnabled);
			properties.setProperty(ClientProperties.NAVIGATOR_LANGUAGE, navigatorLanguage);
			properties.setProperty(ClientProperties.NAVIGATOR_PLATFORM, navigatorPlatform);
			properties.setProperty(ClientProperties.NAVIGATOR_USER_AGENT, navigatorUserAgent);
			properties.setProperty(ClientProperties.SCREEN_WIDTH, screenWidth);
			properties.setProperty(ClientProperties.SCREEN_HEIGHT, screenHeight);
			properties.setProperty(ClientProperties.SCREEN_COLOR_DEPTH, screenColorDepth);
			properties.setProperty(ClientProperties.UTC_OFFSET, utcOffset);
		}

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
		 * Gets navigatorAppName.
		 * 
		 * @return navigatorAppName
		 */
		public String getNavigatorAppName()
		{
			return navigatorAppName;
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
		 * Gets navigatorAppVersion.
		 * 
		 * @return navigatorAppVersion
		 */
		public String getNavigatorAppVersion()
		{
			return navigatorAppVersion;
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
		 * Gets navigatorCookieEnabled.
		 * 
		 * @return navigatorCookieEnabled
		 */
		public Boolean getNavigatorCookieEnabled()
		{
			return navigatorCookieEnabled;
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
		 * Gets navigatorJavaEnabled.
		 * 
		 * @return navigatorJavaEnabled
		 */
		public Boolean getNavigatorJavaEnabled()
		{
			return navigatorJavaEnabled;
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
		 * Gets navigatorLanguage.
		 * 
		 * @return navigatorLanguage
		 */
		public String getNavigatorLanguage()
		{
			return navigatorLanguage;
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
		 * Gets navigatorPlatform.
		 * 
		 * @return navigatorPlatform
		 */
		public String getNavigatorPlatform()
		{
			return navigatorPlatform;
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
		 * Gets navigatorUserAgent.
		 * 
		 * @return navigatorUserAgent
		 */
		public String getNavigatorUserAgent()
		{
			return navigatorUserAgent;
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
		 * Gets screenColorDepth.
		 * 
		 * @return screenColorDepth
		 */
		public String getScreenColorDepth()
		{
			return screenColorDepth;
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
		 * Gets screenHeight.
		 * 
		 * @return screenHeight
		 */
		public String getScreenHeight()
		{
			return screenHeight;
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
		 * Gets screenWidth.
		 * 
		 * @return screenWidth
		 */
		public String getScreenWidth()
		{
			return screenWidth;
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
		 * Gets utcOffset.
		 * 
		 * @return utcOffset
		 */
		public String getUtcOffset()
		{
			return utcOffset;
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
}
