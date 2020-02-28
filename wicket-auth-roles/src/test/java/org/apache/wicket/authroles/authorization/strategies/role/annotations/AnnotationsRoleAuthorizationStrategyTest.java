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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.wicket.Component;
import org.apache.wicket.authroles.authorization.strategies.role.IRoleCheckingStrategy;
import org.apache.wicket.authroles.authorization.strategies.role.Roles;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.request.resource.IResource;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Tests for {@link AnnotationsRoleAuthorizationStrategy}
 */
class AnnotationsRoleAuthorizationStrategyTest
{
	/**
	 * https://issues.apache.org/jira/browse/WICKET-3974
	 */
	@Test
    void allowsRenderWithRequiredRoleAndNoDeniedRole()
	{
		AnnotationsRoleAuthorizationStrategy strategy = new AnnotationsRoleAuthorizationStrategy(
			roles("role1"));
		// use mock to not need Application in the thread
		TestComponent_Render component = Mockito.mock(TestComponent_Render.class);
		assertTrue(strategy.isActionAuthorized(component, Component.RENDER));
	}

	@Test
    void deniesRenderWithoutRequiredRole()
	{
		AnnotationsRoleAuthorizationStrategy strategy = new AnnotationsRoleAuthorizationStrategy(
			roles("role2"));
		TestComponent_Render component = Mockito.mock(TestComponent_Render.class);
		assertFalse(strategy.isActionAuthorized(component, Component.RENDER));
	}

	@Test
    void deniesRenderWithRequiredRoleAndDeniedRole()
	{
		AnnotationsRoleAuthorizationStrategy strategy = new AnnotationsRoleAuthorizationStrategy(
			roles("role1", "role3"));
		TestComponent_Render component = Mockito.mock(TestComponent_Render.class);
		assertFalse(strategy.isActionAuthorized(component, Component.RENDER));
	}

	@Test
    void deniesRenderWithDeniedRole()
	{
		AnnotationsRoleAuthorizationStrategy strategy = new AnnotationsRoleAuthorizationStrategy(
			roles("role3"));
		TestComponent_Render component = Mockito.mock(TestComponent_Render.class);
		assertFalse(strategy.isActionAuthorized(component, Component.RENDER));
	}

	@Test
    void allowsEnableWithRequiredRole()
	{
		AnnotationsRoleAuthorizationStrategy strategy = new AnnotationsRoleAuthorizationStrategy(
			roles("role1"));
		TestComponent_Enable component = Mockito.mock(TestComponent_Enable.class);
		assertTrue(strategy.isActionAuthorized(component, Component.ENABLE));
	}

	@Test
    void deniesEnableWithoutRequiredRoleAndNoDeniedRole()
	{
		AnnotationsRoleAuthorizationStrategy strategy = new AnnotationsRoleAuthorizationStrategy(
			roles("role2"));
		TestComponent_Enable component = Mockito.mock(TestComponent_Enable.class);
		assertFalse(strategy.isActionAuthorized(component, Component.ENABLE));
	}

	@Test
    void deniesEnableWithDeniedRole()
	{
		AnnotationsRoleAuthorizationStrategy strategy = new AnnotationsRoleAuthorizationStrategy(
			roles("role3"));
		TestComponent_Enable component = Mockito.mock(TestComponent_Enable.class);
		assertFalse(strategy.isActionAuthorized(component, Component.ENABLE));
	}

	@Test
    void deniesEnableWithRequiredRoleAndDeniedRole()
	{
		AnnotationsRoleAuthorizationStrategy strategy = new AnnotationsRoleAuthorizationStrategy(
			roles("role1", "role3"));
		TestComponent_Enable component = Mockito.mock(TestComponent_Enable.class);
		assertFalse(strategy.isActionAuthorized(component, Component.ENABLE));
	}

	@Test
    void allowsInstantiationWithRequiredRole() {
		AnnotationsRoleAuthorizationStrategy strategy = new AnnotationsRoleAuthorizationStrategy(
			roles("role1"));
		assertTrue(strategy.isInstantiationAuthorized(TestComponent_Instantiate.class));
	}

