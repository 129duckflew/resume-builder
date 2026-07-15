# KEDA Scale-from-Zero 配置修复设计

## 背景

项目使用 Traefik (k3s built-in) + KEDA HTTP Add-on 实现前后端服务的按需启动（scale from zero）。用户允许首次请求 10-15 秒的冷启动延迟。

## 当前架构

```
用户 → Traefik (k3s, port 80)
  → IngressRoute (resume.local, namespace=keda → keda-add-ons-http-interceptor-proxy:8080)
    → KEDA HTTP Interceptor Proxy
      → HTTPScaledObject 匹配路径
        → frontend-service:80 (路径 /)
        → backend-service:8080 (路径 /api, /s)
  → IngressRoute (grafana.resume.local, namespace=resume-builder → grafana-service:3000)
```

> 注：Traefik 的 Kubernetes CRD provider 不允许 IngressRoute 引用其他命名空间的服务，因此 `resume.local` 的 IngressRoute 必须放在 `keda` 命名空间，而 `grafana.resume.local` 的 IngressRoute 放在 `resume-builder` 命名空间。

## 诊断发现

当前系统实际已能工作，但存在四个配置/环境问题：

1. **IngressRoute 跨命名空间服务引用被 Traefik 拒绝** — 将 IngressRoute 放到 `resume-builder` 后引用 `keda` 命名空间的 interceptor proxy 会报错 `service ... not in the parent resource namespace`。
2. **KEDA 安装不完整** — 缺少 `scaledjobs.keda.sh` CRD，导致 `keda-operator` 反复 CrashLoopBackOff，影响 scaling 稳定性。
3. **Deployment replicas 与 KEDA 冲突** — `replicas: 2` 与 HTTPScaledObject 的 `min: 0` 矛盾。
4. **遗留 HPA 与 KEDA HPA 冲突** — 手动 `backend-hpa.yaml` / `frontend-hpa.yaml` 与 KEDA 自动创建的 HPA 冲突。

## 修复方案

### Fix 1: 拆分 IngressRoute 到对应命名空间

- 删除原来单条的 `resume-ingress` IngressRoute。
- 新建 `resume-ingress` 在 `keda` 命名空间，只路由 `resume.local` → `keda-add-ons-http-interceptor-proxy:8080`。
- 新建 `grafana-ingress` 在 `resume-builder` 命名空间，只路由 `grafana.resume.local` → `grafana-service:3000`。
- `k8s-apply.sh` 应用 `k8s/06-ingress/` 时不再带 `-n resume-builder`，由 YAML 自身声明 namespace。

### Fix 2: 安装完整 KEDA 并修复 ScaledJob CRD

- 新增 `scripts/k8s-install-keda.sh`，一键安装 KEDA core 2.16.1 + HTTP Add-on 0.15.0。
- 对 `scaledjobs.keda.sh` 等超大 CRD 使用 `kubectl apply --server-side`，避免 client-side apply 的 annotation 超长错误。

### Fix 3: 修正 deployment replicas

- backend/frontend deployment: `replicas: 2` → `replicas: 0`。
- 删除 `backend-hpa.yaml` 和 `frontend-hpa.yaml`（手动 HPA 与 KEDA HPA 冲突）。

### Fix 4: 更新脚本与文档

- `scripts/k8s-smoke-test.sh`：先缩到 0，再测试前后端冷启动，再测热请求；使用 `--resolve resume.local:80:127.0.0.1` 绕过 macOS 对 `.local` 的 mDNS 延迟。
- `README.md`：将 HPA 说明替换为 KEDA scale-from-zero 说明，补充 KEDA 安装步骤和 `.local` DNS 提示。

## 预期效果

- 冷启动: 前端 ~8s, 后端 ~18s (Spring Boot)
- 热请求: < 100ms
- scaledown: 300s 无请求后缩到 0

## 验证结果

在 Colima k3s (4 CPU / 8GB) 上实测：

| 请求 | HTTP 状态 | 耗时 |
|---|---|---|
| 前端冷启动 | 200 | 7.5s |
| 后端冷启动 | 200 | 17.6s |
| 前端热请求 | 200 | 1.4ms |
| 后端热请求 | 200 | 50ms |

`./scripts/k8s-smoke-test.sh` 全部通过。
