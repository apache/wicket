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

module org.apache.wicket.extensions {
    requires java.desktop;
    requires jakarta.servlet;
    requires org.slf4j;
    requires com.fasterxml.jackson.databind;
    requires com.github.openjson;
    requires commons.fileupload2;
    requires org.apache.wicket.util;
    requires org.apache.wicket.request;
    requires org.apache.wicket.core;
    requires org.danekja.jdk.serializable.functional;

    provides org.apache.wicket.IInitializer with org.apache.wicket.extensions.Initializer;

    exports org.apache.wicket.extensions;
    exports org.apache.wicket.extensions.ajax;
    exports org.apache.wicket.extensions.ajax.markup.html;
    exports org.apache.wicket.extensions.ajax.markup.html.autocomplete;
    exports org.apache.wicket.extensions.ajax.markup.html.form.upload;
    exports org.apache.wicket.extensions.ajax.markup.html.modal;
    exports org.apache.wicket.extensions.ajax.markup.html.modal.theme;
    exports org.apache.wicket.extensions.ajax.markup.html.repeater;
    exports org.apache.wicket.extensions.ajax.markup.html.repeater.data.sort;
    exports org.apache.wicket.extensions.ajax.markup.html.repeater.data.table;
    exports org.apache.wicket.extensions.ajax.markup.html.tabs;
    exports org.apache.wicket.extensions.breadcrumb;
    exports org.apache.wicket.extensions.breadcrumb.panel;
    exports org.apache.wicket.extensions.captcha.kittens;
    exports org.apache.wicket.extensions.markup.html.basic;
    exports org.apache.wicket.extensions.markup.html.captcha;
    exports org.apache.wicket.extensions.markup.html.form;
    exports org.apache.wicket.extensions.markup.html.form.datetime;
    exports org.apache.wicket.extensions.markup.html.form.palette;
    exports org.apache.wicket.extensions.markup.html.form.palette.component;
    exports org.apache.wicket.extensions.markup.html.form.palette.theme;
    exports org.apache.wicket.extensions.markup.html.form.select;
    exports org.apache.wicket.extensions.markup.html.image.resource;
    exports org.apache.wicket.extensions.markup.html.repeater.data.grid;
    exports org.apache.wicket.extensions.markup.html.repeater.data.sort;
    exports org.apache.wicket.extensions.markup.html.repeater.data.table;
    exports org.apache.wicket.extensions.markup.html.repeater.data.table.export;
    exports org.apache.wicket.extensions.markup.html.repeater.data.table.filter;
    exports org.apache.wicket.extensions.markup.html.repeater.tree;
    exports org.apache.wicket.extensions.markup.html.repeater.tree.content;
    exports org.apache.wicket.extensions.markup.html.repeater.tree.nested;
    exports org.apache.wicket.extensions.markup.html.repeater.tree.table;
    exports org.apache.wicket.extensions.markup.html.repeater.tree.theme;
    exports org.apache.wicket.extensions.markup.html.repeater.util;
    exports org.apache.wicket.extensions.markup.html.tabs;
    exports org.apache.wicket.extensions.model;
    exports org.apache.wicket.extensions.rating;
    exports org.apache.wicket.extensions.requestlogger;
    exports org.apache.wicket.extensions.util.encoding;
    exports org.apache.wicket.extensions.validation.validator;
    exports org.apache.wicket.extensions.wizard;
    exports org.apache.wicket.extensions.wizard.dynamic;
}
