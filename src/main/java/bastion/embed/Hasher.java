package bastion.embed;

/**
 * Deterministic 64-bit string hashing using the FNV-1a algorithm.
 *
 * <p>FNV-1a has good avalanche behaviour for short tokens and, crucially, is
 * stable across JVM runs, which makes every metric in Bastion reproducible.</p>
 */
public final class Hasher {

    private static final long FNV_OFFSET = 0xcbf29ce484222325L;
    private static final long FNV_PRIME = 0x100000001b3L;

    private Hasher() {}

    public static long fnv1a(String value) {
        long hash = FNV_OFFSET;
        for (int i = 0; i < value.length(); i++) {
            hash ^= (value.charAt(i) & 0xff);
            hash *= FNV_PRIME;
        }
        return hash;
    }

    /** Signed 32-bit projection, used to map a token into a fixed vector dimension. */
    public static int project(String token, int dimensions) {
        int h = (int) (fnv1a(token) & 0xffffffffL);
        return Math.floorMod(h, dimensions);
    }
}
