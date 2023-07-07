package jersey.client.bugreport.demo;

import java.io.InputStream;
import java.net.URL;
import java.security.KeyStore;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManagerFactory;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;

/**
 * Jersey client seems to be not thread-safe:
 * When the first GET request is in progress, 
 * all parallel requests from other Jersey client instances fail 
 * with SSLHandshakeException: PKIX path building failed. 
 * 
 * Once the first GET request is completed, 
 * all subsequent requests work without error.
 *
 */
public class BugReport
{
  // private static int THREAD_NUMBER = 10; // set THREAD_NUMBER > 1 to reproduce an issue
  private static int THREAD_NUMBER = 1;

  private volatile static int responseCounter = 0;

  private static SSLContext createContext() throws Exception
  {
    URL url= BugReport.class.getResource("keystore.jks");
    KeyStore keyStore = KeyStore.getInstance("JKS");
    try (InputStream is = url.openStream())
    {
      keyStore.load(is, "password".toCharArray());
    }
    KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
    kmf.init(keyStore, "password".toCharArray());
    TrustManagerFactory tmf = TrustManagerFactory.getInstance("PKIX");
    tmf.init(keyStore);
    SSLContext context = SSLContext.getInstance("TLS");
    context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
    return context;
  }

  public static void main(String[] args) throws Exception
  {
     if (THREAD_NUMBER == 1)
    {
      System.out.println("\nThis is the working case (THREAD_NUMBER==1). Set THREAD_NUMBER > 1 to reproduce the error! \n");
    }

    final HttpsServer server = new HttpsServer(createContext());
    Executors.newFixedThreadPool(1).submit(server);

    // set THREAD_NUMBER > 1 to reproduce an issue
    ExecutorService executorService2clients = Executors.newFixedThreadPool(THREAD_NUMBER);
    
    final ClientBuilder builder = ClientBuilder.newBuilder().sslContext(createContext())
        .hostnameVerifier(new HostnameVerifier()
        {
          public boolean verify(String arg0, SSLSession arg1)
          {
            return true;
          }
        });

    for (int i = 0; i < 5; i++)
    {
      executorService2clients.submit(new Runnable()
      {
        @Override
        public void run()
        {
          try
          {
            Client client = builder.build();
            String ret = client.target("https://127.0.0.1:" + server.getPort()).request(MediaType.TEXT_HTML)
                .get(new GenericType<String>()
                {});
            System.out.print(++responseCounter + ". Server returned: " + ret);
          }
          catch (Exception e)
          {
            //get following exception here, if THREAD_NUMBER > 1
            //jakarta.ws.rs.ProcessingException: javax.net.ssl.SSLHandshakeException: PKIX path building failed:
            e.printStackTrace();
          }
        }
      });
    }
  }
}
