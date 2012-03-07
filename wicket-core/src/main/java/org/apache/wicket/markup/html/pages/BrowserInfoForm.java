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
package org.apache.wicket.markup.html.pages;

import org.apache.wicket.util.io.IClusterable;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.protocol.http.ClientProperties;
import org.apache.wicket.protocol.http.WebSession;
import org.apache.wicket.protocol.http.request.WebClientInfo;
import org.apache.wicket.core.request.ClientInfo;
import org.apache.wicket.request.cycle.RequestCycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Form for posting JavaScript properties.
 */
public class BrowserInfoForm extends Panel
{
	/** log. */
	private static final Logger log = LoggerFactory.getLogger(BrowserInfoForm.class);

	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 * 
	 * @param id
	 *            component id
	 */
	public BrowserInfoForm(String id)
	{
		super(id);

		Form<ClientPropertiesBean> form = new Form<ClientPropertiesBean>("postback",
			new CompoundPropertyModel<ClientPropertiesBean>(new ClientPropertiesBean()))
		{
			private static final long serialVersionUID = 1L;

			/**
			 * @see org.apache.wicket.markup.html.form.Form#onSubmit()
			 */
			@Override
			protected void onSubmit()
			{
				ClientPropertiesBean propertiesBean = getModelObject();

				RequestCycle requestCycle = getRequestCycle();
				WebSession session = (WebSession)getSession();
				WebClientInfo clientInfo = session.getClientInfo();

				if (clientInfo == null)
				{
					clientInfo = new WebClientInfo(requestCycle);
					getSession().setClientInfo(clientInfo);
				}

				ClientProperties properties = clientInfo.getProperties();
				propertiesBean.merge(properties);

				afterSubmit();
			}
		};
		form.add(new TextField<String>("navigatorAppName"));
		form.add(new TextField<String>("navigatorAppVersion"));
		form.add(new TextField<String>("navigatorAppCodeName"));
		form.add(new TextField<Boolean>("navigatorCookieEnabled"));
		form.add(new TextField<Boolean>("navigatorJavaEnabled"));
		form.add(new TextField<String>("navigatorLanguage"));
		form.add(new TextField<String>("navigatorPlatform"));
		form.add(new TextField<String>("navigatorUserAgent"));
		form.add(new TextField<String>("screenWidth"));
		form.add(new TextField<String>("screenHeight"));
		form.add(new TextField<String>("screenColorDepth"));
		form.add(new TextField<String>("utcOffset"));
		form.add(new TextField<String>("utcDSTOffset"));
		form.add(new TextField<String>("browserWidth"));
		form.add(new TextField<String>("browserHeight"));
		form.add(new TextField<String>("hostname"));
		add(form);
	}


	/**
	 * Log a warning that for in order to use this page, you should really be using
	 * {@link WebClientInfo}.
	 * 
	 * @param clientInfo
	 *            the actual client info object
	 */
	void warnNotUsingWebClientInfo(ClientInfo clientInfo)
	{
		log.warn("using " + getClass().getName() + " makes no sense if you are not using " +
			WebClientInfo.class.getName() + " (you are using " + clientInfo.getClass().getName() +
			" instead)");
	}

	protected void afterSubmit()
	{

	}

	/**
	 * Holds properties of the client.
	 */
	public static class ClientPropertiesBean implements IClusterable
	{
		private static final long serialVersionUID = 1L;

		private String navigatorAppCodeName;
		private String navigatorAppName;
		private String navigatorAppVersion;
		private Boolean navigatorCookieEnabled = Boolean.FALSE;
		private Boolean navigatorJavaEnabled = Boolean.FALSE;
		private String navigatorLanguage;
		private String navigatorPlatform;
		private String navigatorUserAgent;
		private String screenColorDepth;
		private String screenHeight;
		private String screenWidth;
		private String utcOffset;
		private String utcDSTOffset;
		private String browserWidth;
		private String browserHeight;
		private String hostname;

		/**
		 * Gets browserHeight.
		 * 
		 * @return browserHeight
		 */
		public String getBrowserHeight()
		{
			return browserHeight;
		}

		/**
		 * Gets browserWidth.
		 * 
		 * @return browserWidth
		 */
		public String getBrowserWidth()
		{
			return browserWidth;
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
		 * Gets utcDSTOffset.
		 * 
		 * @return utcOffset
		 */
		public String getUtcDSTOffset()
		{
			return utcDSTOffset;
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
			properties.setCookiesEnabled((navigatorCookieEnabled != null) ? navigatorCookieEnabled
				: false);
			properties.setJavaEnabled((navigatorJavaEnabled != null) ? navigatorJavaEnabled : false);
			properties.setNavigatorLanguage(navigatorLanguage);
			properties.setNavigatorPlatform(navigatorPlatform);
			properties.setNavigatorUserAgent(navigatorUserAgent);
			properties.setScreenWidth(getInt(screenWidth));
			properties.setScreenHeight(getInt(screenHeight));
			properties.setBrowserWidth(getInt(browserWidth));
			properties.setBrowserHeight(getInt(browserHeight));
			properties.setScreenColorDepth(getInt(screenColorDepth));
			properties.setUtcOffset(utcOffset);
			properties.setUtcDSTOffset(utcDSTOffset);
			properties.setHostname(hostname);
		}

		/**
		 * Sets browserHeight.
		 * 
		 * @param browserHeight
		 *            browserHeight
		 */
		public void setBrowserHeight(String browserHeight)
		{
			this.browserHeight = browserHeight;
		}

		/**
		 * Sets browserWidth.
		 * 
		 * @param browserWidth
		 *            browserWidth
		 */
		public void setBrowserWidth(String browserWidth)
		{
			this.browserWidth = browserWidth;
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
		 * @param hostname
		 *            the hostname shown in the browser.
		 */
		public void setHostname(String hostname)
		{
			this.hostname = hostname;
		}

		/**
		 * @return The clients hostname shown in the browser
		 */
		public String getHostname()
		{
			return hostname;
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

		/**
		 * Sets utcDSTOffset.
		 * 
		 * @param utcDSTOffset
		 *            utcDSTOffset
		 */
		public void setUtcDSTOffset(String utcDSTOffset)
		{
			this.utcDSTOffset = utcDSTOffset;
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

	}
}
