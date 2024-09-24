# react
FROM node:18.17.1-slim as build

WORKDIR /app
COPY frontend/package.json frontend/yarn.lock ./
RUN yarn cache clean
RUN yarn install --network-timeout 1000000

COPY frontend/. .
RUN yarn build

# prod
FROM nginx:latest

COPY --from=build /app/build /usr/share/nginx/html
COPY default.conf /etc/nginx/conf.d/default.conf

# finish
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
