package id.skyop.client;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SkyOPLandingPredictorTest {
    @Test void predictsFirstFlatSurfaceWithinThreeTicks() {
        var p=SkyOPLandingPredictor.predict(.5,1,.5,0,-.3,0,3,(x,y,z)->0);
        assertNotNull(p);
        assertTrue(p.ticks()<=3);
        assertEquals(0,p.y(),.0001);
    }

    @Test void horizontalVelocityChangesPredictedLandingPosition() {
        var p=SkyOPLandingPredictor.predict(0,1,0,1,-.3,0,3,(x,y,z)->y<=0?0:Double.NaN);
        assertNotNull(p);
        assertTrue(p.x()>0&&p.x()<3);
    }

    @Test void noSurfaceMeansNoPrediction() {
        assertNull(SkyOPLandingPredictor.predict(0,10,0,0,-.1,0,3,(x,y,z)->Double.NaN));
    }

    @Test void invalidPredictionInputIsRejected() {
        assertNull(SkyOPLandingPredictor.predict(0,1,0,0,-1,0,4,(x,y,z)->0));
        assertNull(SkyOPLandingPredictor.predict(0,Double.NaN,0,0,-1,0,2,(x,y,z)->0));
    }

    @Test void lookAnglesPointAlongPositiveZAtZeroYawAndPitch() {
        var a=SkyOPLandingPredictor.lookAngles(0,2,0,0,2,4);
        assertNotNull(a);
        assertEquals(0.0F,a.yaw(),.0001F);
        assertEquals(0.0F,a.pitch(),.0001F);
    }

    @Test void lookAnglesTurnTowardHorizontalDrift() {
        var a=SkyOPLandingPredictor.lookAngles(0,2,0,4,2,0);
        assertNotNull(a);
        assertEquals(-90.0F,a.yaw(),.0001F);
        assertEquals(0.0F,a.pitch(),.0001F);
    }

    @Test void lookAnglesPitchDownTowardLandingSurface() {
        var a=SkyOPLandingPredictor.lookAngles(0,3,0,0,0,4);
        assertNotNull(a);
        assertEquals(36.8699F,a.pitch(),.001F);
    }

    @Test void lookAnglesRejectInvalidOrZeroLengthRays() {
        assertNull(SkyOPLandingPredictor.lookAngles(0,0,0,0,0,0));
        assertNull(SkyOPLandingPredictor.lookAngles(0,Double.NaN,0,1,0,0));
    }
}
