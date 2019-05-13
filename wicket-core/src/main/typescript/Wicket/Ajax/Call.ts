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

import {$, bind, htmlToDomDocument, redirect, isUndef, jQuery} from "../WicketUtils";
import {Event} from "../Event";
import * as Form from "../Form";
import * as DOM from "../DOM";
import * as Xml from "../Xml";
import * as Head from "../Head";
import {channelManager} from "../ChannelManager";
import {Browser} from "../Browser";
import {FunctionsExecuter} from "../FunctionsExecuter";
import {Focus} from "../Focus";
import {Log} from "../Log";
import {Ajax} from "../Ajax";

/**
 * Ajax call fires a Wicket Ajax request and processes the response.
 * The response can contain
 *   - javascript that should be invoked
 *   - body of components being replaced
 *   - header contributions of components
 *   - a redirect location
 */
export class Call {

    constructor() {

    }

    /**
     * Initializes the default values for Ajax request attributes.
     * The defaults are not set at the server side to save some bytes
     * for the network transfer
     *
     * @param attrs {Object} - the ajax request attributes to enrich
     * @private
     */
    private _initializeDefaults (attrs) {

        // (ajax channel)
        if (typeof(attrs.ch) !== 'string') {
            attrs.ch = '0|s';
        }

        // (wicketAjaxResponse) be default the Ajax result should be processed for <ajax-response>
        if (typeof(attrs.wr) !== 'boolean') {
            attrs.wr = true;
        }

        // (dataType) by default we expect XML responses from the Ajax behaviors
        if (typeof(attrs.dt) !== 'string') {
            attrs.dt = 'xml';
        }

        if (typeof(attrs.m) !== 'string') {
            attrs.m = 'GET';
        }

        if (attrs.async !== false) {
            attrs.async = true;
        }

        if (!jQuery.isNumeric(attrs.rt)) {
            attrs.rt = 0;
        }

        if (attrs.pd !== true) {
            attrs.pd = false;
        }

        if (!attrs.sp) {
            attrs.sp = "bubble";
        }

        if (!attrs.sr) {
            attrs.sr = false;
        }
    }

    /**
     * Extracts the HTML element that "caused" this Ajax call.
     * An Ajax call is usually caused by JavaScript event but maybe be also
     * caused by manual usage of the JS API..
     *
     * @param attrs {Object} - the ajax request attributes
     * @return {HTMLElement} - the DOM element
     * @private
     */
    private _getTarget (attrs) {
        let target;
        if (attrs.event) {
            target = attrs.event.target;
        } else if (!jQuery.isWindow(attrs.c)) {
            target = $(attrs.c);
        } else {
            target = window;
        }
        return target;
    }

    /**
     * A helper function that executes an array of handlers (before, success, failure)
     * (note: it isn't marked as private because it's used in another class of Wicket)
     *
     * @param handlers {Array[Function]} - the handlers to execute
     * @package
     */
    _executeHandlers (handlers, ... args) {
        if (jQuery.isArray(handlers)) {

            // cut the handlers argument
            // let args = Array.prototype.slice.call(arguments).slice(1);

            // assumes that the Ajax attributes is always the first argument
            let attrs = args[0];
            let that = this._getTarget(attrs);

            for (let i = 0; i < handlers.length; i++) {
                let handler = handlers[i];
                if (jQuery.isFunction(handler)) {
                    handler.apply(that, args);
                } else {
                    new Function(handler).apply(that, args);
                }
            }
        }
    }

    /**
     * Converts an object (hash) to an array suitable for consumption
     * by jQuery.param()
     *
     * @param {Object} parameters - the object to convert to an array of
     *      name -> value pairs.
     * @see jQuery.param
     * @see jQuery.serializeArray
     * @private
     */
    private _asParamArray (parameters) {
        let result = [],
            value,
            name;
        if (jQuery.isArray(parameters)) {
            result = parameters;
        }
        else if (jQuery.isPlainObject(parameters)) {
            for (name in parameters) {
                if (name && parameters.hasOwnProperty(name)) {
                    value = parameters[name];
                    result.push({name: name, value: value});
                }
            }
        }

        for (let i = 0; i < result.length; i++) {
            if (result[i] === null) {
                result.splice(i, 1);
                i--;
            }
        }

        return result;
    }

