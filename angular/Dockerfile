 ### STAGE 1: Build ###
 FROM node:18 AS build
 WORKDIR /usr/src/app
 COPY package.json package-lock.json ./
 RUN npm install
 COPY . .
 RUN npm run build
 ### STAGE 2: Run ###
 FROM nginx:1.27.2-alpine
 COPY nginx.conf /etc/nginx/nginx.conf
 COPY --from=build /usr/src/app/dist/angular/browser /usr/share/nginx/html
