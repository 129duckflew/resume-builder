## JPA 实体设计 Checklist

- [ ] `@Entity` + `@Table(name = "...")` 表名 snake_case
- [ ] ID 策略：`Resume` → `GenerationType.UUID`, `User` → `GenerationType.IDENTITY`
- [ ] `@Column(name = "...")` 列名 snake_case
- [ ] `@Column(nullable = false)` 必填字段
- [ ] TEXT 类型：`@Column(columnDefinition = "TEXT")`
- [ ] `@PrePersist` / `@PreUpdate` 自动时间戳
- [ ] 无参构造器（JPA 要求）
- [ ] Repository 继承 `JpaRepository<Entity, IdType>`
- [ ] 含 userId 的查询用 `findByIdAndUserId(id, userId)`
- [ ] 自定义查询使用 `@Query` 或方法命名规则