    /**
     * Executes all functions to calculate any dynamic extra parameters
     *
     * @param attrs The Ajax request attributes
     * @returns {String} A query string snippet with any calculated request
     *  parameters. An empty string if there are no dynamic parameters in attrs
     * @private
     */
    private _calculateDynamicParameters (attrs) {
        let deps = attrs.dep,
            params = [];

        for (let i = 0; i < deps.length; i++) {
            let dep = deps[i],
                extraParam;
            if (jQuery.isFunction(dep)) {
                extraParam = dep(attrs);
            } else {
                extraParam = new Function('attrs', dep)(attrs);
            }
            extraParam = this._asParamArray(extraParam);
            params = params.concat(extraParam);
        }
        return params;
    }

    /**
     * Executes or schedules for execution #doAjax()
     *
     * @param {Object} attrs - the Ajax request attributes configured at the server side
     */
    public ajax (attrs): any {
        this._initializeDefaults(attrs);

        let res = channelManager.schedule(attrs.ch, bind(function () {
            this.doAjax(attrs);
        }, this));
        return res !== null ? res: true;
    }

    /**
     * Is an element still present for Ajax requests.
     */
    private _isPresent (id) {
        if (isUndef(id)) {
            // no id so no check whether present
            return true;
        }

        let element = $(id);
        if (isUndef(element)) {
            // not present
            return false;
        }

        // present if no attributes at all or not a placeholder
        return (!element.hasAttribute || !element.hasAttribute('data-wicket-placeholder'));
    }

    /**
     * Handles execution of Ajax calls.
     *
     * @param {Object} attrs - the Ajax request attributes configured at the server side
     */
    public doAjax (attrs): any {

        let
            // the headers to use for each Ajax request
            headers = {
                'Wicket-Ajax': 'true',
                'Wicket-Ajax-BaseURL': Ajax.getAjaxBaseUrl()
            },

            url = attrs.u,

            // the request (extra) parameters
            data: any = this._asParamArray(attrs.ep),

            self = this,

            // the precondition to use if there are no explicit ones
            defaultPrecondition = [ function (attributes) {
                return self._isPresent(attributes.c) && self._isPresent(attributes.f);
            }],

            // a context that brings the common data for the success/fialure/complete handlers
            context = {
                attrs: attrs,

                // initialize the array for steps (closures that execute each action)
                steps: []
            },
            we = Event,
            topic = we.Topic;

        if (Focus.lastFocusId) {
            // WICKET-6568 might contain non-ASCII
            headers["Wicket-FocusedElementId"] = Form.encode(Focus.lastFocusId);
        }

        self._executeHandlers(attrs.bh, attrs);
        we.publish(topic.AJAX_CALL_BEFORE, attrs);

        let preconditions = attrs.pre || [];
        preconditions = defaultPrecondition.concat(preconditions);
        if (jQuery.isArray(preconditions)) {

            let that = this._getTarget(attrs);

            for (let p = 0; p < preconditions.length; p++) {

                let precondition = preconditions[p];
                let result;
                if (jQuery.isFunction(precondition)) {
                    result = precondition.call(that, attrs);
                } else {
                    result = new Function(precondition).call(that, attrs);
                }
                if (result === false) {
                    Log.info("Ajax request stopped because of precondition check, url: " + attrs.u);
                    self.done(attrs);
                    return false;
                }
            }
        }

        we.publish(topic.AJAX_CALL_PRECONDITION, attrs);

        if (attrs.f) {
            // serialize the form with id == attrs.f
            let form = $(attrs.f);
            data = data.concat(Form.serializeForm(form));

            // set the submitting component input name
            if (attrs.sc) {
                let scName = attrs.sc;
                data = data.concat({name: scName, value: 1});
            }
        } else if (attrs.c && !jQuery.isWindow(attrs.c)) {
            // serialize just the form component with id == attrs.c
            let el = $(attrs.c);
            data = data.concat(Form.serializeElement(el, attrs.sr));
        }

        // collect the dynamic extra parameters
        if (jQuery.isArray(attrs.dep)) {
            let dynamicData = this._calculateDynamicParameters(attrs);
            if (attrs.m.toLowerCase() === 'post') {
                data = data.concat(dynamicData);
            } else {
                let separator = url.indexOf('?') > -1 ? '&' : '?';
                url = url + separator + jQuery.param(dynamicData);
            }
        }

        let wwwFormUrlEncoded; // undefined is jQuery's default
        if (attrs.mp) {
            try {
                let formData = new FormData();
                for (let i = 0; i < data.length; i++) {
                    formData.append(data[i].name, data[i].value || "");
                }

                data = formData;
                wwwFormUrlEncoded = false;
            } catch (exception) {
                Log.error("Ajax multipat not supported:" + exception);
            }
        }

        // execute the request
        let jqXHR = jQuery.ajax({
            url: url,
            type: attrs.m,
            context: self,
            processData: wwwFormUrlEncoded,
            contentType: wwwFormUrlEncoded,

            beforeSend: function (jqXHR, settings) {
                self._executeHandlers(attrs.bsh, attrs, jqXHR, settings);
                we.publish(topic.AJAX_CALL_BEFORE_SEND, attrs, jqXHR, settings);

                if (attrs.i) {
                    // show the indicator
                    DOM.showIncrementally(attrs.i);
                }
            },
            data: data,
            dataType: attrs.dt,
            async: attrs.async,
            timeout: attrs.rt,
            cache: false,
            headers: headers,
            success: function(data, textStatus, jqXHR) {
                if (attrs.wr) {
                    self.processAjaxResponse(data, textStatus, jqXHR, context);
                } else {
                    self._executeHandlers(attrs.sh, attrs, jqXHR, data, textStatus);
                    we.publish(topic.AJAX_CALL_SUCCESS, attrs, jqXHR, data, textStatus);
                }
            },
            error: function(jqXHR, textStatus, errorMessage) {
                if (jqXHR.status === 301 && jqXHR.getResponseHeader('Ajax-Location')) {
                    self.processAjaxResponse(data, textStatus, jqXHR, context);
                } else {
                    self.failure(context, jqXHR, errorMessage, textStatus);
                }
            },
            complete: function (jqXHR, textStatus) {

                context.steps.push(jQuery.proxy(function (notify) {
                    if (attrs.i && (context as any).isRedirecting !== true) {
                        DOM.hideIncrementally(attrs.i);
                    }

                    self._executeHandlers(attrs.coh, attrs, jqXHR, textStatus);
                    we.publish(topic.AJAX_CALL_COMPLETE, attrs, jqXHR, textStatus);

                    self.done(attrs);
                    return FunctionsExecuter.DONE;
                }, self));

                let executer = new FunctionsExecuter(context.steps);
                executer.start();
            }
        });

        // execute after handlers right after the Ajax request is fired
        self._executeHandlers(attrs.ah, attrs);
        we.publish(topic.AJAX_CALL_AFTER, attrs);

        return jqXHR;
    }

