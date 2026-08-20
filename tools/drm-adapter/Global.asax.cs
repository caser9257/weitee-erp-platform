using System.Web.Http;

namespace Weitee.DrmAdapter
{
    public class DrmApplication : System.Web.HttpApplication
    {
        protected void Application_Start()
        {
            GlobalConfiguration.Configure(App_Start.WebApiConfig.Register);
        }
    }
}
