package jersey.client.bugreport.demo;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;

class HttpsServer implements Runnable
{
  private final SSLServerSocket sslServerSocket;

  public HttpsServer(SSLContext context) throws Exception
  {
    sslServerSocket = (SSLServerSocket) context.getServerSocketFactory().createServerSocket(0);
  }

  public int getPort()
  {
    return sslServerSocket.getLocalPort();
  }

  @Override
  public void run()
  {
    System.out.printf("Server started on port %d%n", getPort());
    while (true)
    {
      SSLSocket s;
      try
      {
        s = (SSLSocket) sslServerSocket.accept();
      }
      catch (IOException e2)
      {
        s = null;
      }
      final SSLSocket socket = s;
      new Thread(new Runnable()
      {
        public void run()
        {
          try
          {
            InputStream is = new BufferedInputStream(socket.getInputStream());
            byte[] data = new byte[2048];
            int len = is.read(data);
            if (len <= 0)
            {
              throw new IOException("no data received");
            }
            //System.out.printf("Server received: %s\n", new String(data, 0, len));
            PrintWriter writer = new PrintWriter(socket.getOutputStream());
            writer.println("HTTP/1.1 200 OK");
            writer.println("Content-Type: text/html");
            writer.println();
            writer.println("Hello from server!");
            writer.flush();
            writer.close();
            socket.close();
          }
          catch (Exception e1)
          {
            e1.printStackTrace();
          }
        }
      }).start();
    }
  }
}