    /**
     * Method that processes a manually supplied <ajax-response>.
     *
     * @param data {XmlDocument} - the <ajax-response> XML document
     */
    public process (data): void {
        let context =  {
            attrs: {},
            steps: []
        };
        let xmlDocument = Xml.parse(data);
        this.loadedCallback(xmlDocument, context);
        let executer = new FunctionsExecuter(context.steps);
        executer.start();
    }

    /**
     * Method that processes the <ajax-response> in the context of an XMLHttpRequest.
     *
     * @param data {XmlDocument} - the <ajax-response> XML document
     * @param textStatus {String} - the response status as text (e.g. 'success', 'parsererror', etc.)
     * @param jqXHR {Object} - the jQuery wrapper around XMLHttpRequest
     * @param context {Object} - the request context with the Ajax request attributes and the FunctionExecuter's steps
     */
    processAjaxResponse (data, textStatus, jqXHR, context): void {

        if (jqXHR.readyState === 4) {

            // first try to get the redirect header
            let redirectUrl;
            try {
                redirectUrl = jqXHR.getResponseHeader('Ajax-Location');
            } catch (ignore) { // might happen in older mozilla
            }

            // the redirect header was set, go to new url
            if (typeof(redirectUrl) !== "undefined" && redirectUrl !== null && redirectUrl !== "") {

                // In case the page isn't really redirected. For example say the redirect is to an octet-stream.
                // A file download popup will appear but the page in the browser won't change.
                this.success(context);

                let withScheme  = /^[a-z][a-z0-9+.-]*:\/\//;  // checks whether the string starts with a scheme

                // support/check for non-relative redirectUrl like as provided and needed in a portlet context
                if (redirectUrl.charAt(0) === '/' || withScheme.test(redirectUrl)) {
                    context.isRedirecting = true;
                    redirect(redirectUrl);
                }
                else {
                    let urlDepth = 0;
                    while (redirectUrl.substring(0, 3) === "../") {
                        urlDepth++;
                        redirectUrl = redirectUrl.substring(3);
                    }
                    // Make this a string.
                    let calculatedRedirect = window.location.pathname;
                    while (urlDepth > -1) {
                        urlDepth--;
                        let i = calculatedRedirect.lastIndexOf("/");
                        if (i > -1) {
                            calculatedRedirect = calculatedRedirect.substring(0, i);
                        }
                    }
                    calculatedRedirect += "/" + redirectUrl;

                    if (Browser.isGecko()) {
                        // firefox 3 has problem with window.location setting relative url
                        calculatedRedirect = window.location.protocol + "//" + window.location.host + calculatedRedirect;
                    }

                    context.isRedirecting = true;
                    redirect(calculatedRedirect);
                }
            }
            else {
                // no redirect, just regular response
                if (Log.enabled()) {
                    let responseAsText = jqXHR.responseText;
                    Log.info("Received ajax response (" + responseAsText.length + " characters)");
                    Log.info("\n" + responseAsText);
                }

                // invoke the loaded callback with an xml document
                return this.loadedCallback(data, context);
            }
        }
    }

