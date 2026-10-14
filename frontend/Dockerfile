# ========================================
# Stage 1 — Build Vite
# ========================================
FROM node:20-alpine AS builder

WORKDIR /app

# Cache de deps
COPY package*.json ./
RUN npm ci

# Build
COPY . .
RUN npm run build

# ========================================
# Stage 2 — Nginx serve estático
# ========================================
FROM nginx:1.27-alpine

# Config customizada
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Assets do build
COPY --from=builder /app/dist /usr/share/nginx/html

EXPOSE 80

HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
  CMD wget -qO- http://localhost/ > /dev/null || exit 1