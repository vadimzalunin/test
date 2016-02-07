package simply;

import htsjdk.samtools.CRAMFileReader;
import htsjdk.samtools.SAMRecordIterator;
import htsjdk.samtools.SAMSequenceRecord;
import htsjdk.samtools.cram.CRAIEntry;
import htsjdk.samtools.cram.CRAIIndex;
import htsjdk.samtools.cram.build.CramIO;
import htsjdk.samtools.cram.structure.CramHeader;
import htsjdk.samtools.seekablestream.SeekableFileStream;
import htsjdk.samtools.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import java.util.*;
import java.util.zip.GZIPInputStream;

/**
 * Created by vadim on 04/02/2016.
 */
public class CramFile {
    File file = new File("C:\\Users\\vadim\\Downloads\\15496_1#45.cram");
    CramHeader header;
    Map<Integer, List<IndexEntry>> index;
    long firstContainerOffset = Long.MAX_VALUE;

    public CramFile(File file) throws IOException {
        this.file = file;
        header = CramIO.readCramHeader(new FileInputStream(file));
        InputStream craiIS = new GZIPInputStream(new FileInputStream(file.getAbsolutePath() + ".crai"));
        List<IndexEntry> entries = new ArrayList<>();
        for (CRAIEntry entry : CRAIIndex.readIndex(craiIS)) {
            entries.add(new IndexEntry(entry));
        }

        Collections.sort(entries, byContainerOffsetComparator);
        firstContainerOffset = entries.get(0).containerStartOffset;

        for (int i = 0; i < entries.size() - 1; i++) {

        }

        long offsetOfFollowingContainer = file.length();
        for (int i = entries.size() - 1; i > 0; i--) {
            entries.get(i).containerByteSize = (int) (offsetOfFollowingContainer - entries.get(i).containerStartOffset);
            if (entries.get(i).containerStartOffset != entries.get(i - 1).containerStartOffset)
                offsetOfFollowingContainer = entries.get(i).containerStartOffset;
            System.out.println(entries.get(i));
        }
        entries.get(0).containerByteSize = (int) (offsetOfFollowingContainer - entries.get(0).containerStartOffset);
        System.out.println("first container offset: " + firstContainerOffset);

        index = new HashMap<>();
        for (IndexEntry entry : entries) {
            if (!index.containsKey(entry.sequenceId)) {
                index.put(entry.sequenceId, new ArrayList<>());
            }
            index.get(entry.sequenceId).add(entry);
        }
    }

    private static class IndexEntry extends CRAIEntry {
        public int containerByteSize;

        public IndexEntry() {
        }

        public IndexEntry(CRAIEntry delegate) {
            containerStartOffset = delegate.containerStartOffset;
            sequenceId = delegate.sequenceId;
            alignmentSpan = delegate.alignmentSpan;
            alignmentStart = delegate.alignmentStart;
            sliceIndex = delegate.sliceIndex;
            sliceOffset = delegate.sliceOffset;
            sliceSize = delegate.sliceSize;
        }

        @Override
        public String toString() {
            return super.toString() + ", " + containerByteSize;
        }
    }

    private static Comparator<CRAIEntry> byContainerOffsetComparator = new Comparator<CRAIEntry>() {

        @Override
        public int compare(CRAIEntry o1, CRAIEntry o2) {
            return (int) (o1.containerStartOffset - o2.containerStartOffset);
        }
    };

    public static void main(final String[] args) throws IOException {
        Log.setGlobalLogLevel(Log.LogLevel.INFO);
        CramFile file = new CramFile(new File("C:\\Users\\vadim\\Downloads\\15496_1#45.cram"));
        int seqSize = file.header.getSamFileHeader().getSequenceDictionary().getSequences().size();
        for (int i = 0; i < 10; i++) {
            SAMSequenceRecord sr = file.header.getSamFileHeader().getSequenceDictionary().getSequences().get(new Random().nextInt(seqSize));
            InputStream stream = file.getCramStreamForRegion(new Region(sr.getSequenceName(), 0, 100000));
            CRAMFileReader reader = new CRAMFileReader(null, stream);
            SAMRecordIterator iterator = reader.iterator();
            int counter = 0;
            while (iterator.hasNext()) {
                iterator.next();
                counter++;
            }
            System.out.println(counter);
        }
    }


    public InputStream getCramStreamForRegion(Region region) throws IOException {
        System.out.println("building stream for " + region.toString());
        int seqId = header.getSamFileHeader().getSequence(region.name).getSequenceIndex();
        List<IndexEntry> entries = index.get(seqId);
        BoundedInputStream.Builder builder = new BoundedInputStream.Builder();
        builder.add(0, firstContainerOffset);
        if (entries == null) return builder.build(new SeekableFileStream(file));

        Collections.sort(entries, byContainerOffsetComparator);
        Iterator<IndexEntry> iterator = entries.iterator();


        int start = region.start;
        int span = region.end - region.start;
        final boolean whole = start < 1 || span < 1;
        final IndexEntry query = new IndexEntry();
        query.sequenceId = seqId;
        query.alignmentStart = start < 1 ? 1 : start;
        query.alignmentSpan = span < 1 ? Integer.MAX_VALUE : span;
        query.containerStartOffset = Long.MAX_VALUE;
        query.sliceOffset = Integer.MAX_VALUE;
        query.sliceSize = Integer.MAX_VALUE;

        IndexEntry entry;
        long startOffset = 0, endOffset = 0;
        while (iterator.hasNext()) {
            entry = iterator.next();
            if (CRAIEntry.intersect(entry, query)) {
                startOffset = entry.containerStartOffset;
                endOffset = startOffset + entry.containerByteSize;
                System.out.println("endOffset1: " + endOffset);
                break;
            }
        }
        if (startOffset == 0) return builder.build(new SeekableFileStream(file));

        while (iterator.hasNext()) {
            entry = iterator.next();
            if (!CRAIEntry.intersect(entry, query)) {
                endOffset = entry.containerStartOffset;
                System.out.println("endOffset2: " + endOffset);
                break;
            }
        }
        System.out.println("endOffset3: " + endOffset);

        builder.add(startOffset, endOffset - startOffset);

        return builder.build(new SeekableFileStream(file));
    }

}
