FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY . .
RUN javac -encoding UTF-8 -d . Main.java model/*.java service/*.java util/*.java
EXPOSE 8080
CMD ["java", "Main"]