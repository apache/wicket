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
package org.apache.wicket.util.image;

import java.io.IOException;
import java.io.InputStream;

import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.util.crypt.Base64;
import org.apache.wicket.util.io.IOUtils;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;

/**
 * Util class to provide basic image functionality like converting image data to base64 content
 *
 * @author Tobias Soloschenko
 * @since 6.20.0
 *
 */
public class ImageUtil
{

	/**
	 * Creates a base64 encoded image string based on the given image reference
	 *
	 * @param imageReference
	 *            the image reference to create the base64 encoded image string of
	 * @param removeWhitespaces
	 *            if whitespaces should be removed from the output
	 * @return the base64 encoded image string
	 * @throws ResourceStreamNotFoundException
	 *             if the resource couldn't be found
	 * @throws IOException
	 *             if the stream couldn't be read
	 */
	public static CharSequence createBase64EncodedImage(PackageResourceReference imageReference,
		boolean removeWhitespaces) throws ResourceStreamNotFoundException, IOException
	{
		IResourceStream resourceStream = imageReference.getResource().getCacheableResourceStream();
		InputStream inputStream = resourceStream.getInputStream();
		try
		{
			byte[] bytes = IOUtils.toByteArray(inputStream);
			String base64EncodedImage = Base64.encodeBase64String(bytes);
			return "data:" + resourceStream.getContentType() + ";base64," +
				(removeWhitespaces ? base64EncodedImage.replaceAll("\\s", "") : base64EncodedImage);
		}
		finally
		{
			IOUtils.closeQuietly(inputStream);
		}
	}
}
