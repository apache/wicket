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
import org.apache.wicket.authroles.authorization.strategies.role.annotations.pkg.EnableOnlyComponent;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.pkg.InheritingActionComponent;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.pkg.InheritingComponent;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.pkg.OptedOutComponent;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.pkg.OverridingComponent;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.pkg.OverridingResource;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.pkg.PackageProtectedResource;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.pkg.PlainComponent;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.pkg.RulesetComponent;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.pkg.sub.NestedComponent;
import org.junit.jupiter.api.Test;

/**
 * Tests how {@link AnnotationsRoleAuthorizationStrategy} resolves annotations on a package against
 * annotations on a class. The fixtures are real classes in real packages rather than mocks, because a
 * mock does not live in the package of the class it mocks.
 * <p>
 * Package <code>pkg</code> restricts instantiation to <code>role1</code>, RENDER to
 * <code>role1</code> and resources to <code>role1</code>; package <code>base</code> and package
 * <code>pkg.sub</code> carry no annotations at all.
 */
class AnnotationsRolePackageTest
{
	@Test
	void packageAnnotationAppliesToClassWithoutOneOfItsOwn()
	{
		assertTrue(strategy("role1").isInstantiationAuthorized(PlainComponent.class));
		assertFalse(strategy("role2").isInstantiationAuthorized(PlainComponent.class));
	}

	@Test
	void classAnnotationReplacesPackageAnnotation()
	{
		assertTrue(strategy("role2").isInstantiationAuthorized(OverridingComponent.class));
		assertFalse(strategy("role1").isInstantiationAuthorized(OverridingComponent.class));
	}

	@Test
	void classAnnotationWithoutRolesExemptsFromPackageAnnotation()
	{
		assertTrue(strategy().isInstantiationAuthorized(OptedOutComponent.class));
	}

	@Test
	void classRulesetReplacesPackageAnnotation()
	{
		assertTrue(strategy("role2", "role3").isInstantiationAuthorized(RulesetComponent.class));
		assertFalse(strategy("role2").isInstantiationAuthorized(RulesetComponent.class));
		assertFalse(strategy("role1").isInstantiationAuthorized(RulesetComponent.class));
	}

	/**
	 * The annotations are {@link java.lang.annotation.Inherited}, so an annotation on a superclass in
	 * another package counts as an annotation on the class and suppresses the annotation of the
	 * class' own package.
	 */
	@Test
	void inheritedClassAnnotationSuppressesPackageAnnotation()
	{
		assertTrue(strategy("role2").isInstantiationAuthorized(InheritingComponent.class));
		assertFalse(strategy("role1").isInstantiationAuthorized(InheritingComponent.class));
	}

	/**
	 * Java has no annotation inheritance between packages.
	 */
	@Test
	void packageAnnotationDoesNotApplyToSubpackage()
	{
		assertTrue(strategy().isInstantiationAuthorized(NestedComponent.class));
		assertTrue(strategy().isActionAuthorized(NestedComponent.class, Component.RENDER));
	}

	@Test
	void packageAnnotationAppliesToActionOfClassWithoutOneOfItsOwn()
	{
		assertTrue(strategy("role1").isActionAuthorized(PlainComponent.class, Component.RENDER));
		assertFalse(strategy("role2").isActionAuthorized(PlainComponent.class, Component.RENDER));
	}

	/**
	 * Actions are resolved per action name, so restricting one action on the class leaves the rule of
	 * the package for another action in place.
	 */
	@Test
	void classActionAnnotationOnlyReplacesThePackageRuleForTheSameAction()
	{
		assertTrue(strategy("role2").isActionAuthorized(EnableOnlyComponent.class, Component.ENABLE));
		assertFalse(strategy("role1").isActionAuthorized(EnableOnlyComponent.class, Component.ENABLE));

		assertTrue(strategy("role1").isActionAuthorized(EnableOnlyComponent.class, Component.RENDER));
		assertFalse(strategy("role2").isActionAuthorized(EnableOnlyComponent.class, Component.RENDER));
	}

	@Test
	void inheritedActionAnnotationSuppressesPackageAnnotation()
	{
		assertTrue(
			strategy("role2").isActionAuthorized(InheritingActionComponent.class, Component.RENDER));
		assertFalse(
			strategy("role1").isActionAuthorized(InheritingActionComponent.class, Component.RENDER));
	}

	@Test
	void packageAnnotationAppliesToResourceWithoutOneOfItsOwn()
	{
		assertTrue(strategy("role1").isResourceAuthorized(new PackageProtectedResource(), null));
		assertFalse(strategy("role2").isResourceAuthorized(new PackageProtectedResource(), null));
	}

	/**
	 * Resources are resolved like everything else: the annotation on the class replaces the one on
	 * the package instead of being combined with it.
	 */
	@Test
	void classAnnotationReplacesPackageAnnotationForResources()
	{
		assertTrue(strategy("role2").isResourceAuthorized(new OverridingResource(), null));
		assertFalse(strategy("role1").isResourceAuthorized(new OverridingResource(), null));
	}

	/**
	 * Create a strategy whose role checker is given a list of roles and returns true if that list
	 * contains any of the asked-for roles.
	 * 
	 * @param availableRoles
	 *            the roles the current user has
	 * @return the strategy to test
	 */
	private AnnotationsRoleAuthorizationStrategy strategy(final String... availableRoles)
	{
		IRoleCheckingStrategy roleChecker = requiredRoles -> requiredRoles
			.hasAnyRole(new Roles(availableRoles));

		return new AnnotationsRoleAuthorizationStrategy(roleChecker);
	}
}
