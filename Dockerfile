# Use a minimal base image
FROM openjdk:24-slim-bullseye

#COPY init.sql /docker-entrypoint-initdb.d/

#COPY nginx.conf /nginx.conf:/etc/nginx/

#RUN chmod +r /docker-entrypoint-initdb.d/init.sql

#RUN echo '18.193.168.136 host.docker.internal' >> /etc/hosts
ENV JAVA_OPTS="-Xms4g -Xmx4g"

WORKDIR /app

COPY target/afriluck-ussd-0.0.1-SNAPSHOT.jar /app/app.jar

EXPOSE 8000

#CMD ["java", "-jar", "app.jar"]
CMD java $JAVA_OPTS -jar app.jar