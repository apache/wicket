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
package org.apache.wicket.markup.html.media;

import org.apache.wicket.Application;
import org.apache.wicket.markup.html.IPackageResourceGuard;
import org.apache.wicket.markup.html.SecurePackageResourceGuard;

/**
 * Helper method to provide access to basic media files like subtitles
 * 
 * @author Tobias Soloschenko
 * 
 */
public class MediaUtils
{

	/**
	 * Method that has to be called within the init method of the web application to make
	 * translation files accessible
	 */
	public static void init()
	{
		IPackageResourceGuard packageResourceGuard = Application.get()
			.getResourceSettings()
			.getPackageResourceGuard();
		if (packageResourceGuard instanceof SecurePackageResourceGuard)
		{
			SecurePackageResourceGuard securePackageResourceGuard = (SecurePackageResourceGuard)packageResourceGuard;
			securePackageResourceGuard.addPattern("+*.vtt");
			securePackageResourceGuard.addPattern("+*.srt");
		}
	}

}
