FROM amazoncorretto:21-alpine
USER 1000
COPY target/*.jar /app.jar
CMD java -jar /app.jar
