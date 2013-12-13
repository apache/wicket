package org.apache.wicket.extensions.markup.html.captcha;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

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

		assertThat(image1.getChallengeId(), is("wicket"));
		assertThat(image1.getChallengeIdModel().getObject(), is("wicket"));
	}

	@Test
	public void sameChallengeIdHasDifferentImageContent()
	{
		CaptchaImageResource image1 = new CaptchaImageResource("wicket");
		CaptchaImageResource image2 = new CaptchaImageResource("wicket");

		assertThat(image1.getImageData(null), is(not(image2.getImageData(null))));
	}

	@Test
	public void imageDataIsCached()
	{
		CaptchaImageResource image1 = new CaptchaImageResource("wicket");

		assertThat(image1.getImageData(null), is(image1.getImageData(null)));
	}

	@Test
	public void invalidateDropsImageDataCache()
	{
		CaptchaImageResource image1 = new CaptchaImageResource("wicket");
		byte[] originalImageData = image1.getImageData(null);
		image1.invalidate();

		assertThat(originalImageData, is(not(image1.getImageData(null))));
	}

	@Test
	public void defaultConstructorCreatesRandomChallengeId()
	{
		Set<String> challengeIds = new HashSet<String>();

		for (int i = 0; i < 100000; ++i)
		{
			challengeIds.add(new CaptchaImageResource().getChallengeId());
		}

		assertThat(challengeIds.size(), is(100000));
	}
}