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
dojo.require("dojo.io.*");

function insertPreview(element, url) {
  dojo.io.bind({
    url: url,
    load: function(type, data, evt) { 
      if (url.indexOf('/') != -1) {
        var path = url.substring(0, url.lastIndexOf('/') + 1);
        data = data.replace(/wicket:preview=\"/g, "wicket:preview=\"" + path);
      }
      element.innerHTML = data;

      //remove attribute to prevent infinite recursion
      element.removeAttribute('wicket:preview');

      //apply rules to newly included components
      Behaviour.apply();
    },
    mimetype: "text/plain"
  });
}

var myrules = {
  'div': function(element) {
    var url = element.getAttribute('wicket:preview');
    if (url) {
      insertPreview(element, url);
    }
  }
};

Behaviour.register(myrules);



