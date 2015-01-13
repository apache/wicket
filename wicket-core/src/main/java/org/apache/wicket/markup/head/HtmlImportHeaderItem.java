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
package org.apache.wicket.markup.head;

import org.apache.wicket.Page;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 * Header item class for HTML 5 imports.
 * 
 * @author Tobias Soloschenko
 * @author Andrea Del Bene
 * @since 6.19.0
 */
public class HtmlImportHeaderItem extends MetaDataHeaderItem
{

	private HtmlImportHeaderItem()
	{
		super(LINK_TAG);
	}

	/**
	 * Factory method to create &lt;link&gt; tag.
	 *
	 * @param rel
	 *            the 'rel' attribute of the tag
	 * @param href
	 *            the 'href' attribute of the tag
	 * @param media
	 *            the 'media' attribute of the tag
	 * @return A new {@link MetaDataHeaderItem}
	 */
	public static MetaDataHeaderItem forLinkTag(String rel, String href, String media)
	{
		return forLinkTag(Model.of(rel), Model.of(href), Model.of(media));
	}

	/**
	 * Factory method to create &lt;link&gt; tag.
	 *
	 * @param rel
	 *            the 'rel' attribute of the tag as String model
	 * @param href
	 *            the 'href' attribute of the tag as String model
	 * @param media
	 *            the 'media' attribute of the tag as String model
	 * @return A new {@link MetaDataHeaderItem}
	 */
	public static MetaDataHeaderItem forLinkTag(IModel<String> rel, IModel<String> href,
		IModel<String> media)
	{
		MetaDataHeaderItem headerItem = forLinkTag(rel, href);
		headerItem.addTagAttribute("media", media);
		return headerItem;
	}
	
	/**
	 * Factory method to create &lt;link&gt; tag.
	 * 
	 * @param href
	 * 			 the 'href' attribute of the tag as String
	 * @return A new {@link MetaDataHeaderItem}
	 */
	public static MetaDataHeaderItem forImportLinkTag(String href)
	{
		return forImportLinkTag(Model.of(href), false);
	}
	
	/**
	 * Factory method to create &lt;link&gt; tag.
	 * 
	 * @param href
	 * 			 the 'href' attribute of the tag as String model
	 * @return A new {@link MetaDataHeaderItem}
	 */
	public static MetaDataHeaderItem forImportLinkTag(IModel<String> href)
	{
		return forImportLinkTag(href, false);
	}
	
	/**
	 * Factory method to create &lt;link&gt; tag.
	 * 
	 * @param href
	 * 			 the 'href' attribute of the tag as String
	 * @param async
	 * 			 the 'async' attribute as boolean value
	 * @return A new {@link MetaDataHeaderItem}
	 */
	public static MetaDataHeaderItem forImportLinkTag(String href, boolean async)
	{
		return forImportLinkTag(Model.of(href), async);
	}
	
	/**
	 * Factory method to create &lt;link&gt; tag.
	 * 
	 * @param href
	 * 			 the 'href' attribute of the tag as String model
	 * @param async
	 * 			 the 'async' attribute as boolean value
	 * @return A new {@link MetaDataHeaderItem}
	 */
	public static MetaDataHeaderItem forImportLinkTag(IModel<String> href, boolean async)
	{
		MetaDataHeaderItem linkTag = forLinkTag(Model.of("import"), href);
		addAsyncAttribute(linkTag, async);
		
		return linkTag;
	}

	/**
	 * Adds 'async' attribute if boolean parameter is true.
	 * 
	 * @param linkTag
	 * 			  the current {@link MetaDataHeaderItem}
	 * @param async
	 * 			  tells if we must add the attribute (true) or not (false)
	 */
	private static void addAsyncAttribute(MetaDataHeaderItem linkTag, boolean async)
	{
		if(async)
		{
			linkTag.addTagAttribute("async");
		}
	}
	
	/**
	 * Factory method to create lt;link&gt; tag for html import.
	 *
	 * @param pageClass
	 *            the page class to generate the import for
	 * @return A new {@link MetaDataHeaderItem}
	 */
	public static MetaDataHeaderItem forImportLinkTag(Class<? extends Page> pageClass)
	{
		return forImportLinkTag(Model.of(RequestCycle.get().urlFor(pageClass, null).toString()));
	}

