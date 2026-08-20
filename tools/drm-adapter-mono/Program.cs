using System;
using System.Collections.Generic;
using System.Collections.Specialized;
using System.IO;
using System.Net;
using System.Text;
using System.Web.Script.Serialization;
using cn.net.drm.edi;

namespace Weitee.DrmAdapterMono
{
    internal static class Program
    {
        private static readonly JavaScriptSerializer Json = new JavaScriptSerializer();
        private static readonly object ClientLock = new object();
        private static cn.net.drm.edi.client.DrmClient client = DrmEdiClient.Instance;

        private static int Main(string[] args)
        {
            try
            {
                int port = ReadIntEnvironment("DRM_ADAPTER_PORT", 8081);
                string prefix = "http://+:" + port + "/";
                var listener = new HttpListener();
                listener.Prefixes.Add(prefix);
                listener.Start();
                Console.WriteLine("DRM adapter listening on port " + port);

                while (true)
                {
                    try
                    {
                        Handle(listener.GetContext());
                    }
                    catch (Exception ex)
                    {
                        Console.Error.WriteLine("Request failed: " + ex.Message);
                    }
                }
            }
            catch (Exception ex)
            {
                Console.Error.WriteLine("DRM adapter startup failed: " + ex.Message);
                return 1;
            }
        }

        private static void Handle(HttpListenerContext context)
        {
            try
            {
                Authorize(context);
                string path = context.Request.Url.AbsolutePath.TrimEnd('/').ToLowerInvariant();
                if (context.Request.HttpMethod == "GET" && path == "/health")
                {
                    GetClient();
                    WriteJson(context, 200, new { status = "UP" });
                    return;
                }

                if (context.Request.HttpMethod != "POST")
                {
                    WriteJson(context, 405, new { message = "仅支持指定的 HTTP 方法" });
                    return;
                }

                if (path == "/detect")
                {
                    HandleDetect(context);
                }
                else if (path == "/decrypt")
                {
                    HandleDecrypt(context);
                }
                else if (path == "/encrypt")
                {
                    HandleEncrypt(context);
                }
                else
                {
                    WriteJson(context, 404, new { message = "接口不存在" });
                }
            }
            catch (AdapterException ex)
            {
                WriteJson(context, ex.StatusCode, new { message = ex.Message });
            }
            catch (Exception ex)
            {
                Console.Error.WriteLine(ex.ToString());
                WriteJson(context, 502, new { message = "DRM 服务处理失败" });
            }
        }

        private static void HandleDetect(HttpListenerContext context)
        {
            UploadedFile file = null;
            try
            {
                file = ReadUpload(context.Request);
                bool encrypted = GetClient().isEncrypted(new FileInfo(file.Path));
                WriteJson(context, 200, new { encrypted = encrypted });
            }
            finally
            {
                DeleteDirectory(file == null ? null : file.Directory);
            }
        }

        private static void HandleDecrypt(HttpListenerContext context)
        {
            UploadedFile file = null;
            try
            {
                file = ReadUpload(context.Request);
                var info = new FileInfo(file.Path);
                if (!GetClient().isEncrypted(info))
                {
                    throw new AdapterException(422, "文件未加密");
                }
                GetClient().decrypt(info);
                WriteFile(context, file.Path, file.FileName);
            }
            finally
            {
                DeleteDirectory(file == null ? null : file.Directory);
            }
        }

        private static void HandleEncrypt(HttpListenerContext context)
        {
            UploadedFile file = null;
            try
            {
                file = ReadUpload(context.Request);
                var info = new FileInfo(file.Path);
                if (!GetClient().isEncrypted(info))
                {
                    NameValueCollection form = file.Form;
                    DateTime start = DateTime.Now;
                    DateTime end = new DateTime(2099, 12, 31, 23, 59, 59);
                    long result = GetClient().encryptAuthFile(
                        info,
                        FormValue(form, "authorId", "system"),
                        FormInt(form, "departmentId", 0),
                        FormInt(form, "secretLevelId", 5),
                        FormValue(form, "authUserId", "system"),
                        FormInt(form, "permission", 1),
                        ref start,
                        ref end,
                        FormBool(form, "supportScreenWaterMark", true),
                        FormBool(form, "supportPrintWaterMark", false));
                    info = new FileInfo(file.Path);
                }
                if (!GetClient().isEncrypted(new FileInfo(file.Path)))
                {
                    throw new AdapterException(422, "DRM 加密后未检测到有效密文");
                }
                WriteFile(context, file.Path, file.FileName);
            }
            finally
            {
                DeleteDirectory(file == null ? null : file.Directory);
            }
        }

