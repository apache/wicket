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
package org.apache.wicket.examples.media;

import org.apache.wicket.Page;
import org.apache.wicket.csp.CSPDirective;
import org.apache.wicket.csp.CSPDirectiveSrcValue;
import org.apache.wicket.examples.WicketExampleApplication;
import org.apache.wicket.markup.html.IPackageResourceGuard;
import org.apache.wicket.markup.html.SecurePackageResourceGuard;


/**
 * Application class for the videos examples.
 *
 * @author Tobias Soloschenko
 */
public class VideosApplication extends WicketExampleApplication
{
	@Override
	public Class<? extends Page> getHomePage()
	{
		return Home.class;
	}

	@Override
	protected void init()
	{
		super.init();
		
		IPackageResourceGuard packageResourceGuard = getResourceSettings().getPackageResourceGuard();
		if (packageResourceGuard instanceof SecurePackageResourceGuard)
		{
			SecurePackageResourceGuard guard = (SecurePackageResourceGuard)packageResourceGuard;
			guard.addPattern("+*.mp4");
		}
		
		getCsp().blocking()
			.add(CSPDirective.MEDIA_SRC, CSPDirectiveSrcValue.SELF)
			.add(CSPDirective.MEDIA_SRC, "https://w3c-test.org/media/movie_300.mp4");
	}

}
