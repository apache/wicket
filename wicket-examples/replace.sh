#!/bin/bash
grep -r -H -l --include=*.html "<span wicket:id=\"mainNavigation\"></span>"  . | while read line; do
   cat $line | sed -e 's/<span wicket:id=\"mainNavigation\"\/><\/span>/<wicket:extend>/g' | sed -e 's/<\/body>/<\/wicket:extend>\n<\/body>/g' >> tmpfile ; mv tmpfile $line
done