	@Test
    void deniesInstantiationWithoutRequiredRole() {
		AnnotationsRoleAuthorizationStrategy strategy = new AnnotationsRoleAuthorizationStrategy(
			roles("role2"));
		assertFalse(strategy.isInstantiationAuthorized(TestComponent_Instantiate.class));
	}

	@Test
    void allowsInstantiationWithAllRequiredRoles() {
		AnnotationsRoleAuthorizationStrategy strategy = new AnnotationsRoleAuthorizationStrategy(
			roles("role1", "role2"));
		assertTrue(strategy.isInstantiationAuthorized(TestComponent_Roleset_Instantiate.class));
	}

	@Test
    void deniesInstantiationWithoutAllRequiredRoles() {
		AnnotationsRoleAuthorizationStrategy strategy = new AnnotationsRoleAuthorizationStrategy(
			roles("role2"));
		assertFalse(strategy.isInstantiationAuthorized(TestComponent_Roleset_Instantiate.class));
	}

	@Test
    void allowsResourceWithRequiredRole() {
		AnnotationsRoleAuthorizationStrategy strategy = new AnnotationsRoleAuthorizationStrategy(
			roles("role1"));
		IResource resource = Mockito.mock(RestrictedResource.class);
		assertTrue(strategy.isResourceAuthorized(resource, null));
	}

	@Test
    void deniesResourceWithoutRequiredRole() {
		AnnotationsRoleAuthorizationStrategy strategy = new AnnotationsRoleAuthorizationStrategy(
			roles("role2"));
		IResource resource = Mockito.mock(RestrictedResource.class);
		assertFalse(strategy.isResourceAuthorized(resource, null));
	}

	@Test
    void allowsUnprotectedResourceWithRole() {
		AnnotationsRoleAuthorizationStrategy strategy = new AnnotationsRoleAuthorizationStrategy(
			roles("role2"));
		IResource resource = Mockito.mock(UnrestrictedResource.class);
		assertTrue(strategy.isResourceAuthorized(resource, null));
	}

	@Test
    void allowsUnprotectedResourceWithoutRole() {
		AnnotationsRoleAuthorizationStrategy strategy = new AnnotationsRoleAuthorizationStrategy(
			roles());
		IResource resource = Mockito.mock(UnrestrictedResource.class);
		assertTrue(strategy.isResourceAuthorized(resource, null));
	}

	/**
	 * Create a test role checking strategy that is simply given a list of roles and returns true if
	 * that list contains any of the asked-for roles.
	 *
	 * @param availableRoles
	 *            rules that this role checker should have
	 * @return test role checking strategy
	 */
	private IRoleCheckingStrategy roles(final String... availableRoles)
	{
		return requiredRoles -> requiredRoles.hasAnyRole(new Roles(availableRoles));
	}

	@AuthorizeInstantiation({ "role1" })
	private static class TestComponent_Instantiate extends WebComponent
	{

		private static final long serialVersionUID = 1L;

		private TestComponent_Instantiate()
		{
			super("notUsed");
		}

	}

	@AuthorizeInstantiations(ruleset = { @AuthorizeInstantiation({ "role1" }),
			@AuthorizeInstantiation({ "role2" }) })
	private static class TestComponent_Roleset_Instantiate extends WebComponent
	{

		private static final long serialVersionUID = 1L;

		private TestComponent_Roleset_Instantiate()
		{
			super("notUsed");
		}

	}

	@AuthorizeAction(action = "RENDER", roles = { "role1" }, deny = { "role3" })
	private static class TestComponent_Render extends WebComponent
	{

		private static final long serialVersionUID = 1L;

		private TestComponent_Render()
		{
			super("notUsed");
		}

	}

	@AuthorizeAction(action = "ENABLE", roles = { "role1" }, deny = { "role3" })
	private static class TestComponent_Enable extends WebComponent
	{
		private static final long serialVersionUID = 1L;

		private TestComponent_Enable()
		{
			super("notUsed");
		}

	}

	@AuthorizeResource("role1")
	private static class RestrictedResource implements IResource
	{
		/**
		 * Renders this resource to response using the provided attributes.
		 *
		 * @param attributes
		 */
		@Override
		public void respond(Attributes attributes)
		{
			; // NOOP
		}
	}

	private static class UnrestrictedResource implements IResource
	{
		@Override
		public void respond(Attributes attributes)
		{
			; // NOOP
		}
	}
}
