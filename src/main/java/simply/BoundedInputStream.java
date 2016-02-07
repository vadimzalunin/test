package simply;

import htsjdk.samtools.seekablestream.SeekableStream;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Created by vadim on 04/02/2016.
 */
public class BoundedInputStream extends InputStream {
    SeekableStream stream;
    ByteBuffer buf;
    Iterator<Zone> zones;
    Zone currentZone;

    public static class Builder {
        List<Zone> zones = new ArrayList<>();
        public Builder add(long offset, long size) {
            System.out.printf("adding: %d+%d\n", offset, size);
            zones.add(new Zone(offset, size));
            return this;
        }
        public InputStream build(SeekableStream delegate) throws IOException {
            return new BoundedInputStream(delegate, zones.toArray(new Zone[zones.size()]));
        }
    }

    public BoundedInputStream(SeekableStream stream, Zone[] zoneArray) throws IOException {
        this.stream = stream;
        zones = Arrays.asList(zoneArray).iterator();
        nextZone();
    }

    private boolean nextZone() throws IOException {
        if (!zones.hasNext()) return false;
        currentZone = zones.next();
        if (stream.position() != currentZone.offset) stream.seek(currentZone.offset);
        return true;
    }

    private int ava(long size) throws IOException {
        if (currentZone == null) return -1;
        long currentZoneEnd = currentZone.offset + currentZone.size;
        if (stream.position() + size < currentZoneEnd) {
            return (int) (size);
        }

        if (stream.position() < currentZoneEnd) {
            return (int) (currentZoneEnd - stream.position());
        }

        if (!nextZone()) return -1;
        currentZoneEnd = currentZone.offset + currentZone.size;
        return (int) (Math.min(currentZoneEnd - stream.position(), size));
    }

    @Override
    public int read() throws IOException {
        long ava = ava(1);
        if (ava < 0) return -1;
        return stream.read();
    }

    @Override
    public int read(byte[] b) throws IOException {
        int ava = ava(b.length);
        if (ava < 0) return -1;
        int offset = 0;
        int read = 0;
        while (read < ava) {
            int len = stream.read(b, offset, ava - read);
            offset += len;
            read += len;
        }
        return ava;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int ava = ava(len);
        if (ava < 0) return -1;
        int offset = off;
        while (offset < ava) {
            int readLen = stream.read(b, offset, ava - offset);
            offset += readLen;
        }
        return ava;
    }

    public static class Zone {
        long offset, size;

        public Zone(long offset, long size) {
            this.offset = offset;
            this.size = size;
        }
    }
}
