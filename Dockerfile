FROM java:8
VOLUME /tmp
EXPOSE 8094
ADD /build/libs/DeltaIoTLoopaExecuter.jar DeltaIoTLoopaExecuter.jar
ENTRYPOINT ["java","-jar","DeltaIoTLoopaExecuter.jar"]
