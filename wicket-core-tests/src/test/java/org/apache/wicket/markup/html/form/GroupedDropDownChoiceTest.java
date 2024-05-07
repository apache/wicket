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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.apache.wicket.markup.html.form.GroupedDropDownChoiceTestPage.Service;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


class GroupedDropDownChoiceTest extends WicketTestCase
{

    /**
     * @throws Exception
     */
    @BeforeEach
    void before() throws Exception
    {
        tester.getSession().setLocale(Locale.ENGLISH);
    }

    /**
     * Null model object with null not valid.
     *
     * @throws Exception
     */
    @Test
    void nullWithNullValidFalse() throws Exception
    {
        final List<Service> SERVICES = new ArrayList<>();
        SERVICES.add(new Service("Service0-no-group", null));
        SERVICES.add(new Service("Service0.1-no-group", null));
        SERVICES.add(new Service("Service1", "main"));
        SERVICES.add(new Service("Service2", "main"));
        SERVICES.add(new Service("Service2.1-no-group", null));
        SERVICES.add(new Service("Service2.2-no-group", null));
        SERVICES.add(new Service("Service3", "secondary"));
        SERVICES.add(new Service("Service4", "secondary"));
        SERVICES.add(new Service("Service5", "secondary"));
        SERVICES.add(new Service("Service5.1-no-group", null));
        SERVICES.add(new Service("Service5.2-no-group", null));
        SERVICES.add(new Service("Service6", "other"));
        SERVICES.add(new Service("Service6.1", "other"));
        SERVICES.add(new Service("Service7-no-group", null));
        SERVICES.add(new Service("Service8-no-group", null));
        executeTest(new GroupedDropDownChoiceTestPage(null,  SERVICES, false), "GroupedDropDownChoiceTestPage_null_false_expected.html");
    }

    @Test
    void nullWithNullValidTrue() throws Exception
    {
        final List<Service> SERVICES = new ArrayList<>();
        SERVICES.add(new Service("Service0-no-group", null));
        SERVICES.add(new Service("Service0.1-no-group", null));
        SERVICES.add(new Service("Service1", "main"));
        SERVICES.add(new Service("Service2", "main"));
        SERVICES.add(new Service("Service2.1-no-group", null));
        SERVICES.add(new Service("Service2.2-no-group", null));
        SERVICES.add(new Service("Service3", "secondary"));
        SERVICES.add(new Service("Service4", "secondary"));
        SERVICES.add(new Service("Service5", "secondary"));
        SERVICES.add(new Service("Service5.1-no-group", null));
        SERVICES.add(new Service("Service5.2-no-group", null));
        SERVICES.add(new Service("Service6", "other"));
        SERVICES.add(new Service("Service6.1", "other"));
        SERVICES.add(new Service("Service7-no-group", null));
        SERVICES.add(new Service("Service8-no-group", null));
        executeTest(new GroupedDropDownChoiceTestPage(null,  SERVICES, true), "GroupedDropDownChoiceTestPage_null_true_expected.html");
    }

    @Test
    void notNullWithNullValidFalse() throws Exception
    {
        final List<Service> SERVICES = new ArrayList<>();
        SERVICES.add(new Service("Service0-no-group", null));
        SERVICES.add(new Service("Service0.1-no-group", null));
        SERVICES.add(new Service("Service1", "main"));
        SERVICES.add(new Service("Service2", "main"));
        SERVICES.add(new Service("Service2.1-no-group", null));
        SERVICES.add(new Service("Service2.2-no-group", null));
        SERVICES.add(new Service("Service3", "secondary"));
        SERVICES.add(new Service("Service4", "secondary"));
        SERVICES.add(new Service("Service5", "secondary"));
        SERVICES.add(new Service("Service5.1-no-group", null));
        SERVICES.add(new Service("Service5.2-no-group", null));
        SERVICES.add(new Service("Service6", "other"));
        SERVICES.add(new Service("Service6.1", "other"));
        SERVICES.add(new Service("Service7-no-group", null));
        SERVICES.add(new Service("Service8-no-group", null));
        executeTest(new GroupedDropDownChoiceTestPage(SERVICES.get(0),  SERVICES, false), "GroupedDropDownChoiceTestPage_0_false_expected.html");
    }

    @Test
    void notNullWithNullValidtrue() throws Exception
    {
        final List<Service> SERVICES = new ArrayList<>();
        SERVICES.add(new Service("Service0-no-group", null));
        SERVICES.add(new Service("Service0.1-no-group", null));
        SERVICES.add(new Service("Service1", "main"));
        SERVICES.add(new Service("Service2", "main"));
        SERVICES.add(new Service("Service2.1-no-group", null));
        SERVICES.add(new Service("Service2.2-no-group", null));
        SERVICES.add(new Service("Service3", "secondary"));
        SERVICES.add(new Service("Service4", "secondary"));
        SERVICES.add(new Service("Service5", "secondary"));
        SERVICES.add(new Service("Service5.1-no-group", null));
        SERVICES.add(new Service("Service5.2-no-group", null));
        SERVICES.add(new Service("Service6", "other"));
        SERVICES.add(new Service("Service6.1", "other"));
        SERVICES.add(new Service("Service7-no-group", null));
        SERVICES.add(new Service("Service8-no-group", null));
        executeTest(new GroupedDropDownChoiceTestPage(SERVICES.get(0),  SERVICES, true), "GroupedDropDownChoiceTestPage_0_true_expected.html");
    }
}
