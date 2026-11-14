package id.skyop.client;

import java.util.Objects;

public final class SkyOPLandingPredictor {
    @FunctionalInterface
    public interface SurfaceProbe {
        double surfaceTopY(double x, double feetY, double z);
    }

    public record Landing(int ticks, double x, double y, double z) { }

    private SkyOPLandingPredictor() { }

    public static Landing predict(double x, double y, double z,
                                  double vx, double vy, double vz,
                                  int maxTicks, SurfaceProbe probe) {
        Objects.requireNonNull(probe, "probe");
        if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z)
                || !Double.isFinite(vx) || !Double.isFinite(vy) || !Double.isFinite(vz)
                || maxTicks < 1 || maxTicks > 3) return null;

        for (int tick = 1; tick <= maxTicks; tick++) {
            double px = x, py = y, pz = z;
            vy = (vy - 0.08D) * 0.98D;
            vx *= 0.91D;
            vz *= 0.91D;
            double nx = x + vx, ny = y + vy, nz = z + vz;
            double dx = nx - px, dy = ny - py, dz = nz - pz;
            int samples = Math.max(1, (int) Math.ceil(Math.sqrt(dx*dx + dy*dy + dz*dz) * 4.0D));

            for (int i = 1; i <= samples; i++) {
                double t = i / (double) samples;
                double sx = px + dx*t, sy = py + dy*t, sz = pz + dz*t;
                double surface = probe.surfaceTopY(sx, sy, sz);
                if (Double.isFinite(surface) && sy <= surface + 1.0E-4D
                        && py >= surface - 0.05D) return new Landing(tick, sx, surface, sz);
            }
            x = nx; y = ny; z = nz;
        }
        return null;
    }
}
