package simply;

import htsjdk.samtools.cram.CRAIEntry;
import htsjdk.samtools.cram.CRAIIndex;
import htsjdk.samtools.cram.build.CramIO;
import htsjdk.samtools.cram.structure.CramHeader;
import htsjdk.samtools.seekablestream.SeekableFileStream;
import htsjdk.samtools.seekablestream.SeekableStream;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.zip.GZIPInputStream;

/**
 * Created by vadim on 04/02/2016.
 */
public class CramFile {
    File file = new File ("C:\\Users\\vadim\\Downloads\\15496_1#45.cram") ;
    CramHeader header;
    Map<Integer, List<CRAIEntry>> index;
    long firstContainerOffset;

    public CramFile(File file) throws IOException {
        this.file = file;
        header = CramIO.readCramHeader(new FileInputStream(file));
        InputStream craiIS = new GZIPInputStream(new FileInputStream(file.getAbsolutePath()+".crai"));
        List<CRAIEntry> craiEntries = CRAIIndex.readIndex(craiIS);
        firstContainerOffset = craiEntries.get(0).containerStartOffset;
        index = new HashMap<>();
        for (CRAIEntry entry : craiEntries) {
            if (!index.containsKey(entry.sequenceId)) {
                index.put(entry.sequenceId, new ArrayList<>());
            }
            index.get(entry.sequenceId).add(entry);
        }
    }

    public InputStream getCramStreamForRegion(Region region) throws IOException {
        int seqId=header.getSamFileHeader().getSequence(region.name).getSequenceIndex();
        List<CRAIEntry> entries = index.get(seqId);
        Iterator<CRAIEntry> iterator = entries.iterator();
        BoundedInputStream.Builder builder = new BoundedInputStream.Builder();
        builder.add(0, firstContainerOffset);


        int start = region.start;
        int span = region.end-region.start;
        final boolean whole = start < 1 || span < 1;
        final CRAIEntry query = new CRAIEntry();
        query.sequenceId = seqId;
        query.alignmentStart = start < 1 ? 1 : start;
        query.alignmentSpan = span < 1 ? Integer.MAX_VALUE : span;
        query.containerStartOffset = Long.MAX_VALUE;
        query.sliceOffset = Integer.MAX_VALUE;
        query.sliceSize = Integer.MAX_VALUE;

        CRAIEntry entry;
        long startOffset=0, endOffset=0;
        while (iterator.hasNext()) {
            entry = iterator.next();
            if (CRAIEntry.intersect(entry, query)) {
                startOffset = entry.containerStartOffset;
                break;
            }
        }

        while (iterator.hasNext()) {
            entry = iterator.next();
            if (!CRAIEntry.intersect(entry, query)) {
                endOffset = entry.containerStartOffset;
                break;
            }
        }

        builder.add(startOffset, endOffset-startOffset);

        return builder.build(new SeekableFileStream(file));
    }

}
