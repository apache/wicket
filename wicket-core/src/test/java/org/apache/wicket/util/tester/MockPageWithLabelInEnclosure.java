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
package org.apache.wicket.util.tester;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;

/**
 * Mock page which has an ajax link in an enclosure. Clicking the link invokes ajax rendering the link again.
 *
 * @author Domas Poliakas (disblader)
 */
public class MockPageWithLabelInEnclosure extends WebPage {

    public MockPageWithLabelInEnclosure() {
        // Clicking this link re-renders the link itself
        this.add(new AjaxLink<Void>("testLink") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                target.add(this);
            }
        });
    }

    public AjaxLink<Void> getSelfRefreshingAjaxLink(){
        return (AjaxLink<Void>) get("testLink");
    }
}
