using System;
using System.Configuration;
using System.IO;
using cn.net.drm.edi;
using cn.net.drm.edi.client;

namespace Weitee.DrmAdapter.Services
{
    public sealed class DrmSdkService
    {
        private readonly object syncRoot = new object();
        private DrmClient client;

        public DrmSdkService()
        {
            EnsureInitialized();
        }

        public void Initialize()
        {
            EnsureInitialized();
        }

        public bool IsEncrypted(string path)
        {
            return GetClient().isEncrypted(new FileInfo(path));
        }

        public void Decrypt(string path)
        {
            GetClient().decrypt(new FileInfo(path));
        }

        public void Encrypt(string path, DrmRequestOptions options)
        {
            DateTime startTime = DateTime.Now;
            DateTime endTime = new DateTime(2099, 12, 31, 23, 59, 59);
            GetClient().encryptAuthFile(
                new FileInfo(path),
                options.AuthorId,
                options.DepartmentId,
                options.SecretLevelId,
                options.AuthUserId,
                options.Permission,
                ref startTime,
                ref endTime,
                options.SupportScreenWaterMark,
                options.SupportPrintWaterMark);
        }

        private DrmClient GetClient()
        {
            EnsureInitialized();
            return client;
        }

        private void EnsureInitialized()
        {
            if (client != null)
            {
                return;
            }

            lock (syncRoot)
            {
                if (client != null)
                {
                    return;
                }

                // DrmEdiClient reads drm.serverip/drm.port/drm.user/drm.password from Web.config,
                // matching the vendor Windows sample configuration.
                client = DrmEdiClient.Instance;
                if (client == null)
                {
                    throw new InvalidOperationException("DRM SDK 初始化失败");
                }
            }
        }

        public static string Setting(string key, string defaultValue = null)
        {
            return ConfigurationManager.AppSettings[key] ?? defaultValue;
        }
    }

    public sealed class DrmRequestOptions
    {
        public string AuthorId { get; set; }
        public int DepartmentId { get; set; }
        public int SecretLevelId { get; set; }
        public string AuthUserId { get; set; }
        public int Permission { get; set; }
        public bool SupportScreenWaterMark { get; set; }
        public bool SupportPrintWaterMark { get; set; }
    }
}
