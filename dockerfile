# FROM openjdk:8-jdk
# RUN apt-get update && apt-get install -y --no-install-recommends xvfb openjfx && rm -rf /var/lib/apt/lists/*
FROM odinuge/maven-javafx:3-jdk-8
COPY . /app

ADD --chown=gradle:gradle . /app

WORKDIR /app

RUN chmod +x ./gradlew

RUN ./gradlew build

EXPOSE 3000

CMD ["./gradlew", "run"]


