FROM nginx
MAINTAINER Steven smart <stevesmart113@gmail.com>
# Remove the default configuration to avoid conflicts
RUN rm /etc/nginx/conf.d/default.conf

# Copy your custom nginx.conf into place
COPY nginx.conf /etc/nginx/nginx.conf

#COPY nginx.conf /etc/nginx/nginx.conf

# Expose the port your config listens on
EXPOSE 8080