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

import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import org.apache.wicket.Component;
import org.apache.wicket.authorization.Action;
import org.apache.wicket.authroles.authorization.strategies.role.AbstractRoleAuthorizationStrategy;
import org.apache.wicket.authroles.authorization.strategies.role.IRoleCheckingStrategy;
import org.apache.wicket.authroles.authorization.strategies.role.Roles;
import org.apache.wicket.request.component.IRequestableComponent;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.IResource;


/**
 * Strategy that checks the role annotations in this package:
 * <ul>
 * <li>{@link AuthorizeInstantiation} and {@link AuthorizeInstantiations} guard the instantiation of
 * a component,</li>
 * <li>{@link AuthorizeAction} and {@link AuthorizeActions} guard an {@link Action} on a component
 * instance, such as {@link org.apache.wicket.Component#RENDER} and
 * {@link org.apache.wicket.Component#ENABLE},</li>
 * <li>{@link AuthorizeResource} guards the request of a resource.</li>
 * </ul>
 * <p>
 * Each of these annotations can be placed on a class or on a package. A package is annotated through
 * its <code>package-info.java</code> file:
 *
 * <pre>
 *  // only users with role ADMIN are allowed to create instances of pages in this package
 *  &#064;AuthorizeInstantiation(&quot;ADMIN&quot;)
 *  package com.example.admin;
 *
 *  import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
 * </pre>
 *
 * <p>
 * <b>Annotations on a class replace the annotations on its package.</b> The annotations on a package
 * are a default that a class can override: when a class carries any annotation relevant to the check
 * being performed, the annotations on its package are not consulted at all. Annotations at the same
 * level are combined with AND, so every one of them has to grant access. For actions this is decided
 * per action name, which means that a class restricting only <code>ENABLE</code> still inherits the
 * <code>RENDER</code> restriction of its package. Within a single {@link AuthorizeAction},
 * {@link AuthorizeAction#deny()} is evaluated before {@link AuthorizeAction#roles()}: a user holding
 * a denied role is refused even when that user also holds an accepted one.
 * <p>
 * Because all of these annotations are {@link java.lang.annotation.Inherited}, the annotations
 * &quot;on a class&quot; include those declared on any of its superclasses, in any package. That is
 * worth spelling out, because it is easy to be caught by:
 *
 * <pre>
 *  // com/example/base/SecuredPage.java
 *  &#064;AuthorizeInstantiation(&quot;USER&quot;)
 *  public class SecuredPage extends WebPage
 *
 *  // com/example/admin/package-info.java
 *  &#064;AuthorizeInstantiation(&quot;ADMIN&quot;)
 *  package com.example.admin;
 *
 *  // com/example/admin/ReportPage.java -- requires USER, not ADMIN!
 *  public class ReportPage extends SecuredPage
 * </pre>
 *
 * <p>
 * <code>ReportPage</code> inherits <code>&#064;AuthorizeInstantiation(&quot;USER&quot;)</code> from
 * its superclass, that inherited annotation counts as an annotation on the class, and therefore the
 * ADMIN restriction of its own package is never applied. Annotate such a subclass explicitly to give
 * it the roles of its package.
 * <p>
 * Further points to keep in mind when annotating packages:
 * <ul>
 * <li>A package annotation applies to that one package only. Java has no annotation inheritance
 * between packages, so <code>com.example</code> does not pass its annotations on to
 * <code>com.example.admin</code>.</li>
 * <li>{@link java.lang.annotation.Inherited} has no meaning on a package, and inheritance between
 * classes never crosses over to interfaces.</li>
 * <li>The <code>package-info.java</code> file has to be compiled and shipped, and to be loaded by
 * the same class loader as the classes of the package, or its annotations cannot be found at
 * runtime.</li>
 * <li>An annotation without any roles, such as <code>&#064;AuthorizeInstantiation()</code>,
 * authorizes everybody. Because it still replaces the annotations on the package, it is the way to
 * exempt a single class from the restrictions of its package.</li>
 * </ul>
 * <p>
 * Note that
 * {@link org.apache.wicket.authroles.authorization.strategies.role.metadata.MetaDataRoleAuthorizationStrategy}
 * resolves its permissions quite differently: it looks up the exact component class and has no
 * package or superclass fallback at all. When both strategies are combined, as
 * {@link org.apache.wicket.authroles.authorization.strategies.role.RoleAuthorizationStrategy} does,
 * they are combined with AND: both have to grant access.
 *
 * @see org.apache.wicket.authorization.IAuthorizationStrategy
 * @see org.apache.wicket.authroles.authorization.strategies.role.RoleAuthorizationStrategy
 *
 * @author Eelco Hillenius
 */
public class AnnotationsRoleAuthorizationStrategy extends AbstractRoleAuthorizationStrategy
{
	/**
	 * Construct.
	 * 
	 * @param roleCheckingStrategy
	 *            the authorizer delegate
	 */
	public AnnotationsRoleAuthorizationStrategy(final IRoleCheckingStrategy roleCheckingStrategy)
	{
		super(roleCheckingStrategy);
	}

