FROM node:18.17.1-slim

WORKDIR /app
COPY . .

RUN yarn install
RUN yarn add react-router-dom
RUN yarn add @emotion/react
RUN yarn add --dev @emotion/babel-plugin
RUN yarn add @emotion/styled
RUN yarn add -D @craco/craco
RUN yarn add -D @emotion/babel-preset-css-prop
RUN yarn add react-star-rate
RUN yarn add rsuite
RUN npm install -g serve
RUN yarn build


EXPOSE 3000

CMD ["serve", "-s", "build"]