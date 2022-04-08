FROM navikt/java:11
USER root
RUN apt-get update && 	apt-get install -y netcat
USER apprunner
COPY sendsoknad-boot/target/sendsoknad-boot-0.0.1.jar /app/app.jar
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 -Doracle.jdbc.javaNetNio=false"
#DEBUG arguments -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=8888


