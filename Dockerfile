FROM navikt/java:11
COPY sendsoknad-boot/target/sendsoknad-boot-0.0.1.jar /app/app.jar
COPY init.sh /init-scripts/init.sh
RUN sudo apt-get update && sudo apt-get install -y iputils-ping

