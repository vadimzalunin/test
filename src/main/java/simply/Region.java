package simply;

/**
 * Created by vadim on 04/02/2016.
 */
public class Region {
    public String name;
    public int start = -1, end = -1;

    public Region(String name) {
        this.name = name;
    }

    public Region(String name, int start, int end) {
        this.name = name;
        this.start = start;
        this.end = end;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer(name);
        if (start > -1) sb.append(":").append(start);
        if (end > -1) sb.append("-").append(end);
        return sb.toString();
    }

    public static Region fromString(final String string) {
        String semiWords[] = string.split(":");
        Region region = new Region(semiWords[0]);
        if (semiWords.length == 1) return region;

        String[] posWords = semiWords[1].split("-");
        region.start = Integer.parseInt(posWords[0]);
        if (posWords.length > 1)
            region.end = Integer.parseInt(posWords[1]);

        return region;
    }
}
