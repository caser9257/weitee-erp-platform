import java.lang.reflect.*;
public class ParamCheck {
  public static void main(String[] args) throws Exception {
    Class<?> c = Class.forName("cn.iocoder.yudao.module.system.service.oauth2.OAuth2ClientServiceImpl");
    for (Method m : c.getDeclaredMethods()) {
      if (m.getName().equals("getOAuth2ClientFromCache")) {
        for (Parameter p : m.getParameters()) {
          System.out.println(p.getName() + ":" + p.isNamePresent());
        }
      }
    }
  }
}
