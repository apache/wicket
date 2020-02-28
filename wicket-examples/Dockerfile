FROM tomcat:9.0-jre11
MAINTAINER Apache Wicket Team <dev@wicket.apache.org>

RUN rm -rf /usr/local/tomcat/webapps/*

ADD target/ROOT.war /usr/local/tomcat/webapps/
