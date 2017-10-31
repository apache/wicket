#!/bin/bash
grep -r -H -l --include=*.html "<span wicket:id=\"mainNavigation\" />"  . | while read line; do
   cat $line | sed -e 's/<span wicket:id=\"mainNavigation\" \/>/<wicket:extend>/g' | sed -e 's/<\/body>/<\/wicket:extend>\n<\/body>/g' >> tmpfile ; mv tmpfile $line
done
