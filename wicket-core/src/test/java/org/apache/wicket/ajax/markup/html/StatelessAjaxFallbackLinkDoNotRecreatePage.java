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
package org.apache.wicket.ajax.markup.html;

import java.util.Optional;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringValue;

public class StatelessAjaxFallbackLinkDoNotRecreatePage extends WebPage
{
  private static final long serialVersionUID = 1L;

  private static final String COUNTER_PARAM = "counter";

  /**
   * Constructor that is invoked when page is invoked without a session.
   *
   * @param parameters
   *          Page parameters
   */
  public StatelessAjaxFallbackLinkDoNotRecreatePage(final PageParameters parameters)
  {
    super(parameters);
    setStatelessHint(true);

    final Label incrementLabel = new Label("incrementLabel", new AbstractReadOnlyModel<Integer>()
    {
      private static final long serialVersionUID = 1L;

      @Override
      public Integer getObject()
      {
        final String counter = getParameter(parameters, COUNTER_PARAM);
        return counter != null ? Integer.parseInt(counter) : 0;
      }
    });
    final Link<?> incrementLink = new AjaxFallbackLink<Void>("incrementLink")
    {
      private static final long serialVersionUID = 1L;

      @Override
      public void onClick(final Optional<AjaxRequestTarget> targetOptional)
      {
        Integer counter = (Integer) incrementLabel.getDefaultModelObject();
        updateParams(getPageParameters(), counter);
        targetOptional.ifPresent(target -> target.add(incrementLabel, this));
      }

      @Override
      protected boolean getStatelessHint()
      {
        return true;
      }
    };

    add(incrementLink);
    add(incrementLabel.setOutputMarkupId(true));
  }

  private String getParameter(final PageParameters parameters, final String key)
  {
    final StringValue value = parameters.get(key);

    if (value.isNull() || value.isEmpty())
    {
      return null;
    }

    return value.toString();
  }

  protected final void updateParams(final PageParameters pageParameters, final int counter)
  {
    pageParameters.set(COUNTER_PARAM, Integer.toString(counter + 1));
  }
}