        private static UploadedFile ReadUpload(HttpListenerRequest request)
        {
            long maxSize = ReadLongEnvironment("DRM_MAX_FILE_SIZE", 500L * 1024 * 1024);
            if (request.ContentLength64 < 0 || request.ContentLength64 > maxSize + 1024 * 1024)
            {
                throw new AdapterException(413, "文件超过大小限制");
            }

            string directory = Path.Combine(Path.GetTempPath(), "weitee-drm", Guid.NewGuid().ToString("N"));
            Directory.CreateDirectory(directory);
            try
            {
                byte[] body = ReadBody(request.InputStream, maxSize + 1024 * 1024);
                string contentType = request.ContentType ?? "";
                string fileName;
                NameValueCollection form;
                byte[] content = contentType.StartsWith("multipart/form-data", StringComparison.OrdinalIgnoreCase)
                    ? ParseMultipart(body, contentType, maxSize, out fileName, out form)
                    : ParseRaw(body, request.Headers["X-File-Name"], maxSize, out fileName, out form);

                string path = Path.Combine(directory, SafeFileName(fileName));
                File.WriteAllBytes(path, content);
                return new UploadedFile(directory, path, SafeFileName(fileName), form);
            }
            catch
            {
                DeleteDirectory(directory);
                throw;
            }
        }

        private static byte[] ParseRaw(byte[] body, string requestedName, long maxSize, out string fileName, out NameValueCollection form)
        {
            CheckFileSize(body, maxSize);
            fileName = string.IsNullOrWhiteSpace(requestedName) ? "file.bin" : requestedName;
            form = new NameValueCollection(StringComparer.OrdinalIgnoreCase);
            return body;
        }

        private static byte[] ParseMultipart(byte[] body, string contentType, long maxSize, out string fileName, out NameValueCollection form)
        {
            string boundary = GetBoundary(contentType);
            byte[] marker = Encoding.ASCII.GetBytes("--" + boundary);
            int cursor = 0;
            fileName = null;
            form = new NameValueCollection(StringComparer.OrdinalIgnoreCase);
            byte[] selected = null;

            while (cursor < body.Length)
            {
                int start = IndexOf(body, marker, cursor);
                if (start < 0) break;
                int partStart = start + marker.Length;
                if (partStart + 1 < body.Length && body[partStart] == '-' && body[partStart + 1] == '-') break;
                partStart = SkipCrlf(body, partStart);
                int headerEnd = IndexOf(body, Encoding.ASCII.GetBytes("\r\n\r\n"), partStart);
                if (headerEnd < 0) throw new AdapterException(400, "multipart 请求格式错误");
                string headers = Encoding.UTF8.GetString(body, partStart, headerEnd - partStart);
                int contentStart = headerEnd + 4;
                int next = IndexOf(body, marker, contentStart);
                if (next < 0) throw new AdapterException(400, "multipart 请求缺少结束边界");
                int contentEnd = next - 2;
                string disposition = HeaderValue(headers, "Content-Disposition");
                string name = DispositionValue(disposition, "name");
                string partFileName = DispositionValue(disposition, "filename");
                int length = Math.Max(0, contentEnd - contentStart);
                if (!string.IsNullOrWhiteSpace(partFileName) && selected == null)
                {
                    selected = new byte[length];
                    Buffer.BlockCopy(body, contentStart, selected, 0, length);
                    fileName = partFileName;
                    CheckFileSize(selected, maxSize);
                }
                else if (!string.IsNullOrWhiteSpace(name))
                {
                    form[name] = Encoding.UTF8.GetString(body, contentStart, length);
                }
                cursor = next;
            }

            if (selected == null) throw new AdapterException(400, "缺少 file 文件字段");
            return selected;
        }

        private static byte[] ReadBody(Stream stream, long maxSize)
        {
            using (var output = new MemoryStream())
            {
                byte[] buffer = new byte[81920];
                int read;
                while ((read = stream.Read(buffer, 0, buffer.Length)) > 0)
                {
                    if (output.Length + read > maxSize) throw new AdapterException(413, "文件超过大小限制");
                    output.Write(buffer, 0, read);
                }
                return output.ToArray();
            }
        }

        private static cn.net.drm.edi.client.DrmClient GetClient()
        {
            if (client != null) return client;
            lock (ClientLock)
            {
                if (client == null) client = DrmEdiClient.Instance;
                if (client == null) throw new AdapterException(502, "DRM SDK 初始化失败");
                return client;
            }
        }

        private static void Authorize(HttpListenerContext context)
        {
            string expected = Environment.GetEnvironmentVariable("DRM_ADAPTER_TOKEN");
            if (string.IsNullOrWhiteSpace(expected)) throw new AdapterException(500, "未配置适配服务令牌");
            string actual = context.Request.Headers["Authorization"] ?? "";
            if (!actual.StartsWith("Bearer ", StringComparison.OrdinalIgnoreCase) ||
                !string.Equals(actual.Substring(7).Trim(), expected.Trim(), StringComparison.Ordinal))
            {
                throw new AdapterException(401, "DRM 适配服务认证失败");
            }
        }

        private static void WriteFile(HttpListenerContext context, string path, string fileName)
        {
            byte[] content = File.ReadAllBytes(path);
            context.Response.StatusCode = 200;
            context.Response.ContentType = "application/octet-stream";
            context.Response.AddHeader("Content-Disposition", "attachment; filename=\"" + SafeFileName(fileName) + "\"");
            context.Response.ContentLength64 = content.Length;
            context.Response.OutputStream.Write(content, 0, content.Length);
            context.Response.OutputStream.Close();
        }

        private static void WriteJson(HttpListenerContext context, int statusCode, object value)
        {
            byte[] body = Encoding.UTF8.GetBytes(Json.Serialize(value));
            context.Response.StatusCode = statusCode;
            context.Response.ContentType = "application/json; charset=utf-8";
            context.Response.ContentLength64 = body.Length;
            context.Response.OutputStream.Write(body, 0, body.Length);
            context.Response.OutputStream.Close();
        }

        private static string GetBoundary(string contentType)
        {
            foreach (string item in contentType.Split(';'))
            {
                string value = item.Trim();
                if (value.StartsWith("boundary=", StringComparison.OrdinalIgnoreCase)) return value.Substring(9).Trim('"');
            }
            throw new AdapterException(400, "multipart 请求缺少 boundary");
        }

        private static string HeaderValue(string headers, string name)
        {
            foreach (string line in headers.Split(new[] { "\r\n" }, StringSplitOptions.RemoveEmptyEntries))
            {
                int colon = line.IndexOf(':');
                if (colon > 0 && line.Substring(0, colon).Trim().Equals(name, StringComparison.OrdinalIgnoreCase)) return line.Substring(colon + 1).Trim();
            }
            return "";
        }

        private static string DispositionValue(string header, string name)
        {
            foreach (string item in header.Split(';'))
            {
                string[] pair = item.Trim().Split(new[] { '=' }, 2);
                if (pair.Length == 2 && pair[0].Trim().Equals(name, StringComparison.OrdinalIgnoreCase)) return pair[1].Trim().Trim('"');
            }
            return null;
        }

        private static int IndexOf(byte[] source, byte[] value, int start)
        {
            for (int i = start; i <= source.Length - value.Length; i++)
            {
                int j = 0;
                while (j < value.Length && source[i + j] == value[j]) j++;
                if (j == value.Length) return i;
            }
            return -1;
        }

        private static int SkipCrlf(byte[] body, int position)
        {
            return position + 2 <= body.Length && body[position] == '\r' && body[position + 1] == '\n' ? position + 2 : position;
        }

        private static void CheckFileSize(byte[] content, long maxSize)
        {
            if (content == null || content.Length == 0) throw new AdapterException(400, "文件内容为空");
            if (content.Length > maxSize) throw new AdapterException(413, "文件超过大小限制");
        }

        private static string SafeFileName(string fileName)
        {
            string value = Path.GetFileName(fileName ?? "file.bin");
            return string.IsNullOrWhiteSpace(value) ? "file.bin" : value;
        }

        private static string FormValue(NameValueCollection form, string key, string fallback) { return string.IsNullOrWhiteSpace(form[key]) ? fallback : form[key]; }
        private static int FormInt(NameValueCollection form, string key, int fallback) { int value; return int.TryParse(form[key], out value) ? value : fallback; }
        private static bool FormBool(NameValueCollection form, string key, bool fallback) { bool value; return bool.TryParse(form[key], out value) ? value : fallback; }
        private static int ReadIntEnvironment(string key, int fallback) { int value; return int.TryParse(Environment.GetEnvironmentVariable(key), out value) && value > 0 ? value : fallback; }
        private static long ReadLongEnvironment(string key, long fallback) { long value; return long.TryParse(Environment.GetEnvironmentVariable(key), out value) && value > 0 ? value : fallback; }

        private static void DeleteDirectory(string path)
        {
            if (string.IsNullOrWhiteSpace(path) || !Directory.Exists(path)) return;
            try { Directory.Delete(path, true); } catch { }
        }
    }

    internal sealed class UploadedFile
    {
        public UploadedFile(string directory, string path, string fileName, NameValueCollection form) { Directory = directory; Path = path; FileName = fileName; Form = form; }
        public string Directory { get; private set; }
        public string Path { get; private set; }
        public string FileName { get; private set; }
        public NameValueCollection Form { get; private set; }
    }

    internal sealed class AdapterException : Exception
    {
        public AdapterException(int statusCode, string message) : base(message) { StatusCode = statusCode; }
        public int StatusCode { get; private set; }
    }
}
