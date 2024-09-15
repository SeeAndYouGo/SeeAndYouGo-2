FROM node:18.17.1-slim

WORKDIR /app

COPY package.json yarn.lock ./
RUN yarn cache clean
RUN yarn install --network-timeout 1000000

COPY . .

RUN yarn build

RUN npm install -g serve

EXPOSE 3000

CMD ["serve", "-s", "build"]