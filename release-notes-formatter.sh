# if you paste JIRA's release notes HTML (in the textbox at the end of the page)
# into a file named "tmp" and run this script it should spit out the release notes
# in the right format for you.  YMMV (works on Ubuntu)
cat tmp | grep -v -e '^</li' -e '^</h2' -e '^<ul' -e '</ul' | sed -e 's|<h2>\s\+|** |' \
 -e 's|<li>\[<a[^>]*>\(WICKET-[0-9]\+\)</a>]|    * \1|' \
 -e 's| -\s\+| - |g' \
 -e "s|&#39;|'|g" \
 -e 's|&lt;|<|g' \
 -e 's|&gt;|>|g'

