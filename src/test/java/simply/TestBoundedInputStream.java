package simply;

import htsjdk.samtools.seekablestream.SeekableMemoryStream;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by vadim on 04/02/2016.
 */
public class TestBoundedInputStream {

    @Test
    public void testEmptyZones() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (int i=0; i<10; i++) baos.write(i);
        BoundedInputStream.Zone[] zones = new BoundedInputStream.Zone[]{} ;
        BoundedInputStream bis = new BoundedInputStream(new SeekableMemoryStream(baos.toByteArray(), null), zones);
        Assert.assertEquals(-1, bis.read());
    }


    @Test
    public void testReadByte() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (int i=0; i<10; i++) baos.write(i);
        BoundedInputStream.Zone[] zones = new BoundedInputStream.Zone[]{new BoundedInputStream.Zone(0, 1)} ;
        BoundedInputStream bis = new BoundedInputStream(new SeekableMemoryStream(baos.toByteArray(), null), zones);
        Assert.assertEquals(0, bis.read());
        Assert.assertEquals(-1, bis.read());
        Assert.assertEquals(-1, bis.read());

        zones = new BoundedInputStream.Zone[]{new BoundedInputStream.Zone(0, 2), new BoundedInputStream.Zone(3, 2)} ;
        bis = new BoundedInputStream(new SeekableMemoryStream(baos.toByteArray(), null), zones);
        Assert.assertEquals(0, bis.read());
        Assert.assertEquals(1, bis.read());
        Assert.assertEquals(3, bis.read());
        Assert.assertEquals(4, bis.read());
        Assert.assertEquals(-1, bis.read());
        Assert.assertEquals(-1, bis.read());

        zones = new BoundedInputStream.Zone[]{new BoundedInputStream.Zone(1, 1), new BoundedInputStream.Zone(9, 1)} ;
        bis = new BoundedInputStream(new SeekableMemoryStream(baos.toByteArray(), null), zones);
        Assert.assertEquals(1, bis.read());
        Assert.assertEquals(9, bis.read());
        Assert.assertEquals(-1, bis.read());
        Assert.assertEquals(-1, bis.read());
    }

    @Test
    public void testReadBytes() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (int i=0; i<10; i++) baos.write(i);
        byte[] bytes = new byte[1] ;
        BoundedInputStream.Zone[] zones = new BoundedInputStream.Zone[]{new BoundedInputStream.Zone(0, 1)} ;
        BoundedInputStream bis = new BoundedInputStream(new SeekableMemoryStream(baos.toByteArray(), null), zones);
        bis.read(bytes);
        Assert.assertArrayEquals(new byte[]{0}, bytes);
        Assert.assertEquals(-1, bis.read());
        Assert.assertEquals(-1, bis.read());

        bytes = new byte[4] ;
        zones = new BoundedInputStream.Zone[]{new BoundedInputStream.Zone(0, 2), new BoundedInputStream.Zone(3, 2)} ;
        bis = new BoundedInputStream(new SeekableMemoryStream(baos.toByteArray(), null), zones);
        int read = bis.read(bytes);
        Assert.assertEquals(2, read);
        Assert.assertArrayEquals(new byte[]{0, 1, 0, 0}, bytes);

        read = bis.read(bytes);
        Assert.assertEquals(2, read);
        Assert.assertArrayEquals(new byte[]{3, 4, 0, 0}, bytes);
        Assert.assertEquals(-1, bis.read());

        bytes = new byte[2] ;
        zones = new BoundedInputStream.Zone[]{new BoundedInputStream.Zone(1, 1), new BoundedInputStream.Zone(9, 1)} ;
        bis = new BoundedInputStream(new SeekableMemoryStream(baos.toByteArray(), null), zones);
        read=bis.read(bytes);
        Assert.assertEquals(1, read);
        Assert.assertArrayEquals(new byte[]{1, 0}, bytes);

        read = bis.read(bytes);
        Assert.assertEquals(1, read);
        Assert.assertArrayEquals(new byte[]{9, 0}, bytes);
        Assert.assertEquals(-1, bis.read());
    }
}