	/**
	 * Factory method to create lt;link&gt; tag for html import.
	 *
	 * @param pageClass
	 *            the page class to generate the import for
	 * @param pageParameters
	 *            the page parameters to apply to the import
	 * @return A new {@link MetaDataHeaderItem}
	 */
	public static MetaDataHeaderItem forImportLinkTag(Class<? extends Page> pageClass,
		PageParameters pageParameters)
	{
		return forImportLinkTag(Model.of(RequestCycle.get().urlFor(pageClass, 
			pageParameters).toString()));
	}
	
	/**
	 * Factory method to create lt;link&gt; tag for html import.
	 *
	 * @param pageClass
	 *            the page class to generate the import for
	 * @param pageParameters
	 *            the page parameters to apply to the import
	 * @param async
	 * 			  the 'async' attribute as boolean value
	 * @return A new {@link MetaDataHeaderItem}
	 */
	public static MetaDataHeaderItem forImportLinkTag(Class<? extends Page> pageClass,
		PageParameters pageParameters, boolean async)
	{
		MetaDataHeaderItem linkTag = forImportLinkTag(Model.of(RequestCycle.get().urlFor(pageClass, 
			pageParameters).toString()));
		
		addAsyncAttribute(linkTag, async);
		
		return linkTag;
	}
	
	/**
	 * Factory method to create lt;link&gt; tag for html import.
	 *
	 * @param pageClass
	 *            the page class to generate the import for
	 * @param pageParameters
	 *            the page parameters to apply to the import
	 * @param media
	 *            the 'media' attribute of the tag
	 * @return A new {@link MetaDataHeaderItem}
	 */
	public static MetaDataHeaderItem forImportLinkTag(Class<? extends Page> pageClass,
		PageParameters pageParameters, String media)
	{
		return forImportLinkTag(pageClass, pageParameters, Model.of(media));
	}
	
	/**
	 * Factory method to create lt;link&gt; tag for html import.
	 *
	 * @param pageClass
	 *            the page class to generate the import for
	 * @param pageParameters
	 *            the page parameters to apply to the import
	 * @param media
	 *            the 'media' attribute of the tag
	 * @param async
	 * 			  the 'async' attribute as boolean value
	 * @return A new {@link MetaDataHeaderItem}
	 */
	public static MetaDataHeaderItem forImportLinkTag(Class<? extends Page> pageClass,
		PageParameters pageParameters, String media, boolean async)
	{
		return forImportLinkTag(pageClass, pageParameters, Model.of(media), async);
	}

	/**
	 * Factory method to create lt;link&gt; tag for html import.
	 *
	 * @param pageClass
	 *            the page class to generate the import for
	 * @param pageParameters
	 *            the page parameters to apply to the import
	 * @param media
	 *            the 'media' attribute of the tag as String model
	 * @return A new {@link MetaDataHeaderItem}
	 */
	public static MetaDataHeaderItem forImportLinkTag(Class<? extends Page> pageClass,
		PageParameters pageParameters, IModel<String> media)
	{
		return forLinkTag(Model.of("import"),
			Model.of(RequestCycle.get().urlFor(pageClass, pageParameters).toString()), media);
	}
	
	/**
	 * Factory method to create lt;link&gt; tag for html import.
	 *
	 * @param pageClass
	 *            the page class to generate the import for
	 * @param pageParameters
	 *            the page parameters to apply to the import
	 * @param media
	 *            the 'media' attribute of the tag as String model
	 * @param async
	 * 			  the 'async' attribute as boolean value
	 * @return A new {@link MetaDataHeaderItem}
	 */
	public static MetaDataHeaderItem forImportLinkTag(Class<? extends Page> pageClass,
		PageParameters pageParameters, IModel<String> media, boolean async)
	{
		MetaDataHeaderItem linkTag = forLinkTag(Model.of("import"),
			Model.of(RequestCycle.get().urlFor(pageClass, pageParameters).toString()), media);
		
		addAsyncAttribute(linkTag, async);
		
		return linkTag;
	}
}
