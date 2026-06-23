#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BOOTSTRAP_LOCAL="$ROOT/bootstrap/src/main/resources/application-local.yml"
BOOTSTRAP_EXAMPLE="$ROOT/bootstrap/src/main/resources/application-local.yml.example"
MCP_LOCAL="$ROOT/mcpserver/src/main/resources/application-local.yml"
MCP_EXAMPLE="$ROOT/mcpserver/src/main/resources/application-local.yml.example"
SKIP_MCP="${SKIP_MCP_SERVER:-false}"

require_command() {
  if ! command -v "$1" >/dev/null 2>&1; then
    echo "未找到命令：$1，请先安装并配置 PATH。" >&2
    exit 1
  fi
}

ensure_local_config() {
  local target="$1"
  local example="$2"
  if [ ! -f "$target" ]; then
    cp "$example" "$target"
    echo "已生成本地配置：$target"
    echo "请确认其中的 PostgreSQL / Redis / API Key 配置是否符合本机环境。"
  fi
}

wait_port() {
  local port="$1"
  local name="$2"
  echo "等待 $name 端口 $port 就绪..."
  for _ in $(seq 1 60); do
    if (echo >/dev/tcp/127.0.0.1/"$port") >/dev/null 2>&1; then
      echo "$name 已就绪：127.0.0.1:$port"
      return 0
    fi
    sleep 1
  done
  echo "$name 端口 $port 在 60 秒内未就绪，请查看 logs/${name}.log。" >&2
  return 1
}

require_command java
require_command mvn
ensure_local_config "$BOOTSTRAP_LOCAL" "$BOOTSTRAP_EXAMPLE"
ensure_local_config "$MCP_LOCAL" "$MCP_EXAMPLE"

mkdir -p "$ROOT/logs"
cd "$ROOT"

echo "打包 bootstrap 与 mcpserver 可执行 Jar..."
mvn -pl mcpserver,bootstrap -am -DskipTests package

if [ "$SKIP_MCP" != "true" ]; then
  echo "启动 mcpserver（端口 8090）..."
  nohup java -jar "$ROOT/mcpserver/target/mcpserver-1.0.0-SNAPSHOT.jar" --spring.profiles.active=local > "$ROOT/logs/mcpserver.log" 2>&1 &
  echo $! > "$ROOT/logs/mcpserver.pid"
  wait_port 8090 "mcpserver"
fi

echo "启动 bootstrap 主后端（端口 8080）..."
nohup java -jar "$ROOT/bootstrap/target/bootstrap-1.0.0-SNAPSHOT.jar" --spring.profiles.active=local > "$ROOT/logs/bootstrap.log" 2>&1 &
echo $! > "$ROOT/logs/bootstrap.pid"

cat <<'MSG'
启动命令已发出，请查看 logs/bootstrap.log 和 logs/mcpserver.log。
验证地址：
  GET  http://localhost:8080/api/health
  GET  http://localhost:8080/api/ready
  POST http://localhost:8080/api/chat
MSG
