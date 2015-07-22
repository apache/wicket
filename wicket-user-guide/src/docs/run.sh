set -e

for line in $(find guide/ -name "*.gdoc"); do 
     dirname=`dirname $line`
     dirname=${dirname:5}/
     basename=`basename $line .gdoc`
     if [ ! -d asciidoc$dirname ]
        then 
            mkdir -p asciidoc$dirname 
     fi
     cat guide$dirname$basename.gdoc | ./adoccommandline.php > asciidoc$dirname$basename.adoc
     echo $line converted to: $basename.adoc
done
