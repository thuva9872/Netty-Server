//import io.undertow.Undertow;
//import io.undertow.UndertowOptions;
//import io.undertow.util.Headers;
//import io.undertow.util.HttpString;
////import jakarta.servlet.Servlet;
//import jakarta.servlet.ServletException;
////import jakarta.servlet.http.HttpServlet;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;

import com.google.common.reflect.ClassPath;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Set;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class Http2Test {
    TrustManager TRUST_ALL_CERTS = new X509TrustManager() {
        @Override
        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {
        }

        @Override
        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {
        }

        @Override
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return new java.security.cert.X509Certificate[] {};
        }
    };

    @Test
    public void testHttp2UsingOkHttp() throws IOException, NoSuchAlgorithmException, KeyManagementException, URISyntaxException {
//        ClassPath classPath = ClassPath.from(this.getClass().getClassLoader());
//        Set<ClassPath.ClassInfo> classes = classPath.getAllClasses();
//        classes.forEach(i->{
//            System.out.println(i.getName());
//        });
        Server server=new Server();
        server.startSSLServer();

        OkHttpClient.Builder builder =
                        new OkHttpClient.Builder();

                SSLContext sslContext = SSLContext.getInstance("SSL");
                sslContext.init(null, new TrustManager[] { TRUST_ALL_CERTS }, new java.security.SecureRandom());
                builder.sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) TRUST_ALL_CERTS);

                builder.hostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                });

                OkHttpClient client=builder.build();
                Request request = new Request.Builder().url("https://localhost:8080").get().build();
                Response response = client.newCall(request).execute();
                Assert.assertEquals(response.protocol(), Protocol.HTTP_2);
                Assert.assertEquals(response.body().string(),"2.0");
                server.stop();
    }
}
