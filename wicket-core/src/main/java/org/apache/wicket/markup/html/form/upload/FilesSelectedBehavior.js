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

;(function (undefined) {

    'use strict';

    if (typeof(Wicket.FilesSelected) === 'object') {
        return;
    }

    /**
     * Contains JS code for FilesSelectedBehavior.
     */
    Wicket.FilesSelected = {

        // the id of temporary object holding files.
        varId: function (componentId) {
            return componentId + "_files";
        },

        /**
         * Precondition to trigger (or not) server round-trip.
         *
         * @param inputField the file input field
         * @returns {boolean} true if some files (file) were (was) selected,
         */
        precondition : function (inputField) {
            if (inputField.files && inputField.files.length > 0) {
                var id = this.varId(inputField.id);
                Wicket.FilesSelected[id] = inputField.files;
                return true;
            }
            return false;
        },

        /**
         * Collects selected files details
         *
         * @param componentId The id of file upload input field.
         *
         * @returns array with file infos.
         */
        collectFilesDetails: function(componentId) {
            var id = this.varId(componentId);
            var sources = Wicket.FilesSelected[id];
            var files = [];
            for (var i = 0; i < sources.length; i++) {
                var file = sources[i];
                var info = {
                    'fileName': file.name,
                    'fileSize': file.size,
                    'lastModified': file.lastModified,
                    'mimeType': file.type
                }
                files.push(info);
            }
            // clean temporary holder object
            delete(Wicket.FilesSelected[id]);
            // return information about selected files.
            return {'fileInfos': JSON.stringify(files)};
        }
    };
})();