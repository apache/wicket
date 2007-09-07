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
package org.apache.wicket.examples.staticpages;

import org.apache.wicket.IRequestTarget;
import org.apache.wicket.request.RequestParameters;
import org.apache.wicket.request.target.coding.BookmarkablePageRequestTargetUrlCodingStrategy;
import org.apache.wicket.response.StringResponse;

/**
 * @author jbq
 */
public class CapturingBookmarkablePageRequestTargetUrlCodingStrategy
		extends
			BookmarkablePageRequestTargetUrlCodingStrategy
{
	Class capturedPageClass;
	Class displayedPageClass;
	/**
	 * Construct.
	 * 
	 * @param mountPath
	 * @param bookmarkablePageClass
	 * @param pageMapName
	 */
	public CapturingBookmarkablePageRequestTargetUrlCodingStrategy(String mountPath,
			Class capturedPageClass, Class displayedPageClass)
	{
		super(mountPath, capturedPageClass, null);
		this.displayedPageClass = displayedPageClass;
		this.capturedPageClass = capturedPageClass;
	}

	@Override
	public IRequestTarget decode(RequestParameters requestParameters)
	{
		return new CapturingBookmarkablePageRequestTarget(capturedPageClass, displayedPageClass)
		{
			/**
			 * @see org.apache.wicket.examples.staticpages.CapturingBookmarkablePageRequestTarget#onCapture(org.apache.wicket.response.StringResponse)
			 */
			@Override
			protected void onCapture(StringResponse emailResponse)
			{
				// Here send the email instead of dumping it to stdout!
				System.out.println(emailResponse.toString());
			}
		};
	}

}
