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

module org.apache.wicket.util {
    requires java.base;
    requires java.management;
    requires java.sql;
    requires java.xml;
    requires java.desktop;
    requires org.apache.commons.io;
    requires org.apache.commons.collections4;
    requires commons.fileupload2;
    requires org.slf4j;
    requires jakarta.servlet;
    requires org.junit.jupiter.api;

    exports org.apache.wicket.util;
    exports org.apache.wicket.util.collections;
    exports org.apache.wicket.util.convert;
    exports org.apache.wicket.util.convert.converter;
    exports org.apache.wicket.util.crypt;
    exports org.apache.wicket.util.diff;
    exports org.apache.wicket.util.diff.myers;
    exports org.apache.wicket.util.encoding;
    exports org.apache.wicket.util.file;
    exports org.apache.wicket.util.io;
    exports org.apache.wicket.util.lang;
    exports org.apache.wicket.util.license;
    exports org.apache.wicket.util.listener;
    exports org.apache.wicket.util.markup.xhtml;
    exports org.apache.wicket.util.parse.metapattern;
    exports org.apache.wicket.util.parse.metapattern.parsers;
    exports org.apache.wicket.util.resource;
    exports org.apache.wicket.util.string;
    exports org.apache.wicket.util.string.interpolator;
    exports org.apache.wicket.util.thread;
    exports org.apache.wicket.util.time;
    exports org.apache.wicket.util.value;
    exports org.apache.wicket.util.visit;
    exports org.apache.wicket.util.watch;
    exports org.apache.wicket.util.xml;

    // temporary hack until CDI-Unit and Spring provide Jakarta EE based releases
    exports javax.servlet;
    exports javax.servlet.http;
}
