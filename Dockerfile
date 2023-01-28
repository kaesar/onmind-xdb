FROM adoptopenjdk/openjdk11:alpine-jre
WORKDIR /opt/onmind/run
#RUN mv onmind-docker.ini /root/onmind.ini
EXPOSE 9990
COPY C:/Users/andrey/onmind/dai/build/libs/onmind-xdb-1.0.0-final2023-full.jar /opt/onmind
CMD [ "java", "-jar", "onmind-xdb-1.0.0-final2023-full.jar", "onmindxdb" ]
