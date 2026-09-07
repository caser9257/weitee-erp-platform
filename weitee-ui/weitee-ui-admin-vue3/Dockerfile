# ---- Build stage ----
# 基础镜像可参数化：内网/CI 环境经 docker.env 或 CI 变量指向可达的镜像源
ARG DOCKER_REGISTRY_MIRROR=docker.io
ARG NODE_IMAGE=library/node:20-alpine
ARG NGINX_IMAGE=library/nginx:1.24-alpine

FROM ${DOCKER_REGISTRY_MIRROR}/${NODE_IMAGE} AS builder

WORKDIR /app

ARG HTTP_PROXY
ARG HTTPS_PROXY
ARG ALL_PROXY
ARG NO_PROXY
ARG NPM_REGISTRY=https://registry.npmmirror.com
ARG PNPM_REGISTRY=https://registry.npmmirror.com
ARG VITE_APP_CAPTCHA_ENABLE=false

ENV HTTP_PROXY=${HTTP_PROXY}
ENV HTTPS_PROXY=${HTTPS_PROXY}
ENV ALL_PROXY=${ALL_PROXY}
ENV NO_PROXY=${NO_PROXY}
ENV VITE_APP_CAPTCHA_ENABLE=${VITE_APP_CAPTCHA_ENABLE}

RUN npm config set registry ${NPM_REGISTRY} \
    && npm install -g pnpm@9 \
    && pnpm config set registry ${PNPM_REGISTRY} \
    && pnpm config set fetch-retries 5 \
    && pnpm config set fetch-timeout 120000

COPY package.json pnpm-lock.yaml ./

RUN pnpm install --frozen-lockfile

COPY . .
RUN pnpm build:prod

# ---- Runtime stage ----
FROM ${DOCKER_REGISTRY_MIRROR}/${NGINX_IMAGE}

ENV TZ=Asia/Shanghai

COPY --from=builder /app/dist-prod /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
