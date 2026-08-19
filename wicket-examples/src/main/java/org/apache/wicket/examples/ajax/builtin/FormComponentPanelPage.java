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

import static org.apache.wicket.markup.html.form.FormComponentPanel.WANT_CHILDREN_TO_PROCESS_INPUT_IN_AJAX_UPDATE;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.stream.Collectors;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.examples.compref.ComponentReferenceApplication;
import org.apache.wicket.examples.compref.Person;
import org.apache.wicket.examples.forminput.Multiply;
import org.apache.wicket.extensions.markup.html.form.datetime.LocalDateTextField;
import org.apache.wicket.extensions.markup.html.form.datetime.LocalDateTimeField;
import org.apache.wicket.extensions.markup.html.form.datetime.TimeField;
import org.apache.wicket.extensions.markup.html.form.palette.Palette;
import org.apache.wicket.extensions.markup.html.form.palette.component.Recorder;
import org.apache.wicket.extensions.markup.html.form.palette.theme.DefaultTheme;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.util.CollectionModel;
import org.apache.wicket.model.util.ListModel;

/**
 * Demonstraties doing Ajax with FormComponentPanels
 *
 * @author Johan Stuyts
 */
public class FormComponentPanelPage extends BasePage
{
    /**
     * Constructor
     */
    public FormComponentPanelPage()
    {
        var now = LocalDateTime.now();

        Form<Object> form = new Form<>("form");
        add(form);

        addLocalDateTimeOnDescendents(form, now);
        addLocalDateTimeOnPanel(form, now);

        addMultiplyOnPanel(form);

        addPaletteOnDescendents(form);
        addPaletteOnPanel(form);

        addGroupsAndChoicesOnPanel(form);
    }

    private void addLocalDateTimeOnDescendents(Form<Object> form, LocalDateTime now)
    {
        var descendentsModel = Model.of(now);

        var descendentsData = new WebMarkupContainer("localDateTimeDescendentsValue");
        descendentsData.setOutputMarkupId(true);

        var ajaxOnDescendents = new LocalDateTimeField("localDateTimeOnDescendents", descendentsModel)
        {
            @Override
            protected void onInitialize()
            {
                super.onInitialize();

                getTimeField().getHoursField().add(new AjaxFormComponentUpdatingBehavior("change")
                {
                    @Override
                    protected void onUpdate(AjaxRequestTarget target)
                    {
                        // IModel#setObject is a no-op for the model used for the hours field, so use the converted input instead of the model object.
                        // And use it update the LocalDateTimeField manually.
                        descendentsModel.setObject(descendentsModel.getObject().withHour(((FormComponent<Integer>)getComponent()).getConvertedInput()));
                        target.add(descendentsData);
                    }
                });
                getTimeField().getMinutesField().add(new AjaxFormComponentUpdatingBehavior("change")
                {
                    @Override
                    protected void onUpdate(AjaxRequestTarget target)
                    {
                        // IModel#setObject is a no-op for the model used for the minutes field, so use the converted input instead of the model object.
                        // And use it update the LocalDateTimeField manually.
                        descendentsModel.setObject(descendentsModel.getObject().withMinute(((FormComponent<Integer>)getComponent()).getConvertedInput()));
                        target.add(descendentsData);
                    }
                });
                getTimeField().getAmOrPmChoice().add(new AjaxFormComponentUpdatingBehavior("change")
                {
                    @Override
                    protected void onUpdate(AjaxRequestTarget target)
                    {
                        // IModel#setObject is a no-op for the model used for the AM/PM field, so use the converted input instead of the model object.
                        // And use it update the LocalDateTimeField manually.
                        switch (((FormComponent<TimeField.AM_PM>)getComponent()).getConvertedInput())
                        {
                            case AM -> descendentsModel.setObject(descendentsModel.getObject().withHour(descendentsModel.getObject().getHour() / 12));
                            case PM ->
                            {
                                var hour = descendentsModel.getObject().getHour();
                                descendentsModel.setObject(descendentsModel.getObject().withHour(hour < 12 ? hour + 12 : hour));
                            }
                        }
                        target.add(descendentsData);
                    }
                });
            }

            @Override
            protected LocalDateTextField newDateField(String id, IModel<LocalDate> dateFieldModel)
            {
                var dateField = super.newDateField(id, dateFieldModel);
                dateField.add(new AjaxFormComponentUpdatingBehavior("input change")
                {
                    @Override
                    protected void onUpdate(AjaxRequestTarget target)
                    {
                        // IModel#setObject is a no-op for the model used for the date field, so use the converted input instead of the model object.
                        var date = ((LocalDateTextField)getComponent()).getConvertedInput();
                        // And use it update the LocalDateTimeField manually.
                        descendentsModel.setObject(descendentsModel.getObject()
                                .withYear(date.getYear())
                                .withMonth(date.getMonthValue())
                                .withDayOfMonth(date.getDayOfMonth()));
                        target.add(descendentsData);
                    }
                });
                return dateField;
            }
        };
        ajaxOnDescendents.setRequired(true);

        form.add(
                ajaxOnDescendents,
                descendentsData.add(
                        new Label("descendentsDate", descendentsModel.map(LocalDateTime::toLocalDate)),
                        new Label("descendentsHour", descendentsModel.map(LocalDateTime::getHour)),
                        new Label("descendentsMinute", descendentsModel.map(LocalDateTime::getMinute))
                )
        );
    }

