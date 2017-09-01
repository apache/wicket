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
package org.apache.wicket.markup.renderStrategy;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;

/**
 * Mock for {@link ChildFirstHeaderRenderStrategyTest#testAjaxAndEnclosures()}
 */
public class SimplePage3 extends WebPage {

    public SimplePage3() {

        this.add(createEnclosureController("enclosed"));
        this.add(createEnclosureController("enclosedInInline"));
        this.add(new AjaxLink<Void>("ajaxLink") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                target.add(getPage().get("enclosed"));
                target.add(getPage().get("enclosedInInline"));
            }
        });

    }

    private Component createEnclosureController(final String id){
        return new WebMarkupContainer(id){
            @Override
            public void renderHead(IHeaderResponse response) {
                super.renderHead(response);
                response.render(CssHeaderItem.forUrl(id + ".css"));
            }
        }.setOutputMarkupId(true);
    }

}
