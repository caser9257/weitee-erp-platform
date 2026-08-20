using System;
using System.Collections.Specialized;
using System.Configuration;
using System.IO;
using System.Net;
using System.Net.Http;
using System.Net.Http.Headers;
using System.Net.Http.Formatting;
using System.Threading.Tasks;
using System.Web.Http;
using Weitee.DrmAdapter.Services;

namespace Weitee.DrmAdapter.Controllers
{
    [RoutePrefix("")]
    public sealed class DrmController : ApiController
    {
        private static readonly Lazy<DrmSdkService> Sdk = new Lazy<DrmSdkService>();

        [HttpGet, Route("health")]
        public IHttpActionResult Health()
        {
            try
            {
                AuthorizeRequest();
                Sdk.Value.Initialize();
                return Ok(new { status = "UP" });
            }
            catch (DrmAdapterException ex)
            {
                return ResponseMessage(Request.CreateErrorResponse(ex.StatusCode, ex.Message));
            }
            catch (Exception)
            {
                return ResponseMessage(Request.CreateErrorResponse(HttpStatusCode.BadGateway, "DRM SDK 初始化失败"));
            }
        }

        [HttpPost, Route("detect")]
        public async Task<IHttpActionResult> Detect()
        {
            return await ExecuteFileOperation(upload =>
            {
                bool encrypted = Sdk.Value.IsEncrypted(upload.Path);
                return Task.FromResult<IHttpActionResult>(Json(new { encrypted }));
            });
        }

        [HttpPost, Route("decrypt")]
        public async Task<HttpResponseMessage> Decrypt()
        {
            return await ExecuteBinaryOperation(upload =>
            {
                if (!Sdk.Value.IsEncrypted(upload.Path))
                {
                    throw new DrmAdapterException((HttpStatusCode)422, "文件未加密");
                }
                Sdk.Value.Decrypt(upload.Path);
                return Task.FromResult(CreateFileResponse(File.ReadAllBytes(upload.Path), upload.FileName));
            });
        }

        [HttpPost, Route("encrypt")]
        public async Task<HttpResponseMessage> Encrypt()
        {
            return await ExecuteBinaryOperation(upload =>
            {
                if (!Sdk.Value.IsEncrypted(upload.Path))
                {
                    Sdk.Value.Encrypt(upload.Path, ReadOptions(upload.FormData));
                }
                if (!Sdk.Value.IsEncrypted(upload.Path))
                {
                    throw new DrmAdapterException((HttpStatusCode)422, "DRM 加密后未检测到有效密文");
                }
                return Task.FromResult(CreateFileResponse(File.ReadAllBytes(upload.Path), upload.FileName));
            });
        }

        private async Task<IHttpActionResult> ExecuteFileOperation(Func<UploadedFile, Task<IHttpActionResult>> operation)
        {
            string tempDirectory = null;
            try
            {
                AuthorizeRequest();
                UploadedFile upload = await SaveUploadAsync();
                tempDirectory = upload.Directory;
                return await operation(upload);
            }
            catch (DrmAdapterException ex)
            {
                return ResponseMessage(Request.CreateErrorResponse(ex.StatusCode, ex.Message));
            }
            catch (Exception)
            {
                return ResponseMessage(Request.CreateErrorResponse(HttpStatusCode.BadGateway, "DRM 服务处理失败"));
            }
            finally
            {
                DeleteDirectory(tempDirectory);
            }
        }

        private async Task<HttpResponseMessage> ExecuteBinaryOperation(Func<UploadedFile, Task<HttpResponseMessage>> operation)
        {
            string tempDirectory = null;
            try
            {
                AuthorizeRequest();
                UploadedFile upload = await SaveUploadAsync();
                tempDirectory = upload.Directory;
                return await operation(upload);
            }
            catch (DrmAdapterException ex)
            {
                return Request.CreateErrorResponse(ex.StatusCode, ex.Message);
            }
            catch (Exception)
            {
                return Request.CreateErrorResponse(HttpStatusCode.BadGateway, "DRM 服务处理失败");
            }
            finally
            {
                DeleteDirectory(tempDirectory);
            }
        }

        private async Task<UploadedFile> SaveUploadAsync()
        {
            if (!Request.Content.IsMimeMultipartContent())
            {
                throw new DrmAdapterException(HttpStatusCode.BadRequest, "请求必须使用 multipart/form-data");
            }

            string tempDirectory = Path.Combine(Path.GetTempPath(), "weitee-drm", Guid.NewGuid().ToString("N"));
            try
            {
                Directory.CreateDirectory(tempDirectory);
                var provider = new MultipartFormDataStreamProvider(tempDirectory);
                await Request.Content.ReadAsMultipartAsync(provider);
                if (provider.FileData.Count == 0)
                {
                    throw new DrmAdapterException(HttpStatusCode.BadRequest, "缺少 file 文件字段");
                }

                string uploaded = provider.FileData[0].LocalFileName;
                long maxSize = ReadLongSetting("drm.adapter.maxFileSize", 500L * 1024 * 1024);
                if (new FileInfo(uploaded).Length > maxSize)
                {
                    throw new DrmAdapterException((HttpStatusCode)413, "文件超过大小限制");
                }

                string fileName = provider.FormData["fileName"] ?? Path.GetFileName(provider.FileData[0].Headers.ContentDisposition.FileName.Trim('"'));
                fileName = Path.GetFileName(fileName);
                if (string.IsNullOrWhiteSpace(fileName))
                {
                    fileName = "file.bin";
                }
                string target = Path.Combine(tempDirectory, fileName);
                File.Copy(uploaded, target, true);
                return new UploadedFile(tempDirectory, target, fileName, provider.FormData);
            }
            catch
            {
                DeleteDirectory(tempDirectory);
                throw;
            }
        }

        private DrmRequestOptions ReadOptions(NameValueCollection formData)
        {
            return new DrmRequestOptions
            {
                AuthorId = FormValue(formData, "authorId", "system"),
                DepartmentId = FormInt(formData, "departmentId", 0),
                SecretLevelId = FormInt(formData, "secretLevelId", 5),
                AuthUserId = FormValue(formData, "authUserId", "system"),
                Permission = FormInt(formData, "permission", 1),
                SupportScreenWaterMark = FormBool(formData, "supportScreenWaterMark", false),
                SupportPrintWaterMark = FormBool(formData, "supportPrintWaterMark", false)
            };
        }

        private static string FormValue(NameValueCollection formData, string key, string defaultValue)
        {
            return formData[key] ?? defaultValue;
        }

        private static int FormInt(NameValueCollection formData, string key, int defaultValue)
        {
            int value;
            return int.TryParse(FormValue(formData, key, null), out value) ? value : defaultValue;
        }

        private static bool FormBool(NameValueCollection formData, string key, bool defaultValue)
        {
            bool value;
            return bool.TryParse(FormValue(formData, key, null), out value) ? value : defaultValue;
        }

        private void AuthorizeRequest()
        {
            bool allowAnonymous;
            bool.TryParse(ConfigurationManager.AppSettings["drm.adapter.allowAnonymous"], out allowAnonymous);
            if (allowAnonymous)
            {
                return;
            }
            string expected = ConfigurationManager.AppSettings["drm.adapter.token"];
            string actual = Request.Headers.Authorization == null ? null : Request.Headers.Authorization.Parameter;
            if (string.IsNullOrWhiteSpace(expected) || !string.Equals(expected, actual, StringComparison.Ordinal))
            {
                throw new DrmAdapterException(HttpStatusCode.Unauthorized, "DRM 适配服务认证失败");
            }
        }

        private HttpResponseMessage CreateFileResponse(byte[] content, string fileName)
        {
            var response = Request.CreateResponse(HttpStatusCode.OK);
            response.Content = new ByteArrayContent(content);
            response.Content.Headers.ContentType = new MediaTypeHeaderValue("application/octet-stream");
            response.Content.Headers.ContentDisposition = new ContentDispositionHeaderValue("attachment") { FileName = fileName };
            return response;
        }

        private static long ReadLongSetting(string key, long fallback)
        {
            long value;
            return long.TryParse(ConfigurationManager.AppSettings[key], out value) && value > 0 ? value : fallback;
        }

        private static void DeleteDirectory(string path)
        {
            if (string.IsNullOrWhiteSpace(path) || !Directory.Exists(path)) return;
            try { Directory.Delete(path, true); } catch { }
        }

        private sealed class UploadedFile
        {
            public UploadedFile(string directory, string path, string fileName, NameValueCollection formData)
            {
                Directory = directory;
                Path = path;
                FileName = fileName;
                FormData = formData;
            }

            public string Directory { get; private set; }
            public string Path { get; private set; }
            public string FileName { get; private set; }
            public NameValueCollection FormData { get; private set; }
        }
    }

    public sealed class DrmAdapterException : Exception
    {
        public HttpStatusCode StatusCode { get; private set; }

        public DrmAdapterException(HttpStatusCode statusCode, string message) : base(message)
        {
            StatusCode = statusCode;
        }
    }
}