    // Processes the response
    loadedCallback (envelope, context): void {
        // To process the response, we go through the xml document and add a function for every action (step).
        // After this is done, a FunctionExecuter object asynchronously executes these functions.
        // The asynchronous execution is necessary, because some steps might involve loading external javascript,
        // which must be asynchronous, so that it doesn't block the browser, but we also have to maintain
        // the order in which scripts are loaded and we have to delay the next steps until the script is
        // loaded.
        try {
            let root = envelope.getElementsByTagName("ajax-response")[0];

            if (isUndef(root) && envelope.compatMode === 'BackCompat') {
                envelope = htmlToDomDocument(envelope);
                root = envelope.getElementsByTagName("ajax-response")[0];
            }

            // the root element must be <ajax-response
            if (isUndef(root) || root.tagName !== "ajax-response") {
                this.failure(context, null, "Could not find root <ajax-response> element", null);
                return;
            }

            let steps = context.steps;

            // go through the ajax response and execute all priority-invocations first
            for (let i = 0; i < root.childNodes.length; ++i) {
                let childNode = root.childNodes[i];
                if (childNode.tagName === "header-contribution") {
                    this.processHeaderContribution(context, childNode);
                } else if (childNode.tagName === "priority-evaluate") {
                    this.processEvaluation(context, childNode);
                }
            }

            // go through the ajax response and for every action (component, js evaluation, header contribution)
            // ad the proper closure to steps
            let stepIndexOfLastReplacedComponent = -1;
            for (let c = 0; c < root.childNodes.length; ++c) {
                let node = root.childNodes[c];

                if (node.tagName === "component") {
                    if (stepIndexOfLastReplacedComponent === -1) {
                        this.processFocusedComponentMark(context);
                    }
                    stepIndexOfLastReplacedComponent = steps.length;
                    this.processComponent(context, node);
                } else if (node.tagName === "evaluate") {
                    this.processEvaluation(context, node);
                } else if (node.tagName === "redirect") {
                    this.processRedirect(context, node);
                }

            }
            if (stepIndexOfLastReplacedComponent !== -1) {
                this.processFocusedComponentReplaceCheck(steps, stepIndexOfLastReplacedComponent);
            }

            // add the last step, which should trigger the success call the done method on request
            this.success(context);

        } catch (exception) {
            this.failure(context, null, exception, null);
        }
    }

    // Adds a closure to steps that should be invoked after all other steps have been successfully executed
    public success (context): void {
        context.steps.push(jQuery.proxy(function (notify) {
            Log.info("Response processed successfully.");

            let attrs = context.attrs;
            this._executeHandlers(attrs.sh, attrs, null, null, 'success');
            Event.publish(Event.Topic.AJAX_CALL_SUCCESS, attrs, null, null, 'success');

            Focus.requestFocus();

            // continue to next step (which should make the processing stop, as success should be the final step)
            return FunctionsExecuter.DONE;
        }, this));
    }

    // On ajax request failure
    public failure (context, jqXHR, errorMessage, textStatus): void {
        context.steps.push(jQuery.proxy(function (notify) {
            if (errorMessage) {
                Log.error("Wicket.Ajax.Call.failure: Error while parsing response: " + errorMessage);
            }
            let attrs = context.attrs;
            this._executeHandlers(attrs.fh, attrs, jqXHR, errorMessage, textStatus);
            Event.publish(Event.Topic.AJAX_CALL_FAILURE, attrs, jqXHR, errorMessage, textStatus);

            return FunctionsExecuter.DONE;
        }, this));
    }

    public done (attrs): void {
        this._executeHandlers(attrs.dh, attrs);
        Event.publish(Event.Topic.AJAX_CALL_DONE, attrs);

        channelManager.done(attrs.ch);
    }

