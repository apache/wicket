/*
 * $Id: org.eclipse.jdt.ui.prefs 5004 2006-03-17 20:47:08 -0800 (Fri, 17 Mar
 * 2006) eelco12 $ $Revision: 5004 $ $Date: 2006-03-17 20:47:08 -0800 (Fri, 17
 * Mar 2006) $
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
package wicket.markup.html.image;

import wicket.Resource;
import wicket.ResourceReference;
import wicket.markup.ComponentTag;
import wicket.model.IModel;
import wicket.util.value.ValueMap;

/**
 * A subclass of {@link Image} that adds random noise to the url every request
 * to prevent the browser from caching the image.
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public class NonCachingImage extends Image
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 * 
	 * @see Image#Image(String, IModel)
	 * 
	 * @param id
	 * @param model
	 */
	public NonCachingImage(String id, IModel model)
	{
		super(id, model);
	}

	/**
	 * Construct.
	 * 
	 * @see Image#Image(String, Resource)
	 * 
	 * @param id
	 * @param imageResource
	 */
	public NonCachingImage(String id, Resource imageResource)
	{
		super(id, imageResource);
	}

	/**
	 * Construct.
	 * 
	 * @see Image#Image(String, ResourceReference, ValueMap)
	 * 
	 * @param id
	 * @param resourceReference
	 * @param resourceParameters
	 */
	public NonCachingImage(String id, ResourceReference resourceReference,
			ValueMap resourceParameters)
	{
		super(id, resourceReference, resourceParameters);
	}

	/**
	 * Construct.
	 * 
	 * @see Image#Image(String, ResourceReference)
	 * 
	 * @param id
	 * @param resourceReference
	 */
	public NonCachingImage(String id, ResourceReference resourceReference)
	{
		super(id, resourceReference);
	}

	/**
	 * Construct.
	 * 
	 * @see Image#Image(String, String)
	 * 
	 * 
	 * @param id
	 * @param string
	 */
	public NonCachingImage(String id, String string)
	{
		super(id, string);
	}

	/**
	 * Construct.
	 * 
	 * @see Image#Image(String)
	 * 
	 * @param id
	 */
	public NonCachingImage(String id)
	{
		super(id);
	}

	/**
	 * @see wicket.markup.html.image.Image#onComponentTag(wicket.markup.ComponentTag)
	 */
	protected void onComponentTag(ComponentTag tag)
	{
		super.onComponentTag(tag);

		String url = tag.getAttributes().getString("src");
		url = url + ((url.indexOf("?") >= 0) ? "&" : "?");
		url = url + "wicket:antiCache=" + System.currentTimeMillis();
		
		tag.put("src", url);
	}

}
