package simply;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import htsjdk.samtools.CRAMIterator;
import htsjdk.samtools.cram.build.CramIO;

import javax.ws.rs.core.HttpHeaders;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

/**
 * Created by vadim on 04/02/2016.
 */
public class Server {
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/test", new MyHandler());
        server.setExecutor(null); // creates a default executor
        server.start();
    }

    static class MyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String response = "This is the response";
            StringBuffer sb = new StringBuffer();
            t.getResponseHeaders().add(HttpHeaders.CONTENT_TYPE, "CRAM");
            t.sendResponseHeaders(200, 0);
            System.out.println(t.getRequestURI());

            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
}
