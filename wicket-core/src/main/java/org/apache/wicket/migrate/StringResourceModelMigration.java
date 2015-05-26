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
package org.apache.wicket.migrate;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;

/**
 * Assistant class for migrating old Wicket 6.x style StringResourceModel instantiations to the
 * Wicket 7.x fluent API.
 * 
 * Replace your old invocation of {@code StringResourceModel}'s constructor:
 * 
 * <pre>
 * new StringResourceModel(...);
 * </pre>
 * 
 * with the following snippet:
 * 
 * <pre>
 * StringResourceModelMigration.of(...);
 * </pre>
 *
 * and then use your IDE's <i>Inline Method</i> refactoring support to use the new fluent API.
 * 
 * @since 7.0.0
 */
public class StringResourceModelMigration
{
	/**
	 * @deprecated use 'inline method' to get rid of the deprecation warning and complete the
	 *             migration to Wicket 7 API.
	 */
	/*
	 * Original JavaDoc:
	 * 
	 * Creates a new string resource model using the supplied parameters. <p> The relative component
	 * parameter should generally be supplied, as without it resources can not be obtained from
	 * resource bundles that are held relative to a particular component or page. However, for
	 * application that use only global resources then this parameter may be null. <p> The model
	 * parameter is also optional and only needs to be supplied if value substitutions are to take
	 * place on either the resource key or the actual resource strings. <p> The parameters parameter
	 * is also optional and is used for substitutions.
	 * 
	 * @param resourceKey The resource key for this string resource
	 * 
	 * @param component The component that the resource is relative to
	 * 
	 * @param model The model to use for property substitutions
	 * 
	 * @param parameters The parameters to substitute using a Java MessageFormat object
	 */
	@Deprecated
	@SuppressWarnings(value = "javadoc")
	public static StringResourceModel of(final String resourceKey, final Component component,
		final IModel<?> model, final Object... parameters)
	{
		return new StringResourceModel(resourceKey, component).setModel(model).setParameters(
			parameters);
	}

	/**
	 * @deprecated use 'inline method' to get rid of the deprecation warning and complete the
	 *             migration to Wicket 7 API.
	 */
	/*
	 * Original JavaDoc:
	 * 
	 * Creates a new string resource model using the supplied parameters.
	 * <p>
	 * The relative component parameter should generally be supplied, as without it resources can
	 * not be obtained from resource bundles that are held relative to a particular component or
	 * page. However, for application that use only global resources then this parameter may be
	 * null.
	 * <p>
	 * The model parameter is also optional and only needs to be supplied if value substitutions are
	 * to take place on either the resource key or the actual resource strings.
	 * <p>
	 * The parameters parameter is also optional and is used for substitutions.
	 * 
	 * @param resourceKey
	 *            The resource key for this string resource
	 * @param component
	 *            The component that the resource is relative to
	 * @param model
	 *            The model to use for property substitutions
	 * @param defaultValue
	 *            The default value if the resource key is not found.
	 * @param parameters
	 *            The parameters to substitute using a Java MessageFormat object
	 */
	@Deprecated
	@SuppressWarnings(value = "javadoc")
	public static StringResourceModel of(final String resourceKey, final Component component,
		final IModel<?> model, final String defaultValue, final Object... parameters)
	{
		return new StringResourceModel(resourceKey, component).setModel(model)
			.setDefaultValue(defaultValue)
			.setParameters(parameters);
	}

	/**
	 * @deprecated use 'inline method' to get rid of the deprecation warning and complete the
	 *             migration to Wicket 7 API.
	 */
	/*
	 * Original JavaDoc:
	 * 
	 * Creates a new string resource model using the supplied parameters.
	 * <p>
	 * The model parameter is also optional and only needs to be supplied if value substitutions are
	 * to take place on either the resource key or the actual resource strings.
	 * <p>
	 * The parameters parameter is also optional and is used for substitutions.
	 * 
	 * @param resourceKey
	 *            The resource key for this string resource
	 * @param model
	 *            The model to use for property substitutions
	 * @param parameters
	 *            The parameters to substitute using a Java MessageFormat object
	 */
	@Deprecated
	@SuppressWarnings(value = "javadoc")
	public static StringResourceModel of(final String resourceKey, final IModel<?> model,
		final Object... parameters)
	{
		return new StringResourceModel(resourceKey).setModel(model).setParameters(parameters);
	}

	/**
	 * @deprecated use 'inline method' to get rid of the deprecation warning and complete the
	 *             migration to Wicket 7 API.
	 */
	/*
	 * Original JavaDoc:
	 * 
	 * Creates a new string resource model using the supplied parameters.
	 * <p>
	 * The model parameter is also optional and only needs to be supplied if value substitutions are
	 * to take place on either the resource key or the actual resource strings.
	 * <p>
	 * The parameters parameter is also optional and is used for substitutions.
	 * 
	 * @param resourceKey
	 *            The resource key for this string resource
	 * @param model
	 *            The model to use for property substitutions
	 * @param parameters
	 *            The parameters to substitute using a Java MessageFormat object
	 * @param defaultValue
	 *            The default value if the resource key is not found.
	 */
	@Deprecated
	@SuppressWarnings(value = "javadoc")
	public static StringResourceModel of(final String resourceKey, final IModel<?> model,
		final String defaultValue, final Object... parameters)
	{
		return new StringResourceModel(resourceKey).setModel(model)
			.setDefaultValue(defaultValue)
			.setParameters(parameters);
	}
}
