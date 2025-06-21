FROM tomcat:10.1-jre17

LABEL authors="Apache Wicket Team <dev@wicket.apache.org>"

RUN rm -rf /usr/local/tomcat/webapps/*

ADD target/wicket-examples.war /usr/local/tomcat/webapps/ROOT.war
