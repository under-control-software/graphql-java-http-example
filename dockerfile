FROM odinuge/maven-javafx:3-jdk-8
COPY . /app

ADD --chown=gradle:gradle . /app

WORKDIR /app

RUN chmod +x ./gradlew

RUN ./gradlew build

EXPOSE 3000

CMD ["sh","-c","./gradlew run"]
