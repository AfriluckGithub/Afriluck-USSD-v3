# Use a minimal base image
FROM openjdk:24-slim-bullseye

COPY init.sql /docker-entrypoint-initdb.d/

COPY nginx.conf /etc/nginx/conf.d/default.conf

#RUN chmod +r /docker-entrypoint-initdb.d/init.sql

#RUN echo '18.193.168.136 host.docker.internal' >> /etc/hosts

WORKDIR /app

COPY target/afriluck-ussd-0.0.1-SNAPSHOT.jar /app/app.jar

EXPOSE 8000

CMD ["java", "-jar", "app.jar"]