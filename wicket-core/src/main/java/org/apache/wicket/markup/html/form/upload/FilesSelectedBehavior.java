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
package org.apache.wicket.markup.html.form.upload;

import java.util.ArrayList;
import java.util.List;
import org.apache.wicket.Component;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.IAjaxCallListener;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.string.StringValue;
import org.danekja.java.util.function.serializable.SerializableBiConsumer;
import com.github.openjson.JSONArray;
import com.github.openjson.JSONObject;

/**
 * {@link org.apache.wicket.ajax.form.OnChangeAjaxBehavior} that streams back to server properties
 * of the selected file(s) (at client side), before uploading it (them).
 *
 * @author Ernesto Reinaldo Barreiro (reiern70@gmail.com).
 */
public abstract class FilesSelectedBehavior extends OnChangeAjaxBehavior {

    private static final long serialVersionUID = 1L;

    private static final ResourceReference JS = new JavaScriptResourceReference(FilesSelectedBehavior.class, "FilesSelectedBehavior.js");

    @Override
    protected void onBind() {
        super.onBind();
        Component component = getComponent();
        if (!(component instanceof FileUploadField)) {
            throw new WicketRuntimeException("Behavior " + getClass().getName()
                    + " can only be added to an instance of a FileUploadField");
        }
    }

    @Override
    protected void onUpdate(AjaxRequestTarget target) {
        Request request = RequestCycle.get().getRequest();
        List<FileDescription> fileDescriptions = new ArrayList<>();
        IRequestParameters parameters = request.getRequestParameters();
        // data is streamed as JSON.
        StringValue fileInfos = parameters.getParameterValue("fileInfos");
        JSONArray jsonArray = new JSONArray(fileInfos.toString());
        for (int i = 0; i < jsonArray.length(); i++)
        {
            fileDescriptions.add(new FileDescription((JSONObject)jsonArray.get(i)));
        }
        onSelected(target, fileDescriptions);
    }


    /**
     * Called when a file, at client side is selected.
     *
     * @param target           The {@link org.apache.wicket.ajax.AjaxRequestTarget}
     * @param fileDescriptions A list of FileDescription
     */
    protected abstract void onSelected(AjaxRequestTarget target, List<FileDescription> fileDescriptions);

    @Override
    protected void updateAjaxAttributes(AjaxRequestAttributes attributes)
    {
        super.updateAjaxAttributes(attributes);
        attributes.getAjaxCallListeners().add(new IAjaxCallListener()
        {
            @Override
            public CharSequence getPrecondition(Component component)
            {
                return "return Wicket.FilesSelected.precondition(this);";
            }
        });
        attributes.getDynamicExtraParameters().add("return Wicket.FilesSelected.collectFilesDetails('" + getComponent().getMarkupId() + "');");
    }

    @Override
    public void renderHead(Component component, IHeaderResponse response)
    {
        super.renderHead(component, response);
        response.render(JavaScriptHeaderItem.forReference(JS));
    }

    /**
     * Creates an {@link FilesSelectedBehavior} based on lambda expressions
     *
     * @param select {@link SerializableBiConsumer}
     *
     * @return the {@link FilesSelectedBehavior} behavior
     */
    public static FilesSelectedBehavior onSelected(
            SerializableBiConsumer<AjaxRequestTarget, List<FileDescription>> select)
    {
        Args.notNull(select, "select");

        return new FilesSelectedBehavior()
        {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onSelected(AjaxRequestTarget target, List<FileDescription> fileDescriptions) {
                select.accept(target, fileDescriptions);
            }
        };
    }
}
