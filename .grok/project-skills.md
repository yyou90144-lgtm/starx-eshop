# 星选商城 eshop-release-v1.01 · 仓库 skill 绑定

> 知识库完整绑定：`D:\Knowledge\projects\eshop-release-v1.01.md`  
> 对话记忆：根目录 `星选商城-v1.01-对话记忆.md`（开项目先读，读完说固定口令）

```yaml
skills_warm_required:
  - design-taste-frontend
  - impeccable
skills_optional:
  - name: redesign-existing-projects
    when: [redesign, polish, audit-existing]
  - name: image-to-code
    when: [visual-ref, redesign, landing, hero]
  - name: imagegen-frontend-web
    when: [section-mock, marketing]
  - name: developing-frontend-apps
    when: [vue, router, pinia]
  - name: developing-backend-services
    when: [api, spring, flyway]
  - name: verification-before-completion
    when: [test, ship]
  - name: recycle
    when: [delete, rm, cleanup]
skills_never:
  - gpt-taste
```

## 约束摘要

- 品牌：星选 STARX；主色 `#ff4d4f`；暖白底 `#f7f4f1`
- 业务代码在 `eshop-release-v1.0\`
- 同会话不要叠 3 个以上审美 skill
