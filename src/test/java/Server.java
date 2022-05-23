import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.HttpConnectorListener;
import org.wso2.transport.http.netty.contract.HttpWsConnectorFactory;
import org.wso2.transport.http.netty.contract.ServerConnector;
import org.wso2.transport.http.netty.contract.ServerConnectorFuture;
import org.wso2.transport.http.netty.contract.config.ListenerConfiguration;
import org.wso2.transport.http.netty.contract.config.ServerBootstrapConfiguration;
import org.wso2.transport.http.netty.contract.exceptions.ServerConnectorException;
import org.wso2.transport.http.netty.contractimpl.DefaultHttpWsConnectorFactory;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;
import org.wso2.transport.http.netty.message.HttpCarbonResponse;

import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.HashMap;

public class Server {
    static Logger log= LoggerFactory.getLogger(Server.class);
    HttpWsConnectorFactory httpWsConnectorFactory=new  DefaultHttpWsConnectorFactory();
    private ServerConnector serverConnector;
    public Server(){

    }
    public void startServer(){
        ListenerConfiguration listenerConfiguration = initListenerConfiguration();

        this.serverConnector = httpWsConnectorFactory
                .createServerConnector(new ServerBootstrapConfiguration(new HashMap<>()), listenerConfiguration);
        ServerConnectorFuture serverConnectorFuture = serverConnector.start();
        serverConnectorFuture.setHttpConnectorListener(new DefaultHttpConnectorListener());
        try {
            serverConnectorFuture.sync();
        } catch (InterruptedException e) {
        }
    }

    public void startSSLServer() throws URISyntaxException {
        ListenerConfiguration listenerConfiguration=initListenerConfiguration();
        listenerConfiguration.setScheme("https");
        listenerConfiguration.setKeyStoreFile(Paths.get(getClass().getResource("/").toURI()).getParent() + "/test" +
                "-classes/security/wso2carbon.jks");
        listenerConfiguration.setKeyStorePass("wso2carbon");
        listenerConfiguration.setValidateCertEnabled(false);
        this.serverConnector = httpWsConnectorFactory
                .createServerConnector(new ServerBootstrapConfiguration(new HashMap<>()), listenerConfiguration);
        ServerConnectorFuture serverConnectorFuture = serverConnector.start();
        serverConnectorFuture.setHttpConnectorListener(new DefaultHttpConnectorListener());
        try {
            serverConnectorFuture.sync();
        } catch (InterruptedException e) {
        }
    }

    public void stop()  {
        serverConnector.stop();
        try {
            httpWsConnectorFactory.shutdown();
        } catch (InterruptedException e) {
        }
    }
    private ListenerConfiguration initListenerConfiguration(){
        ListenerConfiguration listenerConfiguration=new ListenerConfiguration();
        listenerConfiguration.setPort(8080);
        listenerConfiguration.setHost("localhost");
        listenerConfiguration.setVersion("2.0");
        return listenerConfiguration;
    }

//    public static void main(String[] args) {
//        Server server=new Server();
//        try{
//            server.startSSLServer();
//        }catch (Exception e){
//            log.error("error",e);
//        }
//    }

    private class DefaultHttpConnectorListener implements HttpConnectorListener {


        public void onMessage(HttpCarbonMessage httpCarbonMessage) {
            System.out.println("Message");
            ByteBuf RESPONSE_BYTES = Unpooled.unreleasableBuffer(
                    Unpooled.copiedBuffer(httpCarbonMessage.getHttpVersion(), CharsetUtil.UTF_8));
            HttpCarbonMessage response=new HttpCarbonResponse(new DefaultHttpResponse(HttpVersion.HTTP_1_1,
                    HttpResponseStatus.OK));
            response.setHeaders(httpCarbonMessage.getHeaders());
            response.setHttpStatusCode(200);
            response.setKeepAlive(false);
            response.setHeader("content-length",httpCarbonMessage.getHttpVersion().length());
            response.addHttpContent(new DefaultHttpContent(RESPONSE_BYTES));
            response.addHttpContent(new DefaultLastHttpContent());
            System.out.println("response created");
            try {
                httpCarbonMessage.respond(response);
            } catch (ServerConnectorException e) {
                System.out.println("ServerConnectionException");
                throw new RuntimeException(e);
            }

        }

        public void onError(Throwable throwable) {

            System.out.println("Error");
        }

    }

}
