import grails.util.Holders

eventDocStart = { kind ->
   println "Started $kind"
   
   if (kind == "refdocs") {
       //the purpose of this custom macro is to centralize external URLs
       //into Config.groovy Usage:
       //{externalink:urlPropertyId@additional/path}
       //If no content is provide the generated URL is used in its place.
       
       org.radeox.macro.MacroLoader.newInstance().add(
        org.radeox.macro.MacroRepository.instance,
        new org.radeox.macro.Preserved() {
    
          @Override
          String getName() {
            'externalink'
          }
    
          @Override
          void setInitialContext(org.radeox.api.engine.context.InitialRenderContext context) {
            super.setInitialContext(context)
          }
    
          @Override
          void execute(Writer writer, org.radeox.macro.parameter.MacroParameter params) {
           def content = params.content
           def tagValue = params.get("0")
           def tagSplitted = tagValue.split("@")
           def propertyId = tagSplitted[0]
           def urlSegments = tagSplitted.length > 1 ? tagSplitted[1] : ''
           def baseUrl = Holders.config.flatten().get(propertyId)
           
           if (content == null) {
            content = baseUrl + "/" + urlSegments
           }
           
           writer << "<span class=\"nobr\">"
           writer << "<a href=\""
           writer << baseUrl + "/" + urlSegments
           writer << "\" target=\"blank\">"
           writer << content
           writer << "</a></span>"
          }
        })
        
        org.radeox.macro.MacroLoader.newInstance().add(
            org.radeox.macro.MacroRepository.instance,
            new org.radeox.macro.Preserved() {
    
              @Override
              String getName() {
                'divcontainer'
              }
              
              @Override
              void execute(Writer writer, org.radeox.macro.parameter.MacroParameter params) {
                def content = params.content
                def tagValue = params.get("0")
                
                writer << "<div class=\"" + tagValue + "\">"
                writer << content
                writer << "</div>"
              }
            })
        
        println "macro externalink added"
    }
}

eventDocEnd = { kind ->
   println "Completed $kind"
}

