Release Notes - Wicket - Version 7.0.0-M2

** Bug

    * [WICKET-4545] - MarkupNotFoundException for Fragment and TransparentWebMarkupContainer
    * [WICKET-5241] - RequestLogger's server duration does not include 'detach duration'
    * [WICKET-5560] - A 404 error occurs when using a CryptoMapper
    * [WICKET-5569] - Unable to find markup for children of deeply nested IComponentResolvers during Ajax response
    * [WICKET-5573] - FilterToolbar generics broken
    * [WICKET-5579] - wicket:enclosure on wicket:extend - Detach called on component with id '0' while it had a non-empty queue
    * [WICKET-5581] - CachingRes​ourceStrea​mLocator is not extension-​aware
    * [WICKET-5582] - ServletWebResponse#encodeUrl() makes absolute Urls relative
    * [WICKET-5589] - Upgrade wicket-atmosphere to the latest version of atmosphere
    * [WICKET-5591] - Missing translation for HoursValidator.range (in DateTimeField)
    * [WICKET-5592] - Add a method to clear the cache of CachingResourceStreamLocator
    * [WICKET-5593] - AjaxFormValidatingBehavior attempts to update non-visible feedback panels
    * [WICKET-5595] - Atmosphere: updates infinitly with long polling transport
    * [WICKET-5596] - DropDownChoice#wantsOnSelectionChangedNotifications(T) not being called on unmounted page
    * [WICKET-5597] - button behaviour changed
    * [WICKET-5598] - AjaxFallbackButton does not call #onError(ART, Form) when JavaScript disabled

** Improvement

    * [WICKET-5574] - ComponentRenderer should use Application#createRequestCycle
    * [WICKET-5575] - Add support in FormTester#submit(String|Component) for Ajax submitters
    * [WICKET-5577] - Generation of wicket ids with prefix / suffix
    * [WICKET-5580] - Allow markup to find child fragments when wicket:child is inside a component tag
    * [WICKET-5585] - Wicket Extension Automplete does not work well with JavaScriptFilteredIntoFooterHeaderResponse
    * [WICKET-5586] - NextButton isEnabled() should bo logical conjunction of getWizardModel().isNextAvailable() and super.isEnabled()
    * [WICKET-5594] - AjaxButton #onSubmit() #onError() intricacies
    * [WICKET-5600] - Introduce CharSequenceResource similar to ByteArrayResource
    * [WICKET-5606] - SelectOptions with #setRecreateChoices(true) loses selection on form errors

** New Feature

    * [WICKET-5590] - Add a method to clear the cache of CachingResourceVersion







** Task
    * [WICKET-5172] - Add entry for Wicket 7 Javadocs in the website
    * [WICKET-5587] - Upgrade JQuery to latest releases - 1.11.1 & 2.1.1




** Wish
    * [WICKET-5340] - CssAttributeModifier and StyleAttributeModifier

