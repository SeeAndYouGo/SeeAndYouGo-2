FROM node:18.17.1-slim

WORKDIR /app
COPY . .
ARG GOOGLE_ANALYTICS_TRACKING_ID

RUN yarn install
RUN yarn add react-router-dom
RUN yarn add @emotion/react
RUN yarn add --dev @emotion/babel-plugin
RUN yarn add @emotion/styled
RUN yarn add -D @craco/craco
RUN yarn add -D @emotion/babel-preset-css-prop
RUN yarn add react-star-rate
RUN yarn add rsuite

ENV REACT_APP_GOOGLE_ANALYTICS_TRACKING_ID ${GOOGLE_ANALYTICS_TRACKING_ID}
# RUN npm install -g serve
# RUN yarn build


EXPOSE 3000

CMD ["yarn", "start"]