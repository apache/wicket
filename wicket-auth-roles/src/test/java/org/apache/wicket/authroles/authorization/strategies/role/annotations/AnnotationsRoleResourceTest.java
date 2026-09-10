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
package org.apache.wicket.authroles.authorization.strategies.role.annotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.Serializable;

import javax.servlet.http.HttpServletResponse;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.authroles.authorization.strategies.role.IRoleCheckingStrategy;
import org.apache.wicket.authroles.authorization.strategies.role.RoleAuthorizationStrategy;
import org.apache.wicket.authroles.authorization.strategies.role.Roles;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.ResourceLink;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.TagTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * A resource reserved for a role with {@link AuthorizeResource} must not be served to a user who
 * does not hold that role, not even through the component that was handed the resource itself.
 */
class AnnotationsRoleResourceTest
{
	private WicketTester tester;

	@BeforeEach
	void setUp()
	{
		tester = new WicketTester();
	}

	@AfterEach
	void tearDown()
	{
		tester.destroy();
	}

	private void signInAs(String role)
	{
		tester.getApplication()
			.getSecuritySettings()
			.setAuthorizationStrategy(new RoleAuthorizationStrategy(new UserRolesAuthorizer(role)));
	}

	/**
	 * The listener URL Wicket itself rendered for the component with the given wicket:id.
	 */
	private String urlInMarkup(String wicketId, String attribute)
	{
		TagTester tag = tester.getTagByWicketId(wicketId);
		assertNotNull(tag, "no tag with wicket:id '" + wicketId + "' was rendered");

		String url = tag.getAttribute(attribute);
		assertNotNull(url, "the tag with wicket:id '" + wicketId + "' has no " + attribute);

		return url;
	}

	@Test
	void adminMayDownloadTheAdminResourceThroughAResourceLink()
	{
		signInAs("ADMIN");
		TestPage page = new TestPage();
		tester.startPage(page);

		tester.clickLink("admin", false);

		assertEquals(HttpServletResponse.SC_OK, tester.getLastResponse().getStatus());
		assertTrue(page.admin.responded, "the resource did not stream for an ADMIN");
	}

	@Test
	void userMayNotDownloadTheAdminResourceThroughAResourceLink()
	{
		signInAs("USER");
		TestPage page = new TestPage();
		tester.startPage(page);

		tester.clickLink("admin", false);

		assertEquals(HttpServletResponse.SC_FORBIDDEN, tester.getLastResponse().getStatus(),
			"the request should have been refused");
		assertFalse(page.admin.responded,
			"the resource streamed to a user who does not hold the ADMIN role");
	}

	@Test
	void userMayNotDownloadTheAdminResourceThroughAnImage()
	{
		signInAs("USER");
		TestPage page = new TestPage();
		tester.startPage(page);

		tester.executeUrl(urlInMarkup("image", "src"));

		assertEquals(HttpServletResponse.SC_FORBIDDEN, tester.getLastResponse().getStatus(),
			"the request should have been refused");
		assertFalse(page.image.responded,
			"the resource streamed to a user who does not hold the ADMIN role");
	}

	/**
	 * Control: a resource that carries no annotation is not restricted.
	 */
	@Test
	void userMayStillDownloadAnUnannotatedResource()
	{
		signInAs("USER");
		TestPage page = new TestPage();
		tester.startPage(page);

		tester.clickLink("open", false);

		assertEquals(HttpServletResponse.SC_OK, tester.getLastResponse().getStatus());
		assertTrue(page.open.responded, "an unannotated resource must still be served");
	}

	/** A page anyone may open, carrying resources reserved for ADMIN. */
	public static class TestPage extends WebPage implements IMarkupResourceStreamProvider
	{
		private static final long serialVersionUID = 1L;

		final AdminResource admin = new AdminResource();

		final AdminResource image = new AdminResource();

		final OpenResource open = new OpenResource();

		TestPage()
		{
			setStatelessHint(false);

			add(new ResourceLink<Void>("admin", admin));
			add(new ResourceLink<Void>("open", open));
			add(new Image("image", image));
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream("<html><body>" //
				+ "<a wicket:id=\"admin\">admin</a>" //
				+ "<a wicket:id=\"open\">open</a>" //
				+ "<img wicket:id=\"image\"/>" //
				+ "</body></html>");
		}
	}

	/** Records that it was asked to respond. The annotation cannot go on an anonymous class. */
	private static class RecordingResource implements IResource
	{
		private static final long serialVersionUID = 1L;

		boolean responded;

		@Override
		public void respond(Attributes attributes)
		{
			responded = true;
			attributes.getResponse().write("streamed");
		}
	}

	@AuthorizeResource("ADMIN")
	private static class AdminResource extends RecordingResource
	{
		private static final long serialVersionUID = 1L;
	}

	private static class OpenResource extends RecordingResource
	{
		private static final long serialVersionUID = 1L;
	}

	private static final class UserRolesAuthorizer implements IRoleCheckingStrategy, Serializable
	{
		private static final long serialVersionUID = 1L;

		private final Roles roles;

		UserRolesAuthorizer(String roles)
		{
			this.roles = new Roles(roles);
		}

		@Override
		public boolean hasAnyRole(Roles roles)
		{
			return this.roles.hasAnyRole(roles);
		}
	}
}
