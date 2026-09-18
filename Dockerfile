FROM eclipse-temurin:21-jre-alpine
WORKDIR /opt/onmind/run
#RUN mv onmind-docker.ini /root/onmind.ini
EXPOSE 9990
COPY build/libs/onmind-xdb-1.0.0-early2026-full.jar /opt/onmind
CMD [ "java", "-jar", "onmind-xdb-1.0.0-early2026-full.jar", "onmindxdb" ]
