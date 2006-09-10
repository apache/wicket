#! /bin/sh

install() {
	cd ../$1
	mvn clean install -Dmaven.test.skip=true
	cd ../wicket-parent
}

rm -rv ~/.m2/repository/wicket/ 
install wicket-parent
install wicket
install wicket-extensions
install wicket-spring
install wicket-spring-annot
install wicket-auth-roles
