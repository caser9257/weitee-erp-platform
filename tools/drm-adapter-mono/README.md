# Linux/Mono DRM 适配服务

本目录将 `drm.edi.dll` 封装为 Ubuntu Docker 可访问的文件 HTTP 服务。它保留厂商 SDK 在 Mono 中的调用方式，但接口契约与 ERP 的 `HttpDrmServiceClient` 对齐。

## 接口

- `GET /health`
- `POST /detect`
- `POST /decrypt`
- `POST /encrypt`

文件接口使用 `multipart/form-data`，文件字段名为 `file`。`/decrypt` 和 `/encrypt` 返回处理后的二进制文件；失败时返回 JSON 错误。

## 构建

将已验证的 `drm.edi.dll` 放到本目录，复制配置模板并填写真实 SDK 账号密码。配置文件只在运行时挂载，不写入镜像：

```bash
cp DrmBridge.exe.config.example DrmBridge.exe.config
cp /path/to/drm.edi.dll ./drm.edi.dll
docker build -t weitee-drm-adapter:mono .
```

不要把包含真实密码的 `DrmBridge.exe.config` 提交到 Git 或推送到公共镜像仓库。

## 启动

```bash
docker run --rm --name weitee-drm-adapter \
  -e DRM_ADAPTER_TOKEN='<LONG_RANDOM_TOKEN>' \
  -e DRM_ADAPTER_PORT=8081 \
  -v "$(pwd)/DrmBridge.exe.config:/app/DrmBridge.exe.config:ro" \
  -p 127.0.0.1:8081:8081 \
  weitee-drm-adapter:mono
```

若 ERP 在另一台 Ubuntu 主机，端口映射需要绑定 Docker 主机内网 IP，并在防火墙中只允许 ERP 主机访问。

## 测试

```bash
curl -i -H 'Authorization: Bearer <LONG_RANDOM_TOKEN>' \
  http://127.0.0.1:8081/health

curl -i -H 'Authorization: Bearer <LONG_RANDOM_TOKEN>' \
  -F 'file=@plain.xlsx' -F 'fileName=plain.xlsx' \
  http://127.0.0.1:8081/detect

curl -H 'Authorization: Bearer <LONG_RANDOM_TOKEN>' \
  -F 'file=@plain.xlsx' -F 'fileName=plain.xlsx' \
  -F 'authorId=system' -F 'departmentId=0' -F 'secretLevelId=5' \
  -F 'authUserId=system' -F 'permission=1' \
  http://127.0.0.1:8081/encrypt -o encrypted.xlsx
```

## 限制

## 已验证结果（WSL2，2026-08-20）

使用 `dootask-drm-bridge:local` 作为本地 Mono 基础镜像，已真实验证：

- `/health` 返回 `200`；
- `/detect` 可识别明文和密文；
- `/encrypt` 返回 306 字节密文；
- `/decrypt` 返回内容与原文 SHA-256 一致；
- 无效 Token 返回 `401`；
- 对明文调用 `/decrypt` 返回 `422`；
- 对已加密文件重复 `/encrypt` 内容保持不变。

正式构建仍使用 `Dockerfile` 的 `mono:6.12` 基础镜像；`Dockerfile.local-base` 只用于无法下载公共基础镜像时的本地验证。
