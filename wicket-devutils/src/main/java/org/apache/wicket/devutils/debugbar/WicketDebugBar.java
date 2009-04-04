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
package org.apache.wicket.devutils.debugbar;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Application;
import org.apache.wicket.MetaDataKey;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.devutils.DevUtilsPanel;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.JavascriptPackageResource;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;

/**
 * 
 * @author Jeremy Thomerson <jthomerson@apache.org>
 */
public class WicketDebugBar extends DevUtilsPanel {

	private static final MetaDataKey<List<IDebugBarContributor>> CONTRIBS_META_KEY = new MetaDataKey<List<IDebugBarContributor>>() {
		private static final long serialVersionUID = 1L;
	};

	static {
		registerStandardContributors();
	}

	private static final long serialVersionUID = 1L;

	public WicketDebugBar(String id) {
		super(id);
		add(CSSPackageResource.getHeaderContribution(WicketDebugBar.class,
				"wicket-debugbar.css"));
		add(JavascriptPackageResource.getHeaderContribution(
				WicketDebugBar.class, "wicket-debugbar.js"));
		add(new Image("logo", new ResourceReference(WicketDebugBar.class,
				"wicket.png")));
		add(new Image("removeImg", new ResourceReference(WicketDebugBar.class,
				"remove.png")));
		List<IDebugBarContributor> contributors = getContributors();
		if (contributors.isEmpty()) {
			// we do this so that if you have multiple applications running in the same container,
			//	each ends up registering its' own contributors (wicket-examples for example)
			registerStandardContributors();
			contributors = getContributors();
		}
		add(new ListView<IDebugBarContributor>("contributors", contributors) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<IDebugBarContributor> item) {
				item.add(item.getModelObject().createComponent("contrib",
						WicketDebugBar.this));
			}
		});
	}
	
	@Override
	public boolean isVisible() {
		return getApplication().getDebugSettings().isDevelopmentUtilitiesEnabled();
	}

	/**
	 * Register your own custom contributor that will be part of the debug bar.
	 * You must have the context of an application for this thread at the time
	 * of calling this method.
	 * 
	 * @param contrib
	 *            custom contributor - can not be null
	 */
	public static void registerContributor(IDebugBarContributor contrib) {
		if (contrib == null) {
			throw new IllegalArgumentException("contrib can not be null");
		}

		List<IDebugBarContributor> contributors = getContributors();
		contributors.add(contrib);
		Application.get().setMetaData(CONTRIBS_META_KEY, contributors);
	}

	private static List<IDebugBarContributor> getContributors() {
		List<IDebugBarContributor> list = Application.get().getMetaData(
				CONTRIBS_META_KEY);
		return list == null ? new ArrayList<IDebugBarContributor>() : list;
	}

	private static void registerStandardContributors() {
		registerContributor(VersionDebugContributor.DEBUG_BAR_CONTRIB);
		registerContributor(InspectorDebugPanel.DEBUG_BAR_CONTRIB);		
		registerContributor(SessionSizeDebugPanel.DEBUG_BAR_CONTRIB);		
	}
}
