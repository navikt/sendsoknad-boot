FROM navikt/java:11
USER root
RUN apt-get update && 	apt-get install -y netcat
USER apprunner
COPY sendsoknad-boot/target/sendsoknad-boot-0.0.1.jar /app/app.jar
COPY init.sh /init-scripts/init.sh
ENV JAVA_OPTS -Xmx1000m


