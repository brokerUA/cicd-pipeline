FROM node:20-alpine

WORKDIR /opt

COPY package*.json ./

RUN npm install

COPY . .

ENTRYPOINT ["npm", "run", "start"]