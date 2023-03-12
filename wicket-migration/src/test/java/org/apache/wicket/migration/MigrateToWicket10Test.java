/*
 * Copyright (c) 2010-2023. wicket Framework
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.wicket.migration;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openrewrite.config.Environment;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.openrewrite.java.Assertions.java;
import static org.openrewrite.java.Assertions.mavenProject;
import static org.openrewrite.maven.Assertions.pomXml;

class MigrateToWicket10Test implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec
                .parser(JavaParser.fromJavaVersion()
                        .logCompilationWarningsAndErrors(true)
                        .classpath("rewrite-migrate-java"))
                .recipe(Environment.builder()
                        .scanRuntimeClasspath()
                        .build()
                        .activateRecipes("org.apache.wicket.MigrateToWicket10"));
    }

    @Test
    void migrateImports() {
        //language=java
        rewriteRun(
                java("""
                        package org.apache.wicket.http2.markup.head;

                        public class PushHeaderItem { }"""),
                java(
                        """
                                package sample.wicket;
                                                                
                                import org.apache.wicket.http2.markup.head.PushHeaderItem;
                                                                
                                class ATest {
                                   PushHeaderItem item;
                                }""",
                        """
                                package sample.wicket;
                                
                                import org.apache.wicket.markup.head.http2.PushHeaderItem;
                                
                                class ATest {
                                   PushHeaderItem item;
                                }"""
                ));
    }

    @Test
    @Disabled("Required a first release of 10.x")
    void migrateDependencies() {
        //language=xml
        rewriteRun(
                mavenProject("any-project",
                        pomXml(

                                """
                                            <project>
                                                <modelVersion>4.0.0</modelVersion>
                                                <groupId>com.example</groupId>
                                                <artifactId>wicket</artifactId>
                                                <version>1.0.0</version>
                                                <dependencies>
                                                    <dependency>
                                                        <groupId>org.apache.wicket</groupId>
                                                        <artifactId>wicket-core</artifactId>
                                                        <version>9.12.0</version>
                                                    </dependency>
                                                    <dependency>
                                                        <groupId>org.apache.wicket.experimental.wicket9</groupId>
                                                        <artifactId>wicket-http2-core</artifactId>
                                                        <version>0.23</version>
                                                    </dependency>
                                                </dependencies>
                                            </project>
                                        """,
                                spec -> spec.after(pom -> {
                                    Matcher version = Pattern.compile("10\\..+").matcher(pom);
                                    assertThat(version.find()).describedAs("Expected 10.x in %s", pom).isTrue();
                                    return String.format("""
                                                <project>
                                                    <modelVersion>4.0.0</modelVersion>
                                                    <groupId>com.example</groupId>
                                                    <artifactId>wicket</artifactId>
                                                    <version>1.0.0</version>
                                                    <dependencies>
                                                        <dependency>
                                                            <groupId>org.apache.wicket</groupId>
                                                            <artifactId>wicket-core</artifactId>
                                                            <version>%s</version>
                                                        </dependency>
                                                    </dependencies>
                                                </project>
                                            """, version.group(0));
                                }))));
    }

}
