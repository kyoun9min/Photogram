FROM tomcat:10.1-jdk17

RUN rm -rf /usr/local/tomcat/webapps/*

COPY target/photogramstart-1.0.war /usr/local/tomcat/webapps/ROOT.war

ENV SPRING_PROFILES_ACTIVE=dev

EXPOSE 8080

CMD ["catalina.sh", "run"]