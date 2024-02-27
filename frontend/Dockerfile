FROM yenyeny1/seeandyougo-react:latest

WORKDIR /app
COPY . .

RUN yarn install
RUN yarn build
EXPOSE 3000

CMD ["serve", "-s", "build"]