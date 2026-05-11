FROM eclipse-temurin

WORKDIR /app

COPY ./build/libs/HololiveOCGManager-0.0.1-SNAPSHOT.jar ./

ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar HololiveOCGManager-0.0.1-SNAPSHOT.jar"]