package id.skyop.common;

import java.util.Optional;

/** Pure local ballistic estimate used to explain and time Auto MLG attempts. */
public final class SkyOPLandingPrediction {
    private static final double GRAVITY = 0.08D;
    private static final double AIR_DRAG = 0.98D;
    private static final double HORIZONTAL_DRAG = 0.91D;

    private SkyOPLandingPrediction() { }

    @FunctionalInterface
    public interface SolidBlockQuery {
        boolean isSolid(int blockX, int blockY, int blockZ);
    }

    public record Result(int ticks, double x, double y, double z,
                         int blockX, int blockY, int blockZ) { }

    /**
     * Predicts a descending player's first solid support block. The caller must
     * make the query non-loading and restrict it to already-loaded chunks.
     */
    public static Optional<Result> predict(double x, double y, double z,
                                           double velocityX, double velocityY, double velocityZ,
                                           int maxTicks, SolidBlockQuery solidBlockQuery) {
        if (!finite(x, y, z, velocityX, velocityY, velocityZ)
                || maxTicks < 1 || maxTicks > 200 || solidBlockQuery == null
                || velocityY >= 0.0D) return Optional.empty();

        for (int tick = 1; tick <= maxTicks; tick++) {
            velocityY = (velocityY - GRAVITY) * AIR_DRAG;
            velocityX *= HORIZONTAL_DRAG;
            velocityZ *= HORIZONTAL_DRAG;
            int samples = Math.max(1, Math.min(64,
                    (int) Math.ceil(Math.max(Math.abs(velocityX),
                            Math.max(Math.abs(velocityY), Math.abs(velocityZ))) * 4.0D)));
            double startX = x, startY = y, startZ = z;
            for (int sample = 1; sample <= samples; sample++) {
                double fraction = sample / (double) samples;
                double sampleX = startX + velocityX * fraction;
                double sampleY = startY + velocityY * fraction;
                double sampleZ = startZ + velocityZ * fraction;
                int blockX = floor(sampleX);
                int blockY = floor(sampleY - 0.01D);
                int blockZ = floor(sampleZ);
                if (solidBlockQuery.isSolid(blockX, blockY, blockZ)) {
                    return Optional.of(new Result(tick, sampleX, sampleY, sampleZ,
                            blockX, blockY, blockZ));
                }
            }
            x += velocityX;
            y += velocityY;
            z += velocityZ;
            if (!finite(x, y, z, velocityX, velocityY, velocityZ)) return Optional.empty();
        }
        return Optional.empty();
    }

    private static int floor(double value) { return (int) Math.floor(value); }

    private static boolean finite(double... values) {
        for (double value : values) if (!Double.isFinite(value)) return false;
        return true;
    }
}
