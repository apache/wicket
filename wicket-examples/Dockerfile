FROM tomcat:8.5-jre8
MAINTAINER Apache Wicket Team <dev@wicket.apache.org>

RUN rm -rf /usr/local/tomcat/webapps/*

ADD target/ROOT.war /usr/local/tomcat/webapps/

