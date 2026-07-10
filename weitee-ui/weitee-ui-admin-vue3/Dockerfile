# ---- Build stage ----
FROM node:20-alpine AS builder

WORKDIR /app

ARG HTTP_PROXY
ARG HTTPS_PROXY
ARG ALL_PROXY
ARG NO_PROXY
ARG NPM_REGISTRY=https://registry.npmmirror.com
ARG PNPM_REGISTRY=https://registry.npmmirror.com

ENV HTTP_PROXY=${HTTP_PROXY}
ENV HTTPS_PROXY=${HTTPS_PROXY}
ENV ALL_PROXY=${ALL_PROXY}
ENV NO_PROXY=${NO_PROXY}

RUN npm config set registry ${NPM_REGISTRY} \
    && npm install -g pnpm \
    && pnpm config set registry ${PNPM_REGISTRY} \
    && pnpm config set fetch-retries 5 \
    && pnpm config set fetch-timeout 120000

COPY package.json pnpm-lock.yaml ./

RUN pnpm install --frozen-lockfile

COPY . .
RUN pnpm build:prod

# ---- Runtime stage ----
FROM nginx:1.24-alpine

ENV TZ=Asia/Shanghai

COPY --from=builder /app/dist-prod /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
