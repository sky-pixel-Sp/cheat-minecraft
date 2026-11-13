package id.skyop.client;

import id.skyop.common.SkyOPLandingPrediction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SkyOPLandingPredictionTest {
    @Test
    void predictsFirstSolidBlockAlongDescendingTrajectory() {
        var result = SkyOPLandingPrediction.predict(
                0.5D, 10.0D, 0.5D, 0.0D, -0.5D, 0.0D, 20,
                (x, y, z) -> x == 0 && y == 0 && z == 0);
        assertTrue(result.isPresent());
        assertEquals(0, result.get().blockX());
        assertEquals(0, result.get().blockY());
        assertEquals(0, result.get().blockZ());
        assertTrue(result.get().ticks() > 1);
    }

    @Test
    void returnsEmptyWhenNoSolidBlockIsFoundWithinPredictionWindow() {
        assertTrue(SkyOPLandingPrediction.predict(
                0.5D, 30.0D, 0.5D, 0.0D, -0.2D, 0.0D, 4,
                (x, y, z) -> false).isEmpty());
    }

    @Test
    void rejectsInvalidOrUpwardTrajectoryInputs() {
        assertTrue(SkyOPLandingPrediction.predict(
                0.0D, 5.0D, 0.0D, 0.0D, 0.1D, 0.0D, 10,
                (x, y, z) -> true).isEmpty());
        assertTrue(SkyOPLandingPrediction.predict(
                Double.NaN, 5.0D, 0.0D, 0.0D, -0.1D, 0.0D, 10,
                (x, y, z) -> true).isEmpty());
    }
}
