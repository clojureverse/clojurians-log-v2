FROM clojure:openjdk-11-tools-deps-slim-bullseye

RUN apt-get update -yq \
    && apt-get install curl gnupg netcat -yq

WORKDIR /src

COPY ./deps.edn /src/deps.edn

RUN clojure -P

COPY . /src

EXPOSE 8919

CMD ["clojure", "-X:run-prod"]
