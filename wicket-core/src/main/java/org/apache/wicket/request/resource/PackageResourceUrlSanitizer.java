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
package org.apache.wicket.request.resource;

import static org.apache.wicket.request.resource.PackageResource.getResourceStream;

import java.io.IOException;

import org.apache.wicket.request.resource.ResourceReference.UrlAttributes;
import org.apache.wicket.util.resource.IResourceStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sanitizes the URL based on existing package resource style/locale/variations
 * 
 * @author Pedro Santos
 */
public class PackageResourceUrlSanitizer implements IResourceUrlSanitizer
{
	private static final Logger log = LoggerFactory.getLogger(PackageResourceUrlSanitizer.class);

	/**
	 * @return UrlAttributes with an existent locale/style/variation if a resource is bound to the
	 *         scope+name, otherwise returns null
	 */
	@Override
	public UrlAttributes sanitize(UrlAttributes urlAttributes, Class<?> scope, String name)
	{
		IResourceStream filesystemMatch = getResourceStream(scope, name, urlAttributes.getLocale(),
			urlAttributes.getStyle(), urlAttributes.getVariation(), false);
		if (filesystemMatch == null)
		{
			return null;
		}
		try
		{
			filesystemMatch.close();
		}
		catch (IOException e)
		{
			log.error("failed to close", e);
		}
		return new ResourceReference.UrlAttributes(filesystemMatch.getLocale(),
			filesystemMatch.getStyle(), filesystemMatch.getVariation());
	}

}
