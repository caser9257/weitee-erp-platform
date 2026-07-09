# Docker Build & Up

目标：在内网机器上快速部署微泰 ERP 体验环境，包含 MySQL、Redis、后端服务和前端站点。

## 文件清单

```text
script/docker/
├── Docker-HOWTO.md
├── docker-compose.yml
└── docker.env

weitee-server/
└── Dockerfile

weitee-ui/weitee-ui-admin-vue3/
├── .dockerignore
├── Dockerfile
└── nginx.conf

production/ruoyi-vue-pro/
└── weitee-erp_backup_20260709.sql
```

## 数据库初始化

`docker-compose.yml` 默认使用 `production/ruoyi-vue-pro/weitee-erp_backup_20260709.sql` 初始化 `weitee-erp` 数据库。

MySQL 官方镜像只会在数据目录为空时执行 `/docker-entrypoint-initdb.d/*.sql`：

- 新内网机器或新 Docker volume：首次启动会自动导入体验库。
- 已启动过的 MySQL volume：不会再次自动导入 SQL。
- 需要替换旧体验库时，先备份旧库，再重建 MySQL 数据目录或手动恢复 SQL。

## 后端构建

后端镜像会在 Docker 构建阶段自动执行 Maven 打包，不再要求在目标机器上先手工生成 `weitee-server.jar`。Docker 构建使用仓库根目录作为上下文，`weitee-server/Dockerfile` 会在 builder 阶段执行：

```shell
mvn -pl weitee-server -am -Dmaven.test.skip=true package
```

因此目标机器只需要具备 Docker 构建能力，不需要先在宿主机执行 Maven 预打包；宿主机上的 `weitee-server/target/` 也不会作为镜像输入。

## 启动服务

启动前先执行预检。预检会确认体验库 SQL 存在，并阻断“已有 MySQL volume 但误以为会重新导入 SQL”的情况。

Linux / WSL：

```shell
./preflight-intranet.sh
```

Windows PowerShell：

```powershell
.\preflight-intranet.ps1
```

预检通过后启动：

```shell
cd script/docker
docker compose --env-file docker.env up -d --build
```

首次运行会自动构建容器。也可以通过下面命令单独构建服务：

```shell
docker compose --env-file docker.env build server
docker compose --env-file docker.env build admin
```

## 已有库手动恢复

仅在确认要用 `20260709` 体验库替换当前 `weitee-erp` 库时使用。恢复脚本默认拒绝执行，必须显式确认；脚本会先备份当前数据库，再导入体验库 SQL。

Linux shell：

```shell
CONFIRM_RESTORE=1 ./restore-experience-db.sh
```

Windows PowerShell：

```powershell
.\restore-experience-db.ps1 -ConfirmRestore
```

脚本会把备份写入：

```text
production/ruoyi-vue-pro/backups/
```

## 隔离验证

如果本机已经存在 `weitee-system_mysql` 或历史 smoke 数据卷，不要用正式 compose 直接判断初始化是否可用。使用隔离验证 compose，它会使用独立容器名、端口和 volume：

```shell
cd script/docker
docker compose --env-file docker.env -f docker-compose.yml -f docker-compose.verify.yml up -d --build
```

隔离验证入口：

```text
前端: http://localhost:28081
后端: http://localhost:58082
MySQL: 127.0.0.1:23307
Redis: 127.0.0.1:26380
```

## 访问入口

- 前端入口：`http://内网服务器IP:8080`
- 后端接口：`http://内网服务器IP:48080`
- MySQL：`root / 123456`，端口 `3306`
- Redis：端口 `6379`
- 体验账号：`superadmin / 123456`

## 验证清单

```shell
docker compose --env-file docker.env ps
docker logs --tail=200 weitee-server
docker logs --tail=100 weitee-admin
```

浏览器验证：

- 打开 `http://内网服务器IP:8080`
- 使用 `superadmin / 123456` 登录
- 访问 `/scm/stock-in`、`/scm/stock-out`
- 访问 `/finance/payment`、`/finance/expense`
- 提交其它入库或其它出库审批，确认业务单据写入 `process_instance_id`

## 兼容说明

- 后端 Docker 运行镜像为 Java 21，需与项目 `pom.xml` 的 Java 版本一致。
- 启动后端时，内置 BPM 初始化器会补齐财务、采购退货、其它入库、其它出库的 BPMN 流程定义和扩展信息。
- 如果 MySQL volume 已经存在旧数据，`preflight-intranet.*` 会阻断首次启动路径，避免误判 SQL 会自动重新导入。
