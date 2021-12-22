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

module org.apache.wicket.core {
    requires java.instrument;
    requires java.desktop;
    requires java.sql;
    requires org.apache.wicket.util;
    requires org.apache.wicket.request;
    requires org.apache.commons.io;
    requires org.apache.commons.collections4;
    requires commons.fileupload2;
    requires org.slf4j;
    requires jakarta.servlet;
    requires org.danekja.jdk.serializable.functional;
    requires com.github.openjson;
    requires org.junit.jupiter.api;

    provides org.apache.wicket.IInitializer with org.apache.wicket.Initializer;
    provides org.apache.wicket.resource.FileSystemPathService with org.apache.wicket.resource.FileSystemJarPathService;

    exports org.apache.wicket;
    exports org.apache.wicket.ajax;
    exports org.apache.wicket.ajax.attributes;
    exports org.apache.wicket.ajax.form;
    exports org.apache.wicket.ajax.json;
    exports org.apache.wicket.ajax.markup.html;
    exports org.apache.wicket.ajax.markup.html.form;
    exports org.apache.wicket.ajax.markup.html.navigation.paging;
    exports org.apache.wicket.application;
    exports org.apache.wicket.authentication;
    exports org.apache.wicket.authentication.strategy;
    exports org.apache.wicket.authorization;
    exports org.apache.wicket.authorization.strategies;
    exports org.apache.wicket.authorization.strategies.action;
    exports org.apache.wicket.authorization.strategies.page;
    exports org.apache.wicket.behavior;
    exports org.apache.wicket.coep;
    exports org.apache.wicket.coop;
    exports org.apache.wicket.core.random;
    exports org.apache.wicket.core.request;
    exports org.apache.wicket.core.request.handler;
    exports org.apache.wicket.core.request.handler.logger;
    exports org.apache.wicket.core.request.mapper;
    exports org.apache.wicket.core.util.crypt;
    exports org.apache.wicket.core.util.file;
    exports org.apache.wicket.core.util.lang;
    exports org.apache.wicket.core.util.objects.checker;
    exports org.apache.wicket.core.util.resource;
    exports org.apache.wicket.core.util.resource.locator;
    exports org.apache.wicket.core.util.resource.locator.caching;
    exports org.apache.wicket.core.util.string;
    exports org.apache.wicket.core.util.string.interpolator;
    exports org.apache.wicket.core.util.watch;
    exports org.apache.wicket.csp;
    exports org.apache.wicket.css;
    exports org.apache.wicket.event;
    exports org.apache.wicket.feedback;
    exports org.apache.wicket.javascript;
    exports org.apache.wicket.markup;
    exports org.apache.wicket.markup.head;
    exports org.apache.wicket.markup.head.filter;
    exports org.apache.wicket.markup.head.internal;
    exports org.apache.wicket.markup.html;
    exports org.apache.wicket.markup.html.basic;
    exports org.apache.wicket.markup.html.border;
    exports org.apache.wicket.markup.html.debug;
    exports org.apache.wicket.markup.html.form;
    exports org.apache.wicket.markup.html.form.upload;
    exports org.apache.wicket.markup.html.form.validation;
    exports org.apache.wicket.markup.html.image;
    exports org.apache.wicket.markup.html.image.resource;
    exports org.apache.wicket.markup.html.include;
    exports org.apache.wicket.markup.html.internal;
    exports org.apache.wicket.markup.html.link;
    exports org.apache.wicket.markup.html.list;
    exports org.apache.wicket.markup.html.media;
    exports org.apache.wicket.markup.html.media.audio;
    exports org.apache.wicket.markup.html.media.video;
    exports org.apache.wicket.markup.html.navigation.paging;
    exports org.apache.wicket.markup.html.pages;
    exports org.apache.wicket.markup.html.panel;
    exports org.apache.wicket.markup.loader;
    exports org.apache.wicket.markup.parser;
    exports org.apache.wicket.markup.parser.filter;
    exports org.apache.wicket.markup.renderStrategy;
    exports org.apache.wicket.markup.repeater;
    exports org.apache.wicket.markup.repeater.data;
    exports org.apache.wicket.markup.repeater.util;
    exports org.apache.wicket.markup.resolver;
    exports org.apache.wicket.markup.transformer;
    exports org.apache.wicket.mock;
    exports org.apache.wicket.model;
    exports org.apache.wicket.model.util;
    exports org.apache.wicket.page;
    exports org.apache.wicket.pageStore;
    exports org.apache.wicket.pageStore.crypt;
    exports org.apache.wicket.pageStore.disk;
    exports org.apache.wicket.protocol.http;
    exports org.apache.wicket.protocol.http.mock;
    exports org.apache.wicket.protocol.http.request;
    exports org.apache.wicket.protocol.http.servlet;
    exports org.apache.wicket.protocol.https;
    exports org.apache.wicket.request.component;
    exports org.apache.wicket.request.cycle;
    exports org.apache.wicket.request.handler.render;
    exports org.apache.wicket.request.handler.resource;
    exports org.apache.wicket.request.resource;
    exports org.apache.wicket.request.resource.caching;
    exports org.apache.wicket.request.resource.caching.version;
    exports org.apache.wicket.resource;
    exports org.apache.wicket.resource.bundles;
    exports org.apache.wicket.resource.loader;
    exports org.apache.wicket.response;
    exports org.apache.wicket.response.filter;
    exports org.apache.wicket.serialize;
    exports org.apache.wicket.serialize.java;
    exports org.apache.wicket.session;
    exports org.apache.wicket.settings;
    exports org.apache.wicket.util.cookies;
    exports org.apache.wicket.util.image;
    exports org.apache.wicket.util.reference;
    exports org.apache.wicket.util.template;
    exports org.apache.wicket.util.tester;
    exports org.apache.wicket.validation;
    exports org.apache.wicket.validation.validator;
}
