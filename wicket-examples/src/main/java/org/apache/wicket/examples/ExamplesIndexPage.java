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
package org.apache.wicket.examples;

/**
 * An index that gathers a few examples which are applications of their own under one entry of the
 * main menu, so that the menu stays a list of subjects rather than of deployments.
 * <p>
 * A group is a subclass with nothing in it and a bundle saying what it gathers, as described on
 * {@link ExampleApplicationsPanel}. The markup is here, so a group has none of its own.
 *
 * @see ExampleApplicationsPanel
 */
public abstract class ExamplesIndexPage extends WicketExamplePage
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 */
	public ExamplesIndexPage()
	{
		add(new ExampleApplicationsPanel("examples", getClass()));
	}
}
