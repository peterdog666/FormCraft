# FormCraft - 低代码表单平台

![FormCraft](https://img.shields.io/badge/FormCraft-v1.0.0-blue)
![Vue](https://img.shields.io/badge/Vue-3.4-green)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-orange)
![License](https://img.shields.io/badge/License-MIT-yellow)

## 项目简介

FormCraft 是一款轻量级的低代码表单平台，用户可以通过拖拽组件快速设计表单，发布后自动生成可访问的表单链接，收集用户提交的数据，并支持查看和导出。

## 功能特性

- **🎨 表单设计器**：拖拽式表单设计，所见即所得
- **📋 组件丰富**：支持输入框、文本域、下拉选择、单选、多选、日期、数字等组件
- **🚀 一键发布**：快速发布表单，生成访问链接
- **📊 数据管理**：查看提交记录，支持分页和导出

## 技术栈

### 后端
- Spring Boot 3.2
- MyBatis-Plus 3.5
- MySQL 8.0
- Lombok

### 前端
- Vue 3.4
- Vite 5.0
- Element Plus 2.5
- Vue Draggable 4.1

## 快速开始

### 环境要求

- JDK 17+
- Node.js 18+
- MySQL 8.0
- Docker & Docker Compose (可选)

### 方式一：Docker 部署（推荐）

```bash
# 启动所有服务
docker-compose up -d

# 查看服务状态
docker-compose ps
```

访问 http://localhost:3000

### 方式二：本地开发

**1. 初始化数据库**

```bash
mysql -u root -p < init.sql
```

**2. 启动后端**

```bash
cd backend
# 修改 src/main/resources/application.yml 中的数据库配置
mvn spring-boot:run
```

**3. 启动前端**

```bash
cd frontend
npm install
npm run dev
```

访问 http://localhost:3000

## 项目结构

```
FormCraft/
├── backend/                 # Spring Boot 后端
│   ├── src/main/java/com/formcraft/
│   │   ├── controller/      # 控制器层
│   │   ├── service/         # 业务逻辑层
│   │   ├── mapper/         # 数据访问层
│   │   ├── entity/         # 实体类
│   │   └── common/          # 公共类
│   └── src/main/resources/
│       └── application.yml  # 配置文件
├── frontend/                 # Vue3 前端
│   ├── src/
│   │   ├── views/          # 页面组件
│   │   ├── api/            # API 接口
│   │   └── router/         # 路由配置
│   └── package.json
├── docker-compose.yml       # Docker 编排
├── init.sql                 # 数据库初始化
└── README.md
```

## API 接口

### 表单管理

| 方法 | 路径 | 描述 |
|------|------|------|
| GET | /api/form/list | 获取表单列表 |
| GET | /api/form/{id} | 获取表单详情 |
| POST | /api/form | 创建表单 |
| PUT | /api/form/{id} | 更新表单 |
| DELETE | /api/form/{id} | 删除表单 |
| PUT | /api/form/{id}/publish | 发布表单 |
| PUT | /api/form/{id}/unpublish | 停用表单 |

### 表单提交

| 方法 | 路径 | 描述 |
|------|------|------|
| POST | /api/submission/{formId} | 提交表单 |
| GET | /api/submission/{formId} | 获取提交记录 |

## 表单字段类型

| 类型 | 描述 |
|------|------|
| input | 单行输入框 |
| textarea | 多行文本框 |
| number | 数字输入 |
| date | 日期选择 |
| select | 下拉选择 |
| radio | 单选按钮 |
| checkbox | 多选框 |

## 使用示例

1. **创建表单**：点击「创建表单」进入设计器
2. **添加字段**：从左侧组件面板拖拽或点击添加字段
3. **配置属性**：在右侧面板配置标签、占位符、是否必填等
4. **保存发布**：保存后点击「发布」生成访问链接
5. **收集数据**：分享表单链接，收集用户提交
6. **导出数据**：在数据页面导出 Excel 格式

## License

MIT License - 详见 [LICENSE](LICENSE) 文件

---

Made with ❤️ by FormCraft Team
