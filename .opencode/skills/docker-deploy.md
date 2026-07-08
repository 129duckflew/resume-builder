## Docker 部署 Checklist

- [ ] `docker compose up --build -d` — 构建并启动全部服务
- [ ] `docker compose ps` — 检查容器状态（全部 Up / Healthy）
- [ ] `docker compose logs backend --tail=20` — 后端日志
- [ ] `docker compose logs frontend --tail=20` — 前端日志
- [ ] 端口映射：Backend `8081 → 8080`, Frontend `3000 → 80`, PostgreSQL `5432`
- [ ] `docker compose down` — 停止服务
- [ ] `docker compose down -v` — 停止并删除数据卷（注意数据丢失）
- [ ] 构建失败检查：Dockerfile 路径、Maven/NPM 依赖缓存
- [ ] 启动失败检查：端口冲突（`lsof -i :<port>`）、数据库连接
