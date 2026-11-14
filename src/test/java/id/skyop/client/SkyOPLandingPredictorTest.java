package id.skyop.client;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SkyOPLandingPredictorTest {
    @Test void predictsFirstFlatSurfaceWithinThreeTicks() {
        var p = SkyOPLandingPredictor.predict(.5, 1, .5, 0, -.3, 0, 3, (x,y,z) -> 0);
        assertNotNull(p);
        assertTrue(p.ticks() <= 3);
        assertEquals(0, p.y(), .0001);
    }
    @Test void horizontalVelocityChangesPredictedLandingPosition() {
        var p = SkyOPLandingPredictor.predict(0, 1, 0, 1, -.3, 0, 3,
                (x,y,z) -> y <= 0 ? 0 : Double.NaN);
        assertNotNull(p);
        assertTrue(p.x() > 0 && p.x() < 3);
    }
    @Test void noSurfaceMeansNoPrediction() {
        assertNull(SkyOPLandingPredictor.predict(0, 10, 0, 0, -.1, 0, 3,
                (x,y,z) -> Double.NaN));
    }
    @Test void invalidInputIsRejected() {
        assertNull(SkyOPLandingPredictor.predict(0, 1, 0, 0, -1, 0, 4, (x,y,z) -> 0));
        assertNull(SkyOPLandingPredictor.predict(0, Double.NaN, 0, 0, -1, 0, 2, (x,y,z) -> 0));
    }
}
