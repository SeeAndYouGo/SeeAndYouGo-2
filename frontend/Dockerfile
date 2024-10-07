FROM yenyeny1/seeandyougo-react:latest

WORKDIR /app

COPY package.json yarn.lock ./
RUN yarn cache clean
RUN yarn install --network-timeout 1000000

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
RUN yarn add axios
RUN yarn add react-icons
RUN yarn add react-ga4
RUN yarn add redux-persist @reduxjs/toolkit react-redux 
RUN yarn add react-spinners

RUN npm install -g serve
RUN yarn build

EXPOSE 3000

CMD ["serve", "-s", "build"]