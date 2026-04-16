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
package org.apache.wicket.examples.ajax.builtin;

import java.util.List;

import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LambdaModel;
import org.apache.wicket.model.Model;

public class GroupsAndChoicesPanel extends FormComponentPanel<GroupsAndChoicesValues>
{
    private CheckGroup<Integer> checkGroup =
            new CheckGroup<>("checkGroup", LambdaModel.of(getModel(), GroupsAndChoicesValues::getCheckGroup, GroupsAndChoicesValues::setCheckGroup));
    private Check<Integer> check1 = new Check<>("check1", Model.of(1));
    private Check<Integer> check2 = new Check<>("check2", Model.of(2));
    private Check<Integer> check3 = new Check<>("check3", Model.of(3));

    private RadioGroup<Integer> radioGroup =
            new RadioGroup<>("radioGroup", LambdaModel.of(getModel(), GroupsAndChoicesValues::getRadioGroup, GroupsAndChoicesValues::setRadioGroup));
    private Radio<Integer> radio4 = new Radio<>("radio4", Model.of(4));
    private Radio<Integer> radio5 = new Radio<>("radio5", Model.of(5));

    private CheckBoxMultipleChoice<Integer> checkBoxMultipleChoice =
            new CheckBoxMultipleChoice<>("checkBoxMultipleChoice", LambdaModel.of(getModel(), GroupsAndChoicesValues::getCheckBoxMultiple, GroupsAndChoicesValues::setCheckBoxMultiple), List.of(6, 7, 8));

    private RadioChoice<Integer> radioChoice =
            new RadioChoice<>("radioChoice", LambdaModel.of(getModel(), GroupsAndChoicesValues::getRadioChoice, GroupsAndChoicesValues::setRadioChoice), List.of(9, 0));

    public GroupsAndChoicesPanel(String id, IModel<GroupsAndChoicesValues> model)
    {
        super(id, model);
    }

    @Override
    protected void onInitialize()
    {
        super.onInitialize();

        add(
                checkGroup.add(
                        check1,
                        check2,
                        check3
                ),
                radioGroup.add(
                        radio4,
                        radio5
                ),
                checkBoxMultipleChoice,
                radioChoice
        );
    }

    @Override
    public void convertInput()
    {
        setConvertedInput(getModelObject());
    }

    @Override
    public void processInputOfChildren()
    {
        processInputOfChild(checkGroup);
        processInputOfChild(radioGroup);
        processInputOfChild(checkBoxMultipleChoice);
        processInputOfChild(radioChoice);
    }
}
