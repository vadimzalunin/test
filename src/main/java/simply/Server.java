package simply;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import htsjdk.samtools.SAMFileHeader;
import htsjdk.samtools.SAMTextHeaderCodec;
import htsjdk.samtools.cram.build.CramIO;
import htsjdk.samtools.cram.common.CramVersions;
import htsjdk.samtools.cram.io.ExposedByteArrayOutputStream;
import htsjdk.samtools.cram.structure.CramHeader;

import javax.ws.rs.core.HttpHeaders;
import java.io.*;
import java.net.InetSocketAddress;

/**
 * Created by vadim on 04/02/2016.
 */
public class Server {
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/test", new TestHandler());
        server.createContext("/header", new SAMHeaderHandler());
        server.setExecutor(null); // creates a default executor
        server.start();
    }

    static class TestHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {

            String response = "This is the response";
            t.getResponseHeaders().add(HttpHeaders.CONTENT_TYPE, "CRAM");
            t.sendResponseHeaders(200, 0);
            System.out.println(t.getRequestURI());

            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
         }
    }

    static class SAMHeaderHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {

            String fileId = t.getRequestURI().getPath().replaceFirst("/header/", "");
            System.out.println("fileId: "+fileId);
            InputStream is = new FileInputStream("C:\\Users\\vadim\\Downloads\\15496_1#45.cram");
            CramHeader cramHeader = CramIO.readCramHeader(is);

            t.getResponseHeaders().add(HttpHeaders.CONTENT_TYPE, "SAM_HEADER");

            t.getResponseHeaders().add("CRAM_VERSION", String.format("%d.%d", cramHeader.getMajorVersion(), cramHeader.getMinorVersion()));
            t.sendResponseHeaders(200, 0);

            ByteArrayOutputStream headerBodyOS = new ByteArrayOutputStream();
            OutputStreamWriter w = new OutputStreamWriter(headerBodyOS);
            new SAMTextHeaderCodec().encode(w, cramHeader.getSamFileHeader());
            try {
                w.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            OutputStream os = t.getResponseBody();
            os.write(headerBodyOS.toByteArray());
            os.close();
        }
    }
}