	/**
	 * @see org.apache.wicket.authorization.IAuthorizationStrategy#isInstantiationAuthorized(java.lang.Class)
	 */
	@Override
	public <T extends IRequestableComponent> boolean isInstantiationAuthorized(
		final Class<T> componentClass)
	{
		for (final AuthorizeInstantiation rule : resolve(componentClass,
			AnnotationsRoleAuthorizationStrategy::instantiationRules))
		{
			if (!hasAny(new Roles(rule.value())))
			{
				return false;
			}
		}

		return true;
	}

	/**
	 * @see org.apache.wicket.authorization.IAuthorizationStrategy#isActionAuthorized(org.apache.wicket.Component,
	 *      org.apache.wicket.authorization.Action)
	 */
	@Override
	public boolean isActionAuthorized(final Component component, final Action action)
	{
		// Get component's class
		final Class<?> componentClass = component.getClass();

		return isActionAuthorized(componentClass, action);
	}

	/**
	 * Checks whether the given action is authorized for the given component class. Rules are
	 * resolved per action: the rules on the class replace the rules of its package only for the
	 * action they name.
	 * 
	 * @param componentClass
	 *            the class of the component the action is performed on
	 * @param action
	 *            the action to check
	 * @return false if the action is not authorized
	 */
	protected boolean isActionAuthorized(final Class<?> componentClass, final Action action)
	{
		for (final AuthorizeAction rule : resolve(componentClass,
			element -> actionRules(element, action)))
		{
			final Roles deniedRoles = new Roles(rule.deny());
			if (isEmpty(deniedRoles) == false && hasAny(deniedRoles))
			{
				return false;
			}

			if (!hasAny(new Roles(rule.roles())))
			{
				return false;
			}
		}

		return true;
	}

	@Override
	public boolean isResourceAuthorized(IResource resource, PageParameters pageParameters)
	{
		for (final AuthorizeResource rule : resolve(resource.getClass(),
			AnnotationsRoleAuthorizationStrategy::resourceRules))
		{
			if (!hasAny(new Roles(rule.value())))
			{
				return false;
			}
		}

		return true;
	}

	/**
	 * Resolves the rules that apply to the given class: the rules declared on the class itself if
	 * there are any, and the rules declared on its package otherwise. Since all annotations in this
	 * package are {@link java.lang.annotation.Inherited}, the rules declared on the class include
	 * those it inherits from a superclass in another package, and such an inherited rule therefore
	 * suppresses the rules of the class' own package.
	 * 
	 * @param annotatedClass
	 *            the component or resource class to resolve the rules for
	 * @param rules
	 *            extracts the rules relevant to the check being performed from a class or a package
	 * @return the rules that apply, empty if the class is not restricted
	 */
	private static <R> List<R> resolve(final Class<?> annotatedClass,
		final Function<AnnotatedElement, List<R>> rules)
	{
		List<R> resolved = rules.apply(annotatedClass);

		if (resolved.isEmpty())
		{
			// Only fall back to the package when the class itself says nothing
			final Package annotatedPackage = annotatedClass.getPackage();
			if (annotatedPackage != null)
			{
				resolved = rules.apply(annotatedPackage);
			}
		}

		return resolved;
	}

	/**
	 * @param element
	 *            a class or a package
	 * @return the instantiation rules declared on the given element, both the single
	 *         {@link AuthorizeInstantiation} and the {@link AuthorizeInstantiations} ruleset
	 */
	private static List<AuthorizeInstantiation> instantiationRules(final AnnotatedElement element)
	{
		final AuthorizeInstantiation single = element.getAnnotation(AuthorizeInstantiation.class);
		final AuthorizeInstantiations ruleset = element.getAnnotation(AuthorizeInstantiations.class);

		if (single == null && ruleset == null)
		{
			return Collections.emptyList();
		}

		final List<AuthorizeInstantiation> rules = new ArrayList<>();
		if (single != null)
		{
			rules.add(single);
		}
		if (ruleset != null)
		{
			Collections.addAll(rules, ruleset.ruleset());
		}

		return rules;
	}

	/**
	 * @param element
	 *            a class or a package
	 * @param action
	 *            the action being checked
	 * @return the rules declared on the given element that apply to the given action, both the
	 *         single {@link AuthorizeAction} and the ones grouped by {@link AuthorizeActions}
	 */
	private static List<AuthorizeAction> actionRules(final AnnotatedElement element,
		final Action action)
	{
		final AuthorizeAction single = element.getAnnotation(AuthorizeAction.class);
		final AuthorizeActions grouped = element.getAnnotation(AuthorizeActions.class);

		if (grouped == null)
		{
			// The common case: at most one annotation, so avoid building a list for it
			if (single != null && action.getName().equals(single.action()))
			{
				return Collections.singletonList(single);
			}
			return Collections.emptyList();
		}

		final List<AuthorizeAction> rules = new ArrayList<>();
		if (single != null && action.getName().equals(single.action()))
		{
			rules.add(single);
		}
		for (final AuthorizeAction rule : grouped.actions())
		{
			if (action.getName().equals(rule.action()))
			{
				rules.add(rule);
			}
		}

		return rules;
	}

	/**
	 * @param element
	 *            a class or a package
	 * @return the {@link AuthorizeResource} declared on the given element, if any
	 */
	private static List<AuthorizeResource> resourceRules(final AnnotatedElement element)
	{
		final AuthorizeResource rule = element.getAnnotation(AuthorizeResource.class);

		return rule != null ? Collections.singletonList(rule) : Collections.emptyList();
	}
}
