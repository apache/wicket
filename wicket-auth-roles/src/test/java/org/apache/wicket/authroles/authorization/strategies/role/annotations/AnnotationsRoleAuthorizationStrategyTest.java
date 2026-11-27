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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

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
	@Test
	void checksInstantiationMetaAnnotationAlongsideDirectRule()
	{
		assertTrue(new AnnotationsRoleAuthorizationStrategy(roles("role1", "role2"))
			.isInstantiationAuthorized(ComposedComponent.class));
		assertFalse(new AnnotationsRoleAuthorizationStrategy(roles("role1"))
			.isInstantiationAuthorized(ComposedComponent.class));
		assertFalse(new AnnotationsRoleAuthorizationStrategy(roles("role2"))
			.isInstantiationAuthorized(ComposedComponent.class));
	}

	@Test
	void checksComposedActionOnlyForTheNamedAction()
	{
		assertTrue(new AnnotationsRoleAuthorizationStrategy(roles("role2"))
			.isActionAuthorized(ComposedComponent.class, Component.RENDER));
		final AnnotationsRoleAuthorizationStrategy strategy =
			new AnnotationsRoleAuthorizationStrategy(roles("role1"));
		assertFalse(strategy.isActionAuthorized(ComposedComponent.class, Component.RENDER));
		assertTrue(strategy.isActionAuthorized(ComposedComponent.class, Component.ENABLE));
	}

	@Test
	void checksInstantiationRuleThroughTwoCustomAnnotations()
	{
		assertTrue(new AnnotationsRoleAuthorizationStrategy(roles("role2"))
			.isInstantiationAuthorized(NestedComponent.class));
		assertFalse(new AnnotationsRoleAuthorizationStrategy(roles("role1"))
			.isInstantiationAuthorized(NestedComponent.class));
	}

	@Test
	void checksInstantiationRulesAtBothMetaAnnotationLevels()
	{
		assertTrue(new AnnotationsRoleAuthorizationStrategy(roles("role1", "role2"))
			.isInstantiationAuthorized(TwoLevelComponent.class));
		assertFalse(new AnnotationsRoleAuthorizationStrategy(roles("role1"))
			.isInstantiationAuthorized(TwoLevelComponent.class));
		assertFalse(new AnnotationsRoleAuthorizationStrategy(roles("role2"))
			.isInstantiationAuthorized(TwoLevelComponent.class));
	}

	@Test
	void checksDeepInstantiationRulesDespiteAnOuterRuleAndAnAnnotationCycle()
	{
		assertTrue(new AnnotationsRoleAuthorizationStrategy(roles("role1", "role2", "role3"))
			.isInstantiationAuthorized(DeeplyComposedComponent.class));
		assertFalse(new AnnotationsRoleAuthorizationStrategy(roles("role1", "role3"))
			.isInstantiationAuthorized(DeeplyComposedComponent.class));
		assertFalse(new AnnotationsRoleAuthorizationStrategy(roles("role1", "role2"))
			.isInstantiationAuthorized(DeeplyComposedComponent.class));
	}

	@Test
	void checksInstantiationRulesAtTheFirstAndEighthMetaAnnotationLevels()
	{
		assertTrue(new AnnotationsRoleAuthorizationStrategy(roles("role1", "role2"))
			.isInstantiationAuthorized(EightLevelComponent.class));
		assertFalse(new AnnotationsRoleAuthorizationStrategy(roles("role1"))
			.isInstantiationAuthorized(EightLevelComponent.class));
		assertFalse(new AnnotationsRoleAuthorizationStrategy(roles("role2"))
			.isInstantiationAuthorized(EightLevelComponent.class));
	}

	@Test
	void checksDirectInstantiationRuleAlongsideFirstAndEighthLevelRules()
	{
		assertTrue(new AnnotationsRoleAuthorizationStrategy(roles("role1", "role2", "role3"))
			.isInstantiationAuthorized(DirectAndEightLevelComponent.class));
		assertFalse(new AnnotationsRoleAuthorizationStrategy(roles("role1", "role2"))
			.isInstantiationAuthorized(DirectAndEightLevelComponent.class));
		assertFalse(new AnnotationsRoleAuthorizationStrategy(roles("role1", "role3"))
			.isInstantiationAuthorized(DirectAndEightLevelComponent.class));
		assertFalse(new AnnotationsRoleAuthorizationStrategy(roles("role2", "role3"))
			.isInstantiationAuthorized(DirectAndEightLevelComponent.class));
	}

	@Test
	void checksResourceMetaAnnotation()
	{
		IResource resource = new ComposedResource();
		assertTrue(new AnnotationsRoleAuthorizationStrategy(roles("role2"))
			.isResourceAuthorized(resource, null));
		assertFalse(new AnnotationsRoleAuthorizationStrategy(roles("role1"))
			.isResourceAuthorized(resource, null));
	}

	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	@AuthorizeInstantiations(ruleset = { @AuthorizeInstantiation("role2") })
	@AuthorizeActions(actions = { @AuthorizeAction(action = "RENDER", roles = "role2") })
	@AuthorizeResource("role2")
	private @interface ComposedRule
	{
	}

	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	@ComposedRule
	private @interface NestedRule
	{
	}

	@NestedRule
	private static class NestedComponent extends WebComponent
	{
		private static final long serialVersionUID = 1L;

		private NestedComponent()
		{
			super("notUsed");
		}
	}

	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	@AuthorizeInstantiation("role1")
	@ComposedRule
	private @interface TwoLevelRule
	{
	}

	@TwoLevelRule
	private static class TwoLevelComponent extends WebComponent
	{
		private static final long serialVersionUID = 1L;

		private TwoLevelComponent()
		{
			super("notUsed");
		}
	}

	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	@AuthorizeInstantiation("role3")
	@TwoLevelRule
	@CyclicRule
	private @interface DeepRule
	{
	}

	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	@DeepRule
	private @interface CyclicRule
	{
	}

	@DeepRule
	private static class DeeplyComposedComponent extends WebComponent
	{
		private static final long serialVersionUID = 1L;

		private DeeplyComposedComponent()
		{
			super("notUsed");
		}
	}

	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	@AuthorizeInstantiation("role1")
	@Level2Rule
	private @interface Level1Rule
	{
	}

	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	@Level3Rule
	private @interface Level2Rule
	{
	}

	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	@Level4Rule
	private @interface Level3Rule
	{
	}

	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	@Level5Rule
	private @interface Level4Rule
	{
	}

	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	@Level6Rule
	private @interface Level5Rule
	{
	}

	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	@Level7Rule
	private @interface Level6Rule
	{
	}

	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	@ComposedRule
	private @interface Level7Rule
	{
	}

	@Level1Rule
	private static class EightLevelComponent extends WebComponent
	{
		private static final long serialVersionUID = 1L;

		private EightLevelComponent()
		{
			super("notUsed");
		}
	}

	@AuthorizeInstantiation("role3")
	@Level1Rule
	private static class DirectAndEightLevelComponent extends WebComponent
	{
		private static final long serialVersionUID = 1L;

		private DirectAndEightLevelComponent()
		{
			super("notUsed");
		}
	}

	@AuthorizeInstantiation("role1")
	@ComposedRule
	private static class ComposedComponent extends WebComponent
	{
		private static final long serialVersionUID = 1L;

		private ComposedComponent()
		{
			super("notUsed");
		}
	}

	@ComposedRule
	private static class ComposedResource extends UnrestrictedResource
	{
	}

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
