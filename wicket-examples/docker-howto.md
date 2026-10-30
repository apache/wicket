<!--
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
-->
# How to update the Docker image used for the demo

## Build 

```
docker build --tag apache.jfrog.io/wicket-docker/wicket-examples:LATEST-10 .
```

## Push

1. Login

```
docker login apache.jfrog.io
```
Enter your ASF id and password when prompted.

2. Push

```
docker push apache.jfrog.io/wicket-docker/wicket-examples:LATEST-10
```

3. Optional: You could verify at https://apache.jfrog.io/ui/packages/docker:%2F%2Fwicket-examples/LATEST-10?projectKey=wicket