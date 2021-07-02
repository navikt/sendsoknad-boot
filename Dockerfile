FROM navikt/java:11
USER root
RUN apt-get update && 	apt-get install -y iputils-ping
RUN apt-get -y install sudo
RUN adduser apprunner sudo
RUN chmod 7777 /bin/ping
USER apprunner
COPY sendsoknad-boot/target/sendsoknad-boot-0.0.1.jar /app/app.jar
COPY init.sh /init-scripts/init.sh


