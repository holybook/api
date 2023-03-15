FROM openjdk:11
EXPOSE 8080:8080
RUN mkdir /app
COPY ./server/build/libs/*.jar /app/holybook.jar
ENTRYPOINT ["java","-jar","/app/holybook.jar"]