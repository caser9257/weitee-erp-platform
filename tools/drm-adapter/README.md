# DRM HTTP 适配服务

这个服务把现有 `drm.edi.dll` 封装为 ERP 可调用的内网 HTTP 接口。

```text
ERP Java -> POST /detect|/decrypt|/encrypt -> drm.edi.dll -> DRM Server
```

## 运行环境

- Windows Server
- .NET Framework 4.8
- IIS 10 + ASP.NET Web API 2
- 与 `drm.edi.dll` 相同的 x86/x64 位数

首次构建需要在有 .NET Framework 4.8 Developer Pack 和 MSBuild 的 Windows 服务器上执行 NuGet 还原：

```powershell
nuget restore DrmAdapter.csproj
msbuild DrmAdapter.csproj /p:Configuration=Release
```

### 当前开发机状态（2026-08-19）

- 已发现 .NET Framework 4.8 运行时，已使用本机 MSBuild 成功生成 `bin\\Weitee.DrmAdapter.dll`。
- 已还原 Web API 依赖并复制厂商 `drm.edi.dll` 到 `bin`；SDK DLL 元数据为 `drm.edi 2.0.4665.37265`，不限定 x86/x64。
- 未安装 .NET Framework 4.8 Developer Pack，因此当前构建使用 GAC 回退并有架构警告；用于正式部署前必须安装 Developer Pack 后重新构建。
- 未检测到 IIS 服务 `W3SVC` 或 IIS Express；当前尚未启动 HTTP 服务，`/health`、`/detect`、`/decrypt`、`/encrypt` 均未实际验证。

在本机测试前，先由具有管理员权限的人员安装：

1. [.NET Framework 4.8 Developer Pack](https://dotnet.microsoft.com/download/dotnet-framework/net48)。
2. Windows 功能中的“Internet Information Services”，并启用“万维网服务 -> 应用程序开发功能 -> ASP.NET 4.8”和“.NET Extensibility 4.8”。

安装后重新执行构建，并在 IIS 中创建仅监听 `127.0.0.1:8081` 的应用程序。不要将适配服务开放到公网。

构建前将已验证的 `drm.edi.dll` 复制到 `bin\drm.edi.dll`。如果 SDK 依赖其它 DLL，也必须一并复制到 `bin`。

## 部署步骤

1. 将 `drm.edi.dll` 放到 Web 应用的 `bin` 目录。
2. 将 `Web.config.example` 复制为 `Web.config`。
3. 在 `Web.config` 中配置 DRM 服务器、SDK 账号和适配服务令牌。密码只保存在服务器配置，不提交 Git。
4. 在 IIS 创建应用程序，应用程序池选择 `No Managed Code` 或 ASP.NET 4.8 托管模式，并确认位数与 DLL 一致。
5. ERP 配置 `DRM_SERVICE_URL=http://127.0.0.1:<适配服务端口>`，不要配置为 DRM 管理页面地址。

## 接口

所有接口使用 `multipart/form-data`，文件字段名为 `file`。

### `GET /health`

需要适配服务令牌。该接口会触发 SDK 初始化，返回 `{"status":"UP"}` 才表示适配程序已能加载 DLL 并连接 DRM 服务。

### `POST /detect`

返回：`{"encrypted":true}` 或 `{"encrypted":false}`。

### `POST /decrypt`

返回解密后的二进制文件。解密失败返回 `422`，不会返回部分文件。

### `POST /encrypt`

可选表单字段：`authorId`、`departmentId`、`secretLevelId`、`authUserId`、`permission`、`supportScreenWaterMark`、`supportPrintWaterMark`。

返回加密后的二进制文件。加密失败返回 `422`，不会返回部分文件。

## 本地验证

```powershell
curl.exe -H "Authorization: Bearer <token>" -F "file=@plain.xlsx" -F "fileName=plain.xlsx" http://127.0.0.1:8081/detect
curl.exe -H "Authorization: Bearer <token>" -F "file=@encrypted.xlsx" -F "fileName=encrypted.xlsx" http://127.0.0.1:8081/decrypt -o decrypted.xlsx
curl.exe -H "Authorization: Bearer <token>" -F "file=@plain.xlsx" -F "fileName=plain.xlsx" -F "authorId=system" -F "departmentId=0" -F "secretLevelId=5" -F "authUserId=system" -F "permission=1" http://127.0.0.1:8081/encrypt -o encrypted-result.xlsx
```

先使用 C# 示例程序确认 `encrypted.xlsx` 是有效密文，再接 ERP。
