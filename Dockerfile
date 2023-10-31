FROM nginx:latest

RUN rm /etc/nginx/conf.d/default.conf
COPY default.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
 
CMD ["nginx", "-g", "daemon off;"]