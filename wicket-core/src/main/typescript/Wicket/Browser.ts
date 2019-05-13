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

export var Browser;
Browser = {
    _isKHTML: null,
    isKHTML: function () {
        let wb = Browser;
        if (wb._isKHTML === null) {
            wb._isKHTML = (/Konqueror|KHTML/).test(window.navigator.userAgent) && !/Apple/.test(window.navigator.userAgent);
        }
        return wb._isKHTML;
    },

    _isSafari: null,
    isSafari: function () {
        let wb = Browser;
        if (wb._isSafari === null) {
            wb._isSafari = !/Chrome/.test(window.navigator.userAgent) && /KHTML/.test(window.navigator.userAgent) && /Apple/.test(window.navigator.userAgent);
        }
        return wb._isSafari;
    },

    _isChrome: null,
    isChrome: function () {
        let wb = Browser;
        if (wb._isChrome === null) {
            wb._isChrome = (/KHTML/).test(window.navigator.userAgent) && /Apple/.test(window.navigator.userAgent) && /Chrome/.test(window.navigator.userAgent);
        }
        return wb._isChrome;
    },

    _isOpera: null,
    isOpera: function () {
        let wb = Browser;
        if (wb._isOpera === null) {
            wb._isOpera = !Browser.isSafari() && typeof (window["opera"]) !== "undefined";
        }
        return wb._isOpera;
    },

    _isIE: null,
    isIE: function () {
        let wb = Browser;
        if (wb._isIE === null) {
            wb._isIE = !Browser.isSafari() && (typeof (document.all) !== "undefined" || window.navigator.userAgent.indexOf("Trident/") > -1) && typeof (window["opera"]) === "undefined";
        }
        return wb._isIE;
    },

    _isIEQuirks: null,
    isIEQuirks: function () {
        let wb = Browser;
        if (wb._isIEQuirks === null) {
            // is the browser internet explorer in quirks mode (we could use document.compatMode too)
            wb._isIEQuirks = Browser.isIE() && window.document.documentElement.clientHeight === 0;
        }
        return wb._isIEQuirks;
    },

    _isIELessThan9: null,
    isIELessThan9: function () {
        let wb = Browser;
        if (wb._isIELessThan9 === null) {
            let index = window.navigator.userAgent.indexOf("MSIE");
            let version = parseFloat(window.navigator.userAgent.substring(index + 5));
            wb._isIELessThan9 = Browser.isIE() && version < 9;
        }
        return wb._isIELessThan9;
    },

    _isIELessThan11: null,
    isIELessThan11: function () {
        let wb = Browser;
        if (wb._isIELessThan11 === null) {
            wb._isIELessThan11 = !Browser.isSafari() && typeof (document.all) !== "undefined" && typeof (window["opera"]) === "undefined";
        }
        return wb._isIELessThan11;
    },

    _isIE11: null,
    isIE11: function () {
        let wb = Browser;
        if (wb._isIE11 === null) {
            let userAgent = window.navigator.userAgent;
            let isTrident = userAgent.indexOf("Trident") > -1;
            let is11 = userAgent.indexOf("rv:11") > -1;
            wb._isIE11 = isTrident && is11;
        }
        return wb._isIE11;
    },

    _isGecko: null,
    isGecko: function () {
        let wb = Browser;
        if (wb._isGecko === null) {
            wb._isGecko = (/Gecko/).test(window.navigator.userAgent) && !Browser.isSafari();
        }
        return wb._isGecko;
    }
};