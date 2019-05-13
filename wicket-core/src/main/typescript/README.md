Wicket Core TypeScript

## Build
```cd wicket-core/src/main/typescript```  
```npm install```  
```npm run tsc```  
```npm run rollup --config``` 

Please note it will replace original org/apache/wicket/ajax/res/js/wicket-ajax-jquery.js

## Build with MVN
Include `ts-transpile` profile by adding `-P ts-transpile` to build with frontend-maven-plugin.