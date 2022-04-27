FROM openjdk:17-alpine

EXPOSE 8000

COPY api.jar api.jar
RUN apk add --no-cache chromium curl

ENTRYPOINT exec java ${JAVA_OPTS} -jar api.jar