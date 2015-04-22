eventDocStart = { kind ->
   println "Started $kind"
}

eventDocEnd = { kind ->
   println "Completed $kind"
   /*new File("../img").eachFile() { file -> 
      println file.getName()
   }*/
}