    private void addLocalDateTimeOnPanel(Form<Object> form, LocalDateTime now)
    {
        var panelModel = Model.of(now);

        var panelData = new WebMarkupContainer("localDateTimePanelValue");
        panelData.setOutputMarkupId(true);

        var ajaxOnPanel = new LocalDateTimeField("localDateTimeOnPanel", panelModel)
        {
            @Override
            protected TimeField newTimeField(String id, IModel<LocalTime> timeFieldModel)
            {
                var timeField = super.newTimeField(id, timeFieldModel);
                timeField.setMetaData(WANT_CHILDREN_TO_PROCESS_INPUT_IN_AJAX_UPDATE, true);
                return timeField;
            }
        };
        ajaxOnPanel.setRequired(true);
        ajaxOnPanel.setMetaData(WANT_CHILDREN_TO_PROCESS_INPUT_IN_AJAX_UPDATE, true);
        // Unfortunately 2 events for <input type="number">. See documentation of WANT_CHILDREN_TO_PROCESS_INPUT_IN_AJAX_UPDATE.
        ajaxOnPanel.add(new AjaxFormComponentUpdatingBehavior("input change")
        {
            @Override
            protected void onUpdate(AjaxRequestTarget target)
            {
                target.add(panelData);
            }
        });

        form.add(
                ajaxOnPanel,
                panelData.add(
                        new Label("panelDate", panelModel.map(LocalDateTime::toLocalDate)),
                        new Label("panelHour", panelModel.map(LocalDateTime::getHour)),
                        new Label("panelMinute", panelModel.map(LocalDateTime::getMinute))
                )
        );
    }

    private void addMultiplyOnPanel(Form<Object> form)
    {
        var panelModel = Model.of(0);

        var panelValue = new Label("multiplyPanelValue", panelModel);
        panelValue.setOutputMarkupId(true);

        var ajaxOnPanel = new Multiply("multiplyOnPanel", panelModel);
        ajaxOnPanel.setMetaData(WANT_CHILDREN_TO_PROCESS_INPUT_IN_AJAX_UPDATE, true);
        ajaxOnPanel.add(new AjaxFormComponentUpdatingBehavior("input change")
        {
            @Override
            protected void onUpdate(AjaxRequestTarget target)
            {
                target.add(panelValue);
            }
        });

        form.add(ajaxOnPanel, panelValue);
    }

    private void addPaletteOnDescendents(Form<Object> form)
    {
        var descendentsModel = new ListModel<>(new ArrayList<Person>());

        var descendentsValue = new Label("paletteDescendentsValue", descendentsModel.map(people -> people.stream().map(Person::getFullName).collect(Collectors.joining(", "))));
        descendentsValue.setOutputMarkupId(true);

        var persons = ComponentReferenceApplication.getPersons();
        var renderer = new ChoiceRenderer<>("fullName", "fullName");
        var ajaxOnDescendents = new Palette<>("paletteOnDescendents", descendentsModel, new CollectionModel<>(persons), renderer, 10, true, true)
        {
            @Override
            protected Recorder<Person> newRecorderComponent()
            {
                var recorder = super.newRecorderComponent();
                recorder.add(new AjaxFormComponentUpdatingBehavior("change")
                {
                    @Override
                    protected void onUpdate(AjaxRequestTarget target)
                    {
                        processInput();
                        target.add(descendentsValue);
                    }
                });
                return recorder;
            }
        };
        ajaxOnDescendents.add(new DefaultTheme());

        form.add(ajaxOnDescendents, descendentsValue);
    }

    private void addPaletteOnPanel(Form<Object> form)
    {
        var panelModel = new ListModel<>(new ArrayList<Person>());

        var panelValue = new Label("palettePanelValue", panelModel.map(people -> people.stream().map(Person::getFullName).collect(Collectors.joining(", "))));
        panelValue.setOutputMarkupId(true);

        var persons = ComponentReferenceApplication.getPersons();
        var renderer = new ChoiceRenderer<>("fullName", "fullName");
        var ajaxOnPanel = new Palette<>("paletteOnPanel", panelModel, new CollectionModel<>(persons), renderer, 10, true, true);
        ajaxOnPanel.add(new DefaultTheme());
        ajaxOnPanel.setMetaData(WANT_CHILDREN_TO_PROCESS_INPUT_IN_AJAX_UPDATE, true);
        ajaxOnPanel.add(new AjaxFormComponentUpdatingBehavior("change")
        {
            @Override
            protected void onUpdate(AjaxRequestTarget target)
            {
                target.add(panelValue);
            }
        });

        form.add(ajaxOnPanel, panelValue);
    }

    private static void addGroupsAndChoicesOnPanel(Form<Object> form)
    {
        var groupsAndChoicesModel = Model.of(new GroupsAndChoicesValues());
        var groupsAndChoicesValues = new Label("groupsAndChoicesValues", groupsAndChoicesModel.map(groupsAndChoices ->
                "CheckGroup: " +
                        (groupsAndChoices.getCheckGroup().isEmpty()
                                ? "-"
                                : groupsAndChoices.getCheckGroup().stream().map(String::valueOf).collect(Collectors.joining(", "))) +
                        ". RadioGroup: " +
                        (groupsAndChoices.getRadioGroup() == null ? "-" : groupsAndChoices.getRadioGroup().toString()) +
                        ". CheckBoxMultipleChoice: " +
                        (groupsAndChoices.getCheckBoxMultiple().isEmpty()
                                ? "-"
                                : groupsAndChoices.getCheckBoxMultiple().stream().map(String::valueOf).collect(Collectors.joining(", "))) +
                        ". RadioChoice: " +
                        (groupsAndChoices.getRadioChoice() == null ? "-" : groupsAndChoices.getRadioChoice().toString())));
        groupsAndChoicesValues.setOutputMarkupId(true);

        var groupsAndChoices = new GroupsAndChoicesPanel("groupsAndChoices", groupsAndChoicesModel);
        groupsAndChoices.setMetaData(WANT_CHILDREN_TO_PROCESS_INPUT_IN_AJAX_UPDATE, true);
        groupsAndChoices.add(new AjaxFormComponentUpdatingBehavior("change")
        {
            @Override
            protected void onUpdate(AjaxRequestTarget target)
            {
                target.add(groupsAndChoicesValues);
            }
        });

        form.add(groupsAndChoices, groupsAndChoicesValues);
    }
}
