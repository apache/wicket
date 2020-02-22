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
package org.apache.wicket.core.request.handler;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Page;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.page.PartialPageUpdate;
import org.apache.wicket.request.component.IRequestablePage;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;

/**
 * Abstract  {@link IPartialPageRequestHandler} that implements common methods for Ajax and Web-sockets
 * specializations of {@link IPartialPageRequestHandler}
 */
public abstract class AbstractPartialPageRequestHandler implements IPartialPageRequestHandler
{
    /** The associated Page */
    private final Page page;

    protected AbstractPartialPageRequestHandler(Page page) {
        this.page = Args.notNull(page, "page");
    }

    @Override
    public final void addChildren(MarkupContainer parent, Class<?> childCriteria)
    {
        Args.notNull(parent, "parent");
        Args.notNull(childCriteria, "childCriteria");

        parent.visitChildren(childCriteria, new IVisitor<Component, Void>()
        {
            @Override
            public void component(final Component component, final IVisit<Void> visit)
            {
                add(component);
                visit.dontGoDeeper();
            }
        });
    }

    @Override
    public final void add(Component... components)
    {
        for (final Component component : components)
        {
            Args.notNull(component, "component");

            if (component.getOutputMarkupId() == false)
            {
                throw new IllegalArgumentException(
                        "cannot update component that does not have setOutputMarkupId property set to true. Component: " +
                                component.toString());
            }

            add(component, component.getMarkupId());
        }
    }

    @Override
    public final Integer getPageId()
    {
        return page.getPageId();
    }

    @Override
    public final Integer getRenderCount()
    {
        return page.getRenderCount();
    }

    @Override
    public final PageParameters getPageParameters()
    {
        return page.getPageParameters();
    }

    @Override
    public final void add(Component component, String markupId)
    {
        getUpdate().add(component, markupId);
    }

    @Override
    public final void prependJavaScript(CharSequence javascript)
    {
        getUpdate().prependJavaScript(javascript);
    }

    @Override
    public final void appendJavaScript(CharSequence javascript)
    {
        getUpdate().appendJavaScript(javascript);
    }

    @Override
    public final void focusComponent(Component component)
    {
        if (component != null && component.getOutputMarkupId() == false)
        {
            throw new IllegalArgumentException(
                    "cannot update component that does not have setOutputMarkupId property set to true. Component: " +
                            component.toString());
        }
        final String id = component != null ? ("'" + component.getMarkupId() + "'") : "null";
        appendJavaScript("Wicket.Focus.setFocusOnId(" + id + ");");
    }

    @Override
    public final boolean isPageInstanceCreated()
    {
        return true;
    }

    @Override
    public final IHeaderResponse getHeaderResponse()
    {
        return getUpdate().getHeaderResponse();
    }

    protected abstract PartialPageUpdate getUpdate();

    /**
     * @see org.apache.wicket.core.request.handler.IPageRequestHandler#getPage()
     */
    @Override
    public final Page getPage()
    {
        return page;
    }

    @Override
    public final Class<? extends IRequestablePage> getPageClass()
    {
        return page.getPageClass();
    }
}
