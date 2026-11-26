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

package org.apache.wicket.migration;

import org.junit.jupiter.api.Test;
import org.openrewrite.ExecutionContext;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.config.Environment;
import org.openrewrite.maven.MavenExecutionContextView;
import org.openrewrite.maven.tree.MavenRepository;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import java.net.URISyntaxException;
import java.util.List;

import static org.openrewrite.java.Assertions.mavenProject;
import static org.openrewrite.maven.Assertions.pomXml;

/**
 * Resolves the Wicket artifacts from the {@code maven-repository} fixture next to this class
 * only, so the outcome does not depend on what Maven Central or the local repository contain.
 */
class MigrateToWicket11Test implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec
                .executionContext(fixtureRepositoryOnly())
                .recipe(Environment.builder()
                        .scanRuntimeClasspath()
                        .build()
                        .activateRecipes("org.apache.wicket.MigrateToWicket11"));
    }

    private static ExecutionContext fixtureRepositoryOnly() {
        String uri;
        try {
            uri = MigrateToWicket11Test.class.getResource("maven-repository").toURI().toString();
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }

        ExecutionContext ctx = new InMemoryExecutionContext(t -> {
            throw new AssertionError(t);
        });
        MavenExecutionContextView.view(ctx)
                .setAddCentralRepository(false)
                .setAddLocalRepository(false)
                .setRepositories(List.of(MavenRepository.builder()
                        .id("fixture")
                        .uri(uri)
                        .knownToExist(true)
                        .build()));
        return ctx;
    }

    @Test
    void migrateDependencies() {
        //language=xml
        rewriteRun(
                mavenProject("any-project",
                        pomXml("""
                                        <project>
                                            <modelVersion>4.0.0</modelVersion>
                                            <groupId>com.example</groupId>
                                            <artifactId>wicket</artifactId>
                                            <version>1.0.0</version>
                                            <dependencies>
                                                <dependency>
                                                    <groupId>org.apache.wicket</groupId>
                                                    <artifactId>wicket-core</artifactId>
                                                    <version>10.12.0</version>
                                                </dependency>
                                                <dependency>
                                                    <groupId>org.apache.wicket.experimental.wicket10</groupId>
                                                    <artifactId>wicket-metrics</artifactId>
                                                    <version>10.12.0</version>
                                                </dependency>
                                            </dependencies>
                                        </project>
                                        """,
                                """
                                        <project>
                                            <modelVersion>4.0.0</modelVersion>
                                            <groupId>com.example</groupId>
                                            <artifactId>wicket</artifactId>
                                            <version>1.0.0</version>
                                            <properties>
                                                <maven.compiler.release>21</maven.compiler.release>
                                            </properties>
                                            <dependencies>
                                                <dependency>
                                                    <groupId>org.apache.wicket</groupId>
                                                    <artifactId>wicket-core</artifactId>
                                                    <version>11.0.0</version>
                                                </dependency>
                                                <dependency>
                                                    <groupId>org.apache.wicket.experimental.wicket11</groupId>
                                                    <artifactId>wicket-metrics</artifactId>
                                                    <version>11.0.0</version>
                                                </dependency>
                                            </dependencies>
                                        </project>
                                        """
                        )
                )
        );
    }

}