    // Adds a closure that replaces a component
    public processComponent (context, node): void {
        context.steps.push(function (notify) {
            // get the component id
            let compId = node.getAttribute("id");

            // get existing component
            let element = $(compId);

            if (isUndef(element)) {
                Log.error("Wicket.Ajax.Call.processComponent: Component with id [[" +
                    compId + "]] was not found while trying to perform markup update. " +
                    "Make sure you called component.setOutputMarkupId(true) on the component whose markup you are trying to update.");
            } else {
                let text = DOM.text(node);

                // replace the component
                DOM.replace(element, text);
            }
            // continue to next step
            return FunctionsExecuter.DONE;
        });
    }

    /**
     * Adds a closure that evaluates javascript code.
     * @param context {Object} - the object that brings the executer's steps and the attributes
     * @param node {XmlElement} - the <[priority-]evaluate> element with the script to evaluate
     */
    public processEvaluation (context, node): void {

        // used to match evaluation scripts which manually call FunctionsExecuter's notify() when ready
        let scriptWithIdentifierR = new RegExp("\\(function\\(\\)\\{([a-zA-Z_]\\w*)\\|((.|\\n)*)?\\}\\)\\(\\);$");

        /**
         * A regex used to split the text in (priority-)evaluate elements in the Ajax response
         * when there are scripts which require manual call of 'FunctionExecutor#notify()'
         * @type {RegExp}
         */
        let scriptSplitterR = new RegExp("\\(function\\(\\)\\{[\\s\\S]*?}\\)\\(\\);", 'gi');

        // get the javascript body
        let text = DOM.text(node);

        // aliases to improve performance
        let steps = context.steps;
        let log = Log;

        let evaluateWithManualNotify = function (parameters, body) {
            return function(notify) {
                let toExecute = "(function(" + parameters + ") {" + body + "})";

                try {
                    // do the evaluation in global scope
                    let f = (window as any).eval(toExecute);
                    f(notify);
                } catch (exception) {
                    log.error("Wicket.Ajax.Call.processEvaluation: Exception evaluating javascript: " + exception + ", text: " + text);
                }
                return FunctionsExecuter.ASYNC;
            };
        };

        let evaluate = function (script) {
            return function(notify) {
                // just evaluate the javascript
                try {
                    // do the evaluation in global scope
                    (window as any).eval(script);
                } catch (exception) {
                    log.error("Wicket.Ajax.Call.processEvaluation: Exception evaluating javascript: " + exception + ", text: " + text);
                }
                // continue to next step
                return FunctionsExecuter.DONE;
            };
        };

        // test if the javascript is in form of identifier|code
        // if it is, we allow for letting the javascript decide when the rest of processing will continue
        // by invoking identifier();. This allows usage of some asynchronous/deferred logic before the next script
        // See WICKET-5039
        if (scriptWithIdentifierR.test(text)) {
            let scripts = [];
            let scr;
            while ( (scr = scriptSplitterR.exec(text) ) !== null ) {
                scripts.push(scr[0]);
            }

            for (let s = 0; s < scripts.length; s++) {
                let script = scripts[s];
                if (script) {
                    let scriptWithIdentifier = script.match(scriptWithIdentifierR);
                    if (scriptWithIdentifier) {
                        steps.push(evaluateWithManualNotify(scriptWithIdentifier[1], scriptWithIdentifier[2]));
                    }
                    else {
                        steps.push(evaluate(script));
                    }
                }
            }
        } else {
            steps.push(evaluate(text));
        }
    }

    // Adds a closure that processes a header contribution
    public processHeaderContribution (context, node): void {
        let c = Head.Contributor;
        c.processContribution(context, node);
    }

    // Adds a closure that processes a redirect
    public processRedirect (context, node): void {
        let text = DOM.text(node);
        Log.info("Redirecting to: " + text);
        context.isRedirecting = true;
        redirect(text);
    }

    // mark the focused component so that we know if it has been replaced by response
    public processFocusedComponentMark (context): void {
        context.steps.push(function (notify) {
            Focus.markFocusedComponent();

            // continue to next step
            return FunctionsExecuter.DONE;
        });
    }

    // detect if the focused component was replaced
    public processFocusedComponentReplaceCheck (steps, lastReplaceComponentStep): void {
        // add this step imediately after all components have been replaced
        steps.splice(lastReplaceComponentStep + 1, 0, function (notify) {
            Focus.checkFocusedComponentReplaced();

            // continue to next step
            return FunctionsExecuter.DONE;
        });
    }

}