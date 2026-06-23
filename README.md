# Xiaomi-Shopping-Agent

仿小米商城业务下的智能导购 Agent，包括 Orchestrator 主 Agent、Knowledge 知识库子节点、Shopping 购物子节点和独立 MCP Server。Knowledge 节点覆盖商品咨询、规格比对、促销政策、售后服务等场景；Shopping 节点封装加购、下单、库存、物流、优惠等工具能力。

## 1. 后端启动拓扑

当前后端由两个 Spring Boot 进程组成：

| 进程 | 模块 | 默认端口 | 说明 |
| --- | --- | --- | --- |
| 主后端 | `bootstrap` | `8080` | 聚合启动 `orchestrator / knowledge / shopping` 三个模块，提供 HTTP API |
| MCP 工具服务 | `mcpserver` | `8090` | 独立进程，向 Shopping 子节点提供 MCP 工具能力 |

也就是说：**主后端确实由 `bootstrap` 聚合启动**，`mcpserver` 按架构是独立伴随进程，不合并进 `bootstrap`。

关键入口：

- 主后端启动类：[bootstrap/src/main/java/com/xiaomi/shopping/agent/Application.java](bootstrap/src/main/java/com/xiaomi/shopping/agent/Application.java)
- MCP Server 启动类：[mcpserver/src/main/java/com/xiaomi/shopping/agent/mcpserver/McpServerApplication.java](mcpserver/src/main/java/com/xiaomi/shopping/agent/mcpserver/McpServerApplication.java)
- 主配置：[bootstrap/src/main/resources/application.yml](bootstrap/src/main/resources/application.yml)
- MCP 配置：[mcpserver/src/main/resources/application.yml](mcpserver/src/main/resources/application.yml)

## 2. 前置依赖

本地启动前需要准备：

- JDK 17
- Maven 3.8+
- PostgreSQL，需安装/启用：
  - `vector` 扩展
  - `pg_trgm` 扩展
- Redis
- 可选：DashScope API Key，用于 Orchestrator ChatModel
- 可选：SiliconFlow API Key，用于 Embedding 和外部 Rerank

数据库脚本在 [doc/database/](doc/database/)：

```bash
psql -h 192.168.122.128 -U root -d postgres -f doc/database/create-database.sql
psql -h 192.168.122.128 -U root -d xiaomi_agent -f doc/database/schema.sql
psql -h 192.168.122.128 -U root -d xiaomi_agent -f doc/database/init.sql
```

## 3. 本地配置文件

### 3.1 bootstrap 本地配置

复制模板：

```bash
cp bootstrap/src/main/resources/application-local.yml.example bootstrap/src/main/resources/application-local.yml
```

Windows PowerShell：

```powershell
Copy-Item bootstrap/src/main/resources/application-local.yml.example bootstrap/src/main/resources/application-local.yml
```

需要重点检查/填写：

| 配置项 | 环境变量 | 是否必须 | 说明 |
| --- | --- | --- | --- |
| `spring.datasource.url` | `POSTGRES_URL` | 必填 | PostgreSQL 连接地址，默认 `jdbc:postgresql://192.168.122.128:5432/xiaomi_agent` |
| `spring.datasource.username` | `POSTGRES_USERNAME` | 必填 | PostgreSQL 用户名，本地默认可用 `root` |
| `spring.datasource.password` | `POSTGRES_PASSWORD` | 必填 | PostgreSQL 密码，本地默认可用 `root` |
| `spring.data.redis.host` | `REDIS_HOST` | 必填 | Redis 地址，默认 `192.168.122.128` |
| `spring.data.redis.port` | `REDIS_PORT` | 必填 | Redis 端口，默认 `6379` |
| `spring.ai.mcp.client.sse.connections.xiaomi-shopping.url` | `MCP_SERVER_URL` | 必填 | MCP Server 地址，默认 `http://localhost:8090` |
| `spring.autoconfigure.exclude` | - | 本地可选 | 默认模板排除 PgVectorStore 自动装配，避免未配置 EmbeddingModel 时启动失败；启用语义向量召回时删除/注释该项 |
| `xiaomi.agent.chat.api-key` | `DASHSCOPE_API_KEY` | 可选 | 不填时不装配 ChatModel，主 Agent 走本地确定性/降级能力 |
| `xiaomi.agent.embedding.api-key` | `SILICONFLOW_API_KEY` | 可选 | 不填时语义向量路降级，关键词路仍可用 |
| `xiaomi.agent.rerank.api-key` | `SILICONFLOW_API_KEY` | 可选 | 不填时降级到自研 WeightedReranker |

> `application-local.yml` 已被 `.gitignore` 忽略，不要提交真实账号密码或 API Key。

### 3.2 mcpserver 本地配置

复制模板：

```bash
cp mcpserver/src/main/resources/application-local.yml.example mcpserver/src/main/resources/application-local.yml
```

当前 `mcpserver` 不需要账号密码，只需要保证端口与 bootstrap 的 `MCP_SERVER_URL` 对齐，默认端口为 `8090`。

## 4. 一键启动

### Windows PowerShell

```powershell
powershell -ExecutionPolicy Bypass -File scripts/start-local.ps1
```

脚本会：

1. 检查 Java / Maven。
2. 如果本地配置不存在，自动从 `.example` 复制生成。
3. 启动 `mcpserver`。
4. 等待 `8090` 端口就绪。
5. 启动 `bootstrap` 主后端。

如只想启动主后端，不启动 MCP Server：

```powershell
powershell -ExecutionPolicy Bypass -File scripts/start-local.ps1 -SkipMcpServer
```

### Git Bash / Linux / macOS

```bash
bash scripts/start-local.sh
```

如只想启动主后端，不启动 MCP Server：

```bash
SKIP_MCP_SERVER=true bash scripts/start-local.sh
```

日志输出到：

- `logs/mcpserver.log`
- `logs/bootstrap.log`

## 5. 手动启动

如果不使用脚本，可以手动打开两个终端。

先打包可执行 Jar：

```bash
mvn -pl mcpserver,bootstrap -am -DskipTests package
```

终端 1：启动 MCP Server：

```bash
java -jar mcpserver/target/mcpserver-1.0.0-SNAPSHOT.jar --spring.profiles.active=local
```

终端 2：启动主后端 bootstrap：

```bash
java -jar bootstrap/target/bootstrap-1.0.0-SNAPSHOT.jar --spring.profiles.active=local
```

也可以使用 Maven 开发模式分别在模块目录启动：

```bash
mvn -f mcpserver/pom.xml spring-boot:run -Dspring-boot.run.profiles=local
mvn -f bootstrap/pom.xml spring-boot:run -Dspring-boot.run.profiles=local
```

> 注意：不要在父工程直接执行 `mvn -pl mcpserver -am spring-boot:run`，否则 Spring Boot Maven Plugin 会先尝试运行父 POM，导致找不到 main class。

## 6. 启动后验证

健康检查：

```bash
curl http://localhost:8080/api/health
```

聚合就绪检查：

```bash
curl http://localhost:8080/api/ready
```

对话入口：

```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"userId":"demo-user","conversationId":"demo-conversation","message":"小米14有什么参数"}'
```

`/api/ready` 会返回主后端、Orchestrator、KnowledgeGateway、ShoppingGateway、PostgreSQL、Redis、MCP Server 和模型 Key 的状态。如果 PostgreSQL/Redis/MCP 未启动，会显示 `DOWN` 或 `DEGRADED`，便于定位配置问题。

## 7. 配置文件汇总

| 文件 | 是否提交 | 用途 |
| --- | --- | --- |
| [bootstrap/src/main/resources/application.yml](bootstrap/src/main/resources/application.yml) | 是 | 主后端默认配置 |
| `bootstrap/src/main/resources/application-local.yml` | 否 | 主后端本地敏感配置，填写 DB/Redis/API Key |
| [bootstrap/src/main/resources/application-local.yml.example](bootstrap/src/main/resources/application-local.yml.example) | 是 | 主后端本地配置模板 |
| [mcpserver/src/main/resources/application.yml](mcpserver/src/main/resources/application.yml) | 是 | MCP Server 默认配置 |
| `mcpserver/src/main/resources/application-local.yml` | 否 | MCP Server 本地覆盖配置 |
| [mcpserver/src/main/resources/application-local.yml.example](mcpserver/src/main/resources/application-local.yml.example) | 是 | MCP Server 本地配置模板 |
| [doc/database/create-database.sql](doc/database/create-database.sql) | 是 | 创建数据库 |
| [doc/database/schema.sql](doc/database/schema.sql) | 是 | 创建表、索引、扩展 |
| [doc/database/init.sql](doc/database/init.sql) | 是 | 初始化演示数据 |

## 8. 常见问题

### 1. `/api/ready` 显示 PostgreSQL DOWN

检查：

- PostgreSQL 是否启动。
- `spring.datasource.url` 是否正确。
- `POSTGRES_USERNAME / POSTGRES_PASSWORD` 是否正确。
- 是否已执行 `doc/database` 下的初始化 SQL。

### 2. `/api/ready` 显示 Redis DOWN

检查：

- Redis 是否启动。
- `REDIS_HOST / REDIS_PORT` 是否正确。
- 当前默认 Redis 无密码；如果你的 Redis 有密码，需要在本地配置里补 `spring.data.redis.password`。

### 3. `/api/ready` 显示 MCP Server DOWN

检查：

- `mcpserver` 是否启动。
- `MCP_SERVER_URL` 是否与 MCP Server 端口一致。
- 默认地址为 `http://localhost:8090`。

### 4. `chatModel` 或 `embeddingModel` 显示 MISSING_KEY

说明对应模型 API Key 没有配置。当前项目允许无 Key 降级运行：

- `DASHSCOPE_API_KEY` 缺失：不装配 Orchestrator ChatModel。
- `SILICONFLOW_API_KEY` 缺失：本地模板默认排除 PgVectorStore 自动装配，Knowledge 走关键词路和 WeightedReranker 降级；如果要启用语义向量召回，配置 Key 后删除/注释 `spring.autoconfigure.exclude` 中的 PgVectorStore 排除项。
