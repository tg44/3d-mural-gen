FROM hseeberger/scala-sbt:8u212_1.2.8_2.13.0 as builder
WORKDIR /app
COPY build.sbt /app/build.sbt
COPY project /app/project
RUN sbt update test:update
COPY . .
RUN sbt compile test stage && \
    chmod -R u=rX,g=rX /app/target/universal/stage && \
    chmod u+x,g+x /app/target/universal/stage/bin/*


FROM openjdk:8
RUN apt-get update && apt-get -y install openscad
USER root
RUN adduser --system -u 1001 mural
USER 1001
EXPOSE 9000
ENTRYPOINT ["/app/bin/mural-example"]
CMD []
COPY --from=builder --chown=1001:root /app/target/universal/stage /app
