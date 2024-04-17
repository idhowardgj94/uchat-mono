FROM clojure as build

ADD . /usr/src
WORKDIR /usr/src/projects/app

RUN clojure -M:uberjar


FROM clojure
COPY --from=build /usr/src/projects/app/target/app.jar /usr/app.jar
WORKDIR /usr
CMD ["java", \
     "-cp", "app.jar", \
     "clojure.main", "-m", "com.howard.uchat.backend.api-server.main"]