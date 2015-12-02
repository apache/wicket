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
package org.apache.wicket.markup.html.image;

import java.io.Serializable;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.tester.TagTester;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test cases to test the external image components
 * 
 * @author Tobias Soloschenko
 *
 */
public class ExternalImageTest extends WicketTestCase
{

	@Test
	public void testExternalImage()
	{
		tester.startPage(ExternalImageTestPage.class);
		String lastResponseAsString = tester.getLastResponse().getDocument();
		System.out.println(lastResponseAsString);
		Assert.assertTrue(lastResponseAsString.contains(
			"<img wicket:id=\"externalImage1\" src=\"http://wicket.apache.org/img/wicket-7-bg.jpg\"/>"));
		Assert.assertTrue(lastResponseAsString.contains(
			"<img id=\"externalImage2\" wicket:id=\"externalImage2\" src=\"http://wicket.apache.org/img/wicket-7-bg.jpg\" srcset=\"http://wicket.apache.org/img/wicket-7-bg-1.jpg x1, http://wicket.apache.org/img/wicket-7-bg-2.jpg x2\" sizes=\"s1,s2\"/>"));
		Assert.assertTrue(lastResponseAsString.contains(
			"<source wicket:id=\"externalSource\" srcset=\"http://wicket.apache.org/img/wicket-7-bg-1.jpg , http://wicket.apache.org/img/wicket-7-bg-2.jpg x2\" sizes=\"1\" media=\"(min-width: 650px)\"/>"));
	}

	@Test
	public void testExternalImageModel()
	{
		tester.startPage(ExternalImageTestPage.class);
		tester.getLastResponse().getDocument();
		Component externalImage2Component = tester
			.getComponentFromLastRenderedPage("externalImage2");
		ExternalImage externalImage2 = (ExternalImage)externalImage2Component;

		TagTester tagById = tester.getTagById("externalImage2");
		 IModel<List<Serializable>> srcSet = externalImage2.getSrcSetModel();
		for (Serializable model :srcSet.getObject())
		{
			String attribute = tagById.getAttribute("srcset");
			Assert.assertTrue(attribute.contains(model.toString()));
		}

		String attribute = tagById.getAttribute("src");
		Assert.assertEquals(externalImage2.getDefaultModelObject(), attribute);
	}
}
