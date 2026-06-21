# pgvector 连接测试程序

验证 Docker 中 PostgreSQL + pgvector 服务的连通性与 pgvector/HNSW 可用性。

## 连接目标
- Host: `192.168.122.128`
- Port: `5432`
- 数据库: `postgres`（默认，可改）
- 账号/密码: `root` / `root`

## 验证项
1. JDBC 连通性
2. PostgreSQL 版本
3. pgvector 扩展（CREATE EXTENSION vector）
4. 建 vector 测试表 + 插入向量
5. HNSW 索引创建
6. 向量相似度查询（余弦距离 `<=>`）

## 运行方式

### 方式一：Maven 直接运行（推荐）
```bash
cd test
mvn compile exec:java
```

### 方式二：打包后运行
```bash
cd test
mvn clean package
java -cp target/pgvector-connection-test-1.0-SNAPSHOT.jar:$(mvn -q dependency:build-classpath -Dmdep.outputFile=/dev/stdout) \
     com.xiaomi.shopping.test.PgVectorConnectionTest
```

## 覆盖连接参数（环境变量）
```bash
# 如需改连接参数，用环境变量覆盖：
PG_HOST=192.168.122.128 PG_PORT=5432 PG_DB=postgres PG_USER=root PG_PASSWORD=root \
mvn compile exec:java
```

## 常见问题

### 认证失败（password authentication failed / role "root" does not exist）
**PostgreSQL 官方镜像默认无 root 用户**，默认超管账户是 `postgres`。两种解决：

1. 用 postgres 账户测试：
   ```bash
   PG_USER=postgres PG_PASSWORD=<你启动容器时设的密码> mvn compile exec:java
   ```

2. 或手动创建 root 角色：
   ```bash
   docker exec -it postgres_db psql -U postgres -c "CREATE ROLE root WITH LOGIN SUPERUSER PASSWORD 'root';"
   ```

### 网络不通（connection refused / timed out）
检查虚拟机网络：
```bash
ping 192.168.122.128
telnet 192.168.122.128 5432    # Windows: 用 Test-NetConnection 192.168.122.128 -Port 5432
```
确认 Docker 端口映射 `0.0.0.0:5432->5432` 已生效，且虚拟机防火墙放行 5432。

### pg_acls / host authentication（pg_hba.conf）
若提示 `no pg_hba.conf entry`，说明客户端 IP 不在信任列表，需调整容器内 `pg_hba.conf` 放行你的网段。

## 文件结构
```
test/
├── pom.xml                                                 # Maven 最小工程
├── README.md                                               # 本文件
└── src/main/java/com/xiaomi/shopping/test/
    └── PgVectorConnectionTest.java                         # 测试主程序
```
