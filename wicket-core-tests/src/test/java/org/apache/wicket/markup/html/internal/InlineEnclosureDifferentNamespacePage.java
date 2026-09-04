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
package org.apache.wicket.markup.html.internal;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;


/**
 * Mock page for testing.
 *
 * @author Joonas Hamalainen
 *
 */
public class InlineEnclosureDifferentNamespacePage extends WebPage
{
	private static final long serialVersionUID = 1L;

	private final Label label1 = new Label("label1", "Test Label 1");
	private final Label label2 = new Label("label2", "Test Label 2");
	private final Label label3 = new Label("label3", "Test Label 3");
	private final Label label4 = new Label("label4", "Test Label 4");
	private final Label label5 = new Label("label5", "Test Label 5");
	private final Label label6 = new Label("label6", "Test Label 6");
	private final Label label7 = new Label("label7", "Test Label 7");
	private final Label label8 = new Label("label8", "Test Label 8");
	private final Label label9 = new Label("label9", "Test Label 9");
	private final Label label10 = new Label("label10", "Test Label 10");
	private final Label label11 = new Label("label11", "Test Label 11");
	private final Label label12 = new Label("label12", "Test Label 12");
	private final Label label13 = new Label("label13", "Test Label 13");
	private final Label label14 = new Label("label14", "Test Label 14");
	private final Label label15 = new Label("label15", "Test Label 15");

	/**
	 * Construct.
	 */
	public InlineEnclosureDifferentNamespacePage()
	{
		add(label1);
		add(label2);
		add(label3);
		add(label4);
		add(label5);
		add(label6);
		add(label7);
		add(label8);
		add(label9);
		add(label10);
		add(label11);
		add(label12);

		WebMarkupContainer container = new WebMarkupContainer("container");
		add(container);
		container.add(label13);
		container.add(label14);

		add(label15);
	}
}
