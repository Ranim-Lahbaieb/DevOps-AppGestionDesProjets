FROM node:22-alpine AS build

WORKDIR /build

COPY package*.json ./
RUN npm ci

COPY . .
RUN npm run build

RUN if [ -d dist/frontend/browser ]; then cp -r dist/frontend/browser /out; \
    else cp -r dist/frontend /out; fi

FROM nginx:alpine

COPY --from=build /out /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
