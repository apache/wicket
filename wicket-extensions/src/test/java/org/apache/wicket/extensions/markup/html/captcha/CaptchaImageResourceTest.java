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
package org.apache.wicket.extensions.markup.html.captcha;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

/**
 * Tests the {@link org.apache.wicket.extensions.markup.html.captcha.CaptchaImageResource}.
 *
 * @author Michael Haitz
 */
public class CaptchaImageResourceTest
{
	@Test
	public void getterReturnsCorrectValue()
	{
		CaptchaImageResource image1 = new CaptchaImageResource("wicket");

		assertEquals("wicket", image1.getChallengeId());
		assertEquals("wicket", image1.getChallengeIdModel().getObject());
	}

	@Test
	public void sameChallengeIdHasDifferentImageContent()
	{
		CaptchaImageResource image1 = new CaptchaImageResource("wicket");
		CaptchaImageResource image2 = new CaptchaImageResource("wicket");

		assertNotEquals(image1.getImageData(null), image2.getImageData(null));
	}

	@Test
	public void imageDataIsCached()
	{
		CaptchaImageResource image1 = new CaptchaImageResource("wicket");

		assertSame(image1.getImageData(null), image1.getImageData(null));
	}

	@Test
	public void invalidateDropsImageDataCache()
	{
		CaptchaImageResource image1 = new CaptchaImageResource("wicket");
		byte[] originalImageData = image1.getImageData(null);
		image1.invalidate();

		assertNotEquals(originalImageData, image1.getImageData(null));
	}

	@Test
	public void defaultConstructorCreatesRandomChallengeId()
	{
		int idsNumber = 100000;
		Set<String> challengeIds = new HashSet<String>();

		for (int i = 0; i < idsNumber; ++i)
		{
			challengeIds.add(new CaptchaImageResource().getChallengeId());
		}

		assertEquals(idsNumber, challengeIds.size());
	}
}
