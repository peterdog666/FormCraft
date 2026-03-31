# FormCraft 🎨

<div align="center">

**5 分钟设计一个表单，零代码、所见即所得**

[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Vue](https://img.shields.io/badge/Vue-3.x-42b883.svg)](https://vuejs.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Docker](https://img.shields.io/badge/Docker-ready-2496ed.svg)](https://www.docker.com/)

[English](#english) | [中文](#中文)

</div>

---

## 中文

### 🎯 一句话介绍

**零代码表单设计平台**，拖拽组件即可设计表单，实时预览，一键发布，自动收集数据。比 Typeform 便宜，比 JimuForm 简单，比自己写代码快 10 倍。

### 📊 与竞品对比

| 特性 | FormCraft | Typeform | JimuForm | 自己写代码 |
|------|:---:|:---:|:---:|:---:|
| **部署成本** | 💰 免费 | 💰💰💰 按量收费 | 💰💰 企业版贵 | 💰 免费 |
| **学习成本** | ⭐ 无 | ⭐ 无 | ⭐⭐ 中等 | ⭐⭐⭐⭐ 高 |
| **实时预览** | ✅ 是 | ❌ 否 | ⚠️ 部分 | ✅ 是 |
| **表单校验** | ✅ 完整 | ✅ 完整 | ✅ 完整 | ❌ 需自己写 |
| **数据导出** | ✅ Excel/CSV | ✅ 是 | ✅ 是 | ❌ 需自己写 |
| **自部署** | ✅ 支持 | ❌ SaaS 只 | ✅ 支持 | ✅ 支持 |
| **开源** | ✅ MIT | ❌ 否 | ⚠️ 部分开源 | ✅ 可以 |
| **适合小团队** | ✅ 完美 | ⚠️ 贵 | ⚠️ 复杂 | ⚠️ 维护难 |

### 💡 使用场景

- 📋 **问卷调查** - 快速创建问卷，收集用户反馈
- 👔 **招聘表单** - HR 用来收集简历和应聘信息
- 📝 **报名表** - 活动报名、课程注册、会议签到
- 💬 **用户反馈** - 收集用户建议和投诉
- 📊 **数据收集** - 市场调研、客户信息收集
- 🎓 **教育培训** - 学生信息表、课程评价表
- 🏥 **医疗健康** - 患者信息表、健康问卷

### ✨ 核心功能

#### 1️⃣ 拖拽式表单设计器（四栏布局）

```
┌─────────────┬──────────────┬──────────────┬──────────────┐
│  组件面板   │   表单画布   │  属性配置    │  实时预览    │
├─────────────┼──────────────┼──────────────┼──────────────┤
│ 单行输入    │ ┌──────────┐ │ 标签: 姓名   │ ┌──────────┐ │
│ 多行文本    │ │ 姓名 *   │ │ 必填: ✓     │ │ 姓名 *   │ │
│ 下拉选择    │ │ [输入框] │ │ 最小长度: 2 │ │ [输入框] │ │
│ 单选按钮    │ │          │ │ 最大长度: 50│ │          │ │
│ 多选框      │ │ 邮箱 *   │ │ 格式: email │ │ 邮箱 *   │ │
│ 日期选择    │ │ [输入框] │ │ 错误提示... │ │ [输入框] │ │
│ 数字输入    │ │          │ │             │ │          │ │
└─────────────┴──────────────┴──────────────┴──────────────┘
```

**特点：**
- 左侧组件面板：点击或拖拽添加组件
- 中间画布：拖拽排序字段，实时编辑
- 右侧属性：配置字段标签、校验规则、错误提示
- 右下预览：实时看到表单效果

#### 2️⃣ 丰富的表单组件

- 📝 **单行输入** - 文本、邮箱、电话、URL
- 📄 **多行文本** - 长文本输入
- 🔢 **数字输入** - 整数、小数、范围
- 📅 **日期选择** - 日期、时间、日期范围
- 📋 **下拉选择** - 单选下拉菜单
- ⭕ **单选按钮** - 单选按钮组
- ☑️ **多选框** - 多选复选框

#### 3️⃣ 完整的表单校验

**客户端校验：**
- 必填字段检查
- 长度范围验证
- 邮箱、电话、URL 格式验证
- 自定义正则表达式
- 实时错误提示

**服务端校验：**
- 二次验证，防止绕过
- 返回详细错误信息
- 支持自定义错误提示

#### 4️⃣ 表单发布和分享

- 一键发布表单
- 生成可分享的表单链接
- 支持二维码分享
- 无需登录即可填写

#### 5️⃣ 数据收集和管理

```
提交记录表：
┌────┬──────────────┬─────────┬──────────────────┐
│ # │ 提交时间     │ IP地址  │ 提交数据         │
├────┼──────────────┼─────────┼──────────────────┤
│ 1  │ 2024-01-15   │ 1.2.3.4 │ 姓名: 张三       │
│    │ 14:30:45     │         │ 邮箱: xxx@xx.com │
├────┼──────────────┼─────────┼──────────────────┤
│ 2  │ 2024-01-15   │ 5.6.7.8 │ 姓名: 李四       │
│    │ 14:35:20     │         │ 邮箱: yyy@yy.com │
└────┴──────────────┴─────────┴──────────────────┘
```

- 查看所有提交记录
- 支持分页查询
- 按日期范围筛选

#### 6️⃣ 数据导出

- **CSV 导出** - 兼容 Excel，支持中文
- **Excel 导出** - 专业格式，支持样式
- **日期筛选** - 导出指定时间范围的数据
- **一键下载** - 浏览器直接下载

### 🚀 快速开始

#### 方式一：Docker Compose（推荐，一键启动）

```bash
# 克隆项目
git clone https://github.com/peterdog666/FormCraft.git
cd FormCraft

# 一键启动（MySQL + 后端 + 前端）
docker-compose up -d

# 等待 30 秒，然后访问
# 前端：http://localhost
# 后端 API：http://localhost:8080
```

#### 方式二：本地开发

**后端**
```bash
cd backend

# 修改数据库配置
vim src/main/resources/application.yml

# 启动
mvn spring-boot:run
```

**前端**
```bash
cd frontend

# 安装依赖
npm install

# 开发模式
npm run dev

# 构建
npm run build
```

### 📸 功能演示

> **表单设计器** - 四栏布局，实时预览，所见即所得
> ![Designer](docs/screenshots/designer.png)

> **表单列表** - 管理所有表单，一键发布
> ![List](docs/screenshots/list.png)

> **数据管理** - 查看提交记录，支持导出
> ![Data](docs/screenshots/data.png)

### 🛠️ 技术栈

| 层级 | 技术 | 版本 |
|------|------|------|
| 后端框架 | Spring Boot | 3.x |
| ORM | MyBatis-Plus | 3.5+ |
| 数据库 | MySQL | 8.0+ |
| Excel 导出 | Apache POI | 5.2+ |
| 前端框架 | Vue | 3.x |
| 构建工具 | Vite | 5.x |
| UI 组件库 | Element Plus | 2.5+ |
| 拖拽库 | Vue Draggable | 4.x |
| HTTP 客户端 | Axios | 1.x |
| 容器化 | Docker | 20.10+ |

### 📖 详细文档

- [安装指南](docs/INSTALL.md)
- [使用教程](docs/TUTORIAL.md)
- [API 文档](docs/API.md)
- [常见问题](docs/FAQ.md)

### ⚙️ 配置说明

编辑 `backend/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/formcraft
    username: root
    password: your_password
  jpa:
    hibernate:
      ddl-auto: update

# 文件上传配置（可选）
file:
  upload-dir: /tmp/formcraft/uploads
  max-size: 10MB
```

### ❓ FAQ

**Q: 表单可以有多少个字段？**
A: 理论上无限制，实际建议不超过 100 个字段，以保证用户体验。

**Q: 支持条件显示/隐藏字段吗？**
A: 当前版本不支持，这是我们的 Roadmap 中的功能。

**Q: 数据会保存多久？**
A: 默认永久保存，你可以手动删除。

**Q: 支持自定义样式吗？**
A: 当前版本不支持，但我们计划在下个版本支持主题切换。

**Q: 可以嵌入到我的网站吗？**
A: 可以，通过 iframe 嵌入表单链接即可。

**Q: 支持 API 集成吗？**
A: 支持，所有功能都有 REST API，详见 [API 文档](docs/API.md)。

### 🗺️ Roadmap

- [ ] 条件显示/隐藏字段
- [ ] 自定义主题和样式
- [ ] 表单模板库
- [ ] 数据统计和分析
- [ ] 邮件通知集成
- [ ] 支付集成（收费表单）
- [ ] 移动端优化

### 🤝 贡献

欢迎提交 Issue 和 Pull Request！

- 🐛 发现 Bug？[提交 Issue](https://github.com/peterdog666/FormCraft/issues)
- 💡 有新想法？[讨论 Discussion](https://github.com/peterdog666/FormCraft/discussions)
- 🔧 想贡献代码？[Fork 并提交 PR](https://github.com/peterdog666/FormCraft/pulls)

### 📄 License

MIT License - 详见 [LICENSE](LICENSE) 文件

---

## English

### 🎯 One-liner

**Zero-code form design platform** - Drag and drop to create forms, real-time preview, one-click publish, automatic data collection. Cheaper than Typeform, simpler than JimuForm, 10x faster than coding.

### ✨ Key Features

- 🎨 **Drag & Drop Designer** - Four-column layout with real-time preview
- 📋 **Rich Components** - Input, textarea, select, radio, checkbox, date, number
- ✅ **Complete Validation** - Client-side + server-side validation
- 🚀 **One-click Publish** - Generate shareable form links
- 📊 **Data Management** - View submissions, support pagination
- 📥 **Data Export** - CSV and Excel export with date filtering
- 🐳 **One-click Docker Deploy** - Complete Docker Compose configuration

### 🚀 Quick Start

```bash
git clone https://github.com/peterdog666/FormCraft.git
cd FormCraft
docker-compose up -d
# Frontend: http://localhost
# Backend API: http://localhost:8080
```

### 🛠️ Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Spring Boot 3 |
| ORM | MyBatis-Plus |
| Database | MySQL 8 |
| Excel Export | Apache POI |
| Frontend | Vue 3 + Vite |
| UI Library | Element Plus |
| Drag & Drop | Vue Draggable |
| HTTP Client | Axios |
| Container | Docker + Docker Compose |

---

<div align="center">

⭐ **如果觉得有帮助，请给个 Star！** ⭐

Made with ❤️ | [Issues](https://github.com/peterdog666/FormCraft/issues) | [Discussions](https://github.com/peterdog666/FormCraft/discussions)

</div>
