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

var WicketPreview = {
  require: function(libraryName) {
    // inserting via DOM fails in Safari 2.0, so brute force approach
    document.write('<script type="text/javascript" src="' + libraryName + '"></script>');
  },
  findJavascriptPath: function() {
    var scripts = document.getElementsByTagName("script");
    for (var x = 0; x < scripts.length; x++) {
      var s = scripts[x];
      if (s.src && s.src.match(/wicket-preview\.js(\?.*)?$/)) {
        return s.src.replace(/wicket-preview\.js(\?.*)?$/,'');
      }
    }
  },
  load: function() {
    var path = WicketPreview.findJavascriptPath();
    var includes = 'behaviour,dojo,wicket-preview-behaviour'.split(',');
    for (var x = 0; x < includes.length; x++) {
      var include = includes[x];
      WicketPreview.require(path + include + '.js');
    }
  }
}

WicketPreview.load();


