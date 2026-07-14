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
package org.apache.wicket.markup.html.form;

import java.util.List;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.string.AppendingStringBuffer;
import org.apache.wicket.util.string.Strings;

/**
 * Convenience class for generating dropdown choices with option groups.
 *
 * @param <T> The model object type
 */
public abstract class GroupedDropDownChoice<T> extends DropDownChoice<T>
{
    private static final long serialVersionUID = 1L;

    /*
        A reference to previous option (in order to determine if a new group was created)
     */
    private transient T previousChoice;

    private transient boolean inOptionGroup;

    public GroupedDropDownChoice(String id)
    {
        super(id);
    }

    public GroupedDropDownChoice(String id, List<? extends T> choices)
    {
        super(id, choices);
    }

    public GroupedDropDownChoice(String id, List<? extends T> choices, IChoiceRenderer<? super T> renderer)
    {
        super(id, choices, renderer);
    }

    public GroupedDropDownChoice(String id, IModel<T> model, List<? extends T> choices)
    {
        super(id, model, choices);
    }

    public GroupedDropDownChoice(String id, IModel<T> model, List<? extends T> choices, IChoiceRenderer<? super T> renderer)
    {
        super(id, model, choices, renderer);
    }

    public GroupedDropDownChoice(String id, IModel<? extends List<? extends T>> choices)
    {
        super(id, choices);
    }

    public GroupedDropDownChoice(String id, IModel<T> model, IModel<? extends List<? extends T>> choices)
    {
        super(id, model, choices);
    }

    public GroupedDropDownChoice(String id, IModel<? extends List<? extends T>> choices, IChoiceRenderer<? super T> renderer)
    {
        super(id, choices, renderer);
    }

    public GroupedDropDownChoice(String id, IModel<T> model, IModel<? extends List<? extends T>> choices, IChoiceRenderer<? super T> renderer)
    {
        super(id, model, choices, renderer);
    }

    private boolean isLast(int index)
    {
        return index - 1 == getChoices().size();
    }

    /**
     * Determines if a new group has started
     *
     * @param previous The previous entry (it can be null)
     * @param current  The current entry (always non-null)
     * @return if a new group has started
     */
    protected abstract boolean isNewGroup(T previous, T current);

    /**
     * Determines if current entry belongs to no group
     *
     * @param current The current entry
     * @return true if the current entry belongs to no group.
     */
    protected abstract boolean hasNoGroup(T current);

    /**
     * @param current The current entry
     * @return Returns the label for current group.
     */
    protected abstract IModel<String> getGroupLabel(T current);

    @Override
    protected void appendOptionHtml(AppendingStringBuffer buffer, T choice, int index, String selected)
    {
        if (hasNoGroup(choice))
        {
            if (inOptionGroup) {
                buffer.append("\n</optgroup>");
                inOptionGroup = false;
            }
            super.appendOptionHtml(buffer, choice, index, selected);
            previousChoice = choice;
            return;
        }
        else if (isNewGroup(previousChoice, choice))
        {
            if (inOptionGroup)
            {
                buffer.append("\n</optgroup>");
            }
            inOptionGroup = true;
            buffer.append("\n<optgroup label='");
            buffer.append(Strings.escapeMarkup(getGroupLabel(choice).getObject()));
            buffer.append("'>");
        }
        super.appendOptionHtml(buffer, choice, index, selected);
        if (isLast(index) && inOptionGroup)
        {
            buffer.append("\n</optgroup>");
            inOptionGroup = false;
        }
        previousChoice = choice;
    }
}
