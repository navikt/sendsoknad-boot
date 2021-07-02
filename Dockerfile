FROM navikt/java:11
COPY sendsoknad-boot/target/sendsoknad-boot-0.0.1.jar /app/app.jar
COPY init.sh /init-scripts/init.sh
RUN apt-get update && apt-get install -y iputils-ping

