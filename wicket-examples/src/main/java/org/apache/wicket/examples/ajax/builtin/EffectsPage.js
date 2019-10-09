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
EffectsPage = {};
EffectsPage.animatedCountHide = function (countComponentSelector) {
    var notify = Wicket.Ajax.Call.suspend(); // suspend the prepend JS until animation finished
    jQuery(countComponentSelector).hide("fade", function () { // hide with fade effect
        EffectsPage.delayedFunction1(); // Example of acquiring multiple locks
        notify(); // animation is finished, release to allow the element to be replaced
    });
};
EffectsPage.animatedCountShow = function (countComponentSelector) {
    jQuery(countComponentSelector).removeClass("is-hidden").css("display", "none").show("drop"); // show with drop effect
};
EffectsPage.delayedFunction1 = function () {
    var notify = Wicket.Ajax.Call.suspend();
    setTimeout(function () {
        EffectsPage.delayedFunction2(); // call another function acquiring another lock
        notify();
    }, 100);
};
EffectsPage.delayedFunction2 = function () {
    var notify = Wicket.Ajax.Call.suspend();
    setTimeout(function () {
        notify(); // finally release
    }, 200);
};