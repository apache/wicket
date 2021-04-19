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
package org.apache.wicket;

import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public class BehavioursDetachTestPage extends WebPage {
    private static final long serialVersionUID = 1L;

    /**
     * This model should be detached after page rendering
     */
    final LoadableDetachableModel<String> theModel;

    public BehavioursDetachTestPage(final PageParameters parameters) {
        super(parameters);

        // Bug seems to only occur on stateful pages,
        // hence a Link to force it to be stateful in this quickstart
        Link<String> link = new Link<String>("link") {
            @Override
            public void onClick() {/*NoOp*/}
        };
        queue(link);

        // A behavior that causes the problem
        link.add(new VisibilityBehavior());

        theModel = LoadableDetachableModel.of(() -> "attribute value");

        // AttributeModifier and thus its LoadableDetachableModel don't get detached
        link.add(AttributeModifier.replace("some-random-attribute", theModel));
    }

    /**
     * A behavior that modifies the component's data by removing the last MetaDataEntry
     * in its detach() method
     */
    public static class VisibilityBehavior extends Behavior {

        private static final MetaDataKey<Boolean> META_DATA_KEY = new MetaDataKey<Boolean>() {};

        @Override
        public void onConfigure(Component component) {
            super.onConfigure(component);

            // If no other Behavior of this kind deemed the component to be hidden yet,
            // calculate its visibilty now
            Boolean calculatdVisibilitySoFar = component.getMetaData(META_DATA_KEY);
            if (!Boolean.FALSE.equals(calculatdVisibilitySoFar)) {
                boolean calculatedVisibility = calculateVisibility();
                component.setVisibilityAllowed(calculatedVisibility);
                component.setMetaData(META_DATA_KEY, calculatedVisibility);
            }
        }

        // usually abstract and gets overridden in subclasses with more meaningful code
        protected boolean calculateVisibility() {
            return true;
        }

        @Override
        public void detach(Component component) {
            // Reset visibility information so it's not being used in the next request anymore.

            // ==> Setting the MetaData to "null" here causes the issue
            component.setMetaData(META_DATA_KEY, null);
            super.detach(component);
        }
    }

}
