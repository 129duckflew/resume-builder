---
name: rest-api
description: REST API design checklist for Spring Boot controllers. Use when adding or changing @RestController endpoints, request/response DTOs, status codes, @Valid validation, or pagination.
---

## REST API 设计 Checklist

- [ ] GET /resources — 列表查询，返回 `200 + JSON[]`
- [ ] GET /resources/{id} — 单个查询，返回 `200` 或 `404`
- [ ] POST /resources — 创建，返回 `200 + JSON`
- [ ] PUT /resources/{id} — 更新（部分更新，null 不覆盖），返回 `200`
- [ ] DELETE /resources/{id} — 删除，返回 `204 No Content`
- [ ] `@Valid` + `@NotBlank` / `@Email` / `@Size` 参数校验
- [ ] 错误统一格式：`{ "error": "描述信息" }`
- [ ] 分页参数：`@RequestParam(defaultValue = "...")`
- [ ] 所有端点加 `@RequestMapping("/api/...")`
