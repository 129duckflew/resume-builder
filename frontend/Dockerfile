# Build stage（固定构建机原生架构，避免多架构构建时 npm 在 QEMU 下运行）
FROM --platform=$BUILDPLATFORM node:20-alpine AS build

WORKDIR /app
COPY package.json package-lock.json ./
RUN npm install

COPY . .
RUN npm run build

# Production stage
FROM nginx:1.25-alpine

COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
