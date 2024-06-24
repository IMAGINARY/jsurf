package de.mfo.jsurf.rendering.cpu;

import java.util.List;

import javax.vecmath.Vector2d;

import de.mfo.jsurf.algebra.ColumnSubstitutor;
import de.mfo.jsurf.algebra.ColumnSubstitutorForGradient;
import de.mfo.jsurf.algebra.RealRootFinder;
import de.mfo.jsurf.algebra.UnivariatePolynomial;
import de.mfo.jsurf.rendering.cpu.clipping.Clipper;

public class PolynomialTracer {
    private final RealRootFinder realRootFinder;
    private final RayCreator rayCreator;
    private final Clipper rayClipper;

	public PolynomialTracer(DrawcallStaticData dcds) {
		this.rayClipper = dcds.rayClipper;
		this.rayCreator = dcds.rayCreator;
		this.realRootFinder = dcds.realRootFinder;
	}
	
	/**
	 * Returns the closest valid distance at which there was a hit. Double.NaN if no hit.
	 */
    public double findClosestHit( ColumnSubstitutor scs, ColumnSubstitutorForGradient gcs, double u, double v )
    {
        Ray clippingRay = rayCreator.createClippingSpaceRay( u, v );

        List< Vector2d > intervals = rayClipper.clipRay( clippingRay );
        if( !intervals.isEmpty() )
        {
            UnivariatePolynomial surfacePoly = scs.setU( u );
            for( Vector2d interval : intervals )
            {
                // adjust interval, so that it does not start before the eye point
                double eyeLocation = rayCreator.getEyeLocationOnRay();
                if( interval.x < eyeLocation && eyeLocation < interval.y )
                    interval.x = Math.max( interval.x, eyeLocation );

                // intersect ray with surface and shade pixel
                double hit = realRootFinder.findFirstRootIn( surfacePoly, interval.x, interval.y );
                Ray surfaceRay = rayCreator.createSurfaceSpaceRay( u, v );
                if( !java.lang.Double.isNaN( hit ) && rayClipper.clipPoint( surfaceRay.at( hit ), true ) )
                {
                	return hit;
                }
            }
        }
        return Double.NaN;
    }

}
