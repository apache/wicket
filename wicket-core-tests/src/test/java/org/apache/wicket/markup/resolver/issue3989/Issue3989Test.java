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
package org.apache.wicket.markup.resolver.issue3989;

import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.Test;

/**
 * When the {@link org.apache.wicket.Component#markup markup of a component} has been reset at the
 * end of the request, and the component's markup was resolved by a
 * {@code TransparentWebMarkupContainer}, it will fail when being re-rendered in an Ajax request.
 * The cause is that the {@code PanelMarkupSourcingStrategy} fails to look for markup in
 * {@code IComponentResolver}s like {@code TransparentWebMarkupContainer}.
 */
class Issue3989Test
{
	/**
	 * This will fail unless the markup sourcing strategies look for the label {@code innerpanel} in
	 * the transparent markup container.
	 */
	@Test
    void ajaxRenderOfTransparentlyResolvedLabel()
	{
		WicketTester tester = new WicketTester();
		tester.startPage(HomePage.class);
		tester.assertRenderedPage(HomePage.class);
		tester.clickLink("panel:link");
		tester.assertComponentOnAjaxResponse("panel:innerpanel");
	}
}
