/*
 *    Copyright 2008 Christian Stussak
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.mfo.jsurf.rendering.cpu;

import de.mfo.jsurf.algebra.*;
import de.mfo.jsurf.rendering.*;

import javax.vecmath.*;
import java.util.concurrent.*;

public class RenderingTask implements Callable<Boolean>
{
    /**
     * Holds information needed for stepping through the pixels in the image
     */
	static class PixelStep {
		private final double u_start;
		private final double v_start;
		final double u_incr;
		final double v_incr;
		
		public final int width;
		public final int height;
		
		public double v;
		public double u;
		public double vOld;
		public double uOld;

		public int internalBufferIndex;
		public int colorBufferIndex;
		private final int colorBufferVStep;
		
		public PixelStep(DrawcallStaticData dcsd, int xStart, int yStart, int width, int height) {
			RayCreator rayCreator = dcsd.rayCreator;
			Vector2d uInterval = rayCreator.getUInterval();
			Vector2d vInterval = rayCreator.getVInterval();
			double displace = (dcsd.antiAliasingPattern == AntiAliasingPattern.OG_1x1) ? 0 : 0.5;
			this.u_start = rayCreator.transformU( ( xStart - displace ) / ( dcsd.width - 1.0 ) );
			this.v_start = rayCreator.transformV( ( yStart - displace ) / ( dcsd.height - 1.0 ) );
			this.u_incr = ( uInterval.y - uInterval.x ) / ( dcsd.width - 1.0 );
			this.v_incr = ( vInterval.y - vInterval.x ) / ( dcsd.height - 1.0 );
			
			this.width = width;
			this.height = height;
			this.colorBufferIndex = (yStart - 1) * dcsd.width + xStart - 1;
			this.colorBufferVStep = dcsd.width - width;
			reset();
		}

		private void reset() {
			vOld = 0;
			v = v_start;

			uOld = 0;
			u = u_start;
		}

		public void stepU() {
	        uOld = u;
	        u += u_incr;
	        colorBufferIndex++;
	        internalBufferIndex++;
		}
		
		public void stepV() {
		    vOld = v;
		    v += v_incr;

			uOld = 0;
			u = u_start;
			colorBufferIndex += colorBufferVStep;
		}
	}

    static ColorBufferPool bufferPool = new ColorBufferPool();
	
    // initialized by the constructor
    private final DrawcallStaticData dcsd;
    private final PixelStep step;
    private final ColumnSubstitutorPairProvider cspProvider;
    private final PolynomialTracer polyTracer;

    public RenderingTask( DrawcallStaticData dcsd, int xStart, int yStart, int xEnd, int yEnd )
    {
        this.dcsd = dcsd;
		this.step = new PixelStep(dcsd, xStart, yStart, xEnd - xStart + 2, yEnd - yStart + 2);
        this.cspProvider = new ColumnSubstitutorPairProvider(dcsd);
        this.polyTracer = new PolynomialTracer(dcsd);
    }

    public Boolean call() {
		Color3f[] colorBuffer = null;
        try {
        	PixelRenderStrategy pixelRenderer; 
        	if (useAntiAliasing()) {
        		colorBuffer = bufferPool.getBuffer(step.width * step.height);
        		pixelRenderer = new AntiAliasedPixelRenderer(dcsd, colorBuffer, polyTracer, cspProvider);
        	} else {
        		pixelRenderer = new BasicPixelRenderer(dcsd, polyTracer);
        	}
        	renderImage(pixelRenderer);
            return true;
        } catch( RenderingInterruptedException rie ) { // rendering interrupted .. that's ok
        } catch( Throwable t ) {
            t.printStackTrace();
        } finally {
        	if (colorBuffer != null)
        		bufferPool.releaseBuffer(colorBuffer);
        }
        return false;
    }

    private boolean useAntiAliasing() {
    	return dcsd.antiAliasingPattern != AntiAliasingPattern.OG_1x1;
    }

	private void renderImage(PixelRenderStrategy pixelRenderer) {
		for( int y = 0; y < step.height; ++y )
		{
			ColumnSubstitutorPair csp = cspProvider.get(step.v);

		    for( int x = 0; x < step.width; ++x )
		    {
		        if( Thread.currentThread().isInterrupted() )
		            throw new RenderingInterruptedException();
		        
		        pixelRenderer.renderPixel(x, y, step, csp);
		        step.stepU();
		    }
		    step.stepV();
		}
	}
    
//    private Color3f traceRay( double u, double v )
//    {
//        // create rays
//        Ray ray = dcsd.rayCreator.createCameraSpaceRay( u, v );
//        Ray clippingRay = dcsd.rayCreator.createClippingSpaceRay( u, v );
//        Ray surfaceRay = dcsd.rayCreator.createSurfaceSpaceRay( u, v );
//
//        Point3d eye = Helper.interpolate1D( ray.o, ray.d, dcsd.rayCreator.getEyeLocationOnRay() );
//        UnivariatePolynomialVector3d gradientPolys = null;
//
//        // optimize rays and root-finder parameters
//        //optimizeRays( ray, clippingRay, surfaceRay );
//
//        //System.out.println( u + "," + v + ":("+surfaceRay.o.x+","+surfaceRay.o.y+","+surfaceRay.o.z+")"+"("+surfaceRay.d.x+","+surfaceRay.d.y+","+surfaceRay.d.z+")t" );
//
//        // clip ray
//        List< Vector2d > intervals = dcsd.rayClipper.clipRay( clippingRay );
//        for( Vector2d interval : intervals )
//        {
//            // adjust interval, so that it does not start before the eye point
//            double eyeLocation = dcsd.rayCreator.getEyeLocationOnRay();
//
//            if( interval.x < eyeLocation && eyeLocation < interval.y )
//                interval.x = Math.max( interval.x, eyeLocation );
//
//            // intersect ray with surface and shade pixel
//            double[] hit = new double[ 1 ];
//            if( intersect( surfaceRay, interval.x, interval.y, hit ) )
//                if( dcsd.rayClipper.clipPoint( surfaceRay.at( hit[ 0 ] ), true ) )
//                {
//                        if( gradientPolys == null )
//                            gradientPolys = gcs.setU( u );
//                        Vector3d n_surfaceSpace = gradientPolys.setT( hit );
//                        Vector3d n_cameraSpace = dcsd.rayCreator.surfaceSpaceNormalToCameraSpaceNormal( n_surfaceSpace );
//
//                        return shade( ray.at( hit ), n_cameraSpace, eye );
//                }
//                    return shade( ray, surfaceRay, hit[ 0 ], eye );
//        }
//        return dcsd.backgroundColor;
//    }

    protected boolean intersectPolynomial( UnivariatePolynomial p, double rayStart, double rayEnd, double[] hit )
    {   
        //System.out.println( p );
        hit[ 0 ] = dcsd.realRootFinder.findFirstRootIn( p, rayStart, rayEnd );
        return !java.lang.Double.isNaN( hit[ 0 ] );
    }

    protected boolean intersect( Ray r, double rayStart, double rayEnd, double[] hit )
    {
        UnivariatePolynomial x = new UnivariatePolynomial( r.o.x, r.d.x );
        UnivariatePolynomial y = new UnivariatePolynomial( r.o.y, r.d.y );
        UnivariatePolynomial z = new UnivariatePolynomial( r.o.z, r.d.z );

        UnivariatePolynomial p = dcsd.coefficientCalculator.calculateCoefficients( x, y, z );
        p = p.shrink();

        hit[ 0 ] = ( float ) dcsd.realRootFinder.findFirstRootIn( p, rayStart, rayEnd );
        return !java.lang.Double.isNaN( hit[ 0 ] );
    }

    boolean blowUpChooseMaterial( Point3d p )
    {
        double R;
        if( dcsd.rayClipper instanceof de.mfo.jsurf.rendering.cpu.clipping.ClipBlowUpSurface )
            R = ( ( de.mfo.jsurf.rendering.cpu.clipping.ClipBlowUpSurface ) dcsd.rayClipper ).get_R();
        else
            R = 1.0;
        
	double u = p.x;
	double tmp = Math.sqrt( p.y*p.y + p.z*p.z );
	double v = R + tmp;
	double dist = u * u + v * v;
	if( dist > 1.0 )
		v = R - tmp; // choose the solution inside the disc
	return ( 3.0 * dist ) % 2.0 < 1.0;
    }

}
