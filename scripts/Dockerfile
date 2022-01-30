FROM openjdk:17-alpine

EXPOSE 8000

COPY api.jar api.jar

ENTRYPOINT exec java ${JAVA_OPTS} -jar api.jar