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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class GroupsAndChoicesValues implements Serializable
{
    private List<Integer> checkGroup = new ArrayList<>();
    private Integer radioGroup;
    private List<Integer> checkBoxMultiple = new ArrayList<>();
    private Integer radioChoice;

    public List<Integer> getCheckGroup()
    {
        return checkGroup;
    }

    public void setCheckGroup(List<Integer> checkGroup)
    {
        this.checkGroup = checkGroup;
    }

    public Integer getRadioGroup()
    {
        return radioGroup;
    }

    public void setRadioGroup(Integer radioGroup)
    {
        this.radioGroup = radioGroup;
    }

    public List<Integer> getCheckBoxMultiple()
    {
        return checkBoxMultiple;
    }

    public void setCheckBoxMultiple(List<Integer> checkBoxMultiple)
    {
        this.checkBoxMultiple = checkBoxMultiple;
    }

    public Integer getRadioChoice()
    {
        return radioChoice;
    }

    public void setRadioChoice(Integer radioChoice)
    {
        this.radioChoice = radioChoice;
    }

    @Override
    public String toString()
    {
        return "GroupsAndChoicesValues{" +
                "checkGroup=" + checkGroup +
                ", radioGroup=" + radioGroup +
                ", checkBoxMultiple=" + checkBoxMultiple +
                ", radioChoice=" + radioChoice +
                '}';
    }
}
