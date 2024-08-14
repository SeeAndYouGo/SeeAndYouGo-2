FROM yenyeny1/seeandyougo-react:latest as build

WORKDIR /app
COPY package.json .
COPY . .

RUN yarn install
RUN yarn build
EXPOSE 3000

#CMD ["serve", "-s", "build"]

FROM nginx:latest

RUN rm /etc/nginx/conf.d/default.conf
COPY default.conf /etc/nginx/conf.d/default.conf

# React 빌드 결과물을 Nginx의 기본 경로로 복사
COPY --from=build /app/build /usr/share/nginx/html

EXPOSE 80
EXPOSE 443
 
CMD ["nginx", "-g", "daemon off;"]