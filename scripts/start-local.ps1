param(
    [switch]$SkipMcpServer
)

$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot
$BootstrapLocal = Join-Path $Root "bootstrap/src/main/resources/application-local.yml"
$BootstrapExample = Join-Path $Root "bootstrap/src/main/resources/application-local.yml.example"
$McpLocal = Join-Path $Root "mcpserver/src/main/resources/application-local.yml"
$McpExample = Join-Path $Root "mcpserver/src/main/resources/application-local.yml.example"

function Assert-Command($Name) {
    if (-not (Get-Command $Name -ErrorAction SilentlyContinue)) {
        throw "未找到命令：$Name，请先安装并配置 PATH。"
    }
}

function Ensure-LocalConfig($Target, $Example) {
    if (-not (Test-Path $Target)) {
        Copy-Item $Example $Target
        Write-Host "已生成本地配置：$Target" -ForegroundColor Yellow
        Write-Host "请确认其中的 PostgreSQL / Redis / API Key 配置是否符合本机环境。" -ForegroundColor Yellow
    }
}

function Wait-Port($Port, $Name) {
    Write-Host "等待 $Name 端口 $Port 就绪..."
    for ($i = 0; $i -lt 60; $i++) {
        $tcp = New-Object Net.Sockets.TcpClient
        try {
            $tcp.Connect("127.0.0.1", $Port)
            $tcp.Close()
            Write-Host "$Name 已就绪：127.0.0.1:$Port" -ForegroundColor Green
            return
        } catch {
            Start-Sleep -Seconds 1
        } finally {
            $tcp.Dispose()
        }
    }
    throw "$Name 端口 $Port 在 60 秒内未就绪，请查看对应启动窗口日志。"
}

Assert-Command "java"
Assert-Command "mvn"
Ensure-LocalConfig $BootstrapLocal $BootstrapExample
Ensure-LocalConfig $McpLocal $McpExample

Set-Location $Root

Write-Host "打包 bootstrap 与 mcpserver 可执行 Jar..." -ForegroundColor Cyan
mvn -pl mcpserver,bootstrap -am -DskipTests package

if (-not $SkipMcpServer) {
    Write-Host "启动 mcpserver（端口 8090）..." -ForegroundColor Cyan
    Start-Process powershell -ArgumentList @(
        "-NoExit",
        "-Command",
        "cd '$Root'; java -jar mcpserver/target/mcpserver-1.0.0-SNAPSHOT.jar --spring.profiles.active=local"
    )
    Wait-Port 8090 "mcpserver"
}

Write-Host "启动 bootstrap 主后端（端口 8080）..." -ForegroundColor Cyan
Start-Process powershell -ArgumentList @(
    "-NoExit",
    "-Command",
    "cd '$Root'; java -jar bootstrap/target/bootstrap-1.0.0-SNAPSHOT.jar --spring.profiles.active=local"
)

Write-Host ""
Write-Host "启动命令已发出，请在新打开的窗口查看日志。" -ForegroundColor Green
Write-Host "验证地址："
Write-Host "  GET  http://localhost:8080/api/health"
Write-Host "  GET  http://localhost:8080/api/ready"
Write-Host "  POST http://localhost:8080/api/chat"
