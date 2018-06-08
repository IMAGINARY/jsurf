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
import java.util.*;

public class RenderingTask implements Callable<Boolean>
{
	static ColorBufferPool bufferPool = new ColorBufferPool();
	
    // initialized by the constructor
    private int xStart;
    private int yStart;
    private int xEnd;
    private int yEnd;
    private final DrawcallStaticData dcsd;
    private final Shader frontShader;
    private final Shader backShader;

    public RenderingTask( DrawcallStaticData dcsd, int xStart, int yStart, int xEnd, int yEnd )
    {
        this.dcsd = dcsd;
        this.xStart = xStart;
        this.yStart = yStart;
        this.xEnd = xEnd;
        this.yEnd = yEnd;
        this.frontShader = new Shader(dcsd.frontAmbientColor, dcsd.lightSources, dcsd.frontLightProducts);
        this.backShader = new Shader(dcsd.backAmbientColor, dcsd.lightSources, dcsd.backLightProducts);
    }

    public Boolean call() {
		int width = xEnd - xStart + 2;
		int height = yEnd - yStart + 2;
		Color3f[] colorBuffer = bufferPool.getBuffer(width * height);
        try {
    		PixelStep step = new PixelStep(dcsd, xStart, yStart, width, height);
            render(colorBuffer, step);
            return true;
        } catch( RenderingInterruptedException rie ) { // rendering interrupted .. that's ok
        } catch( Throwable t ) {
            t.printStackTrace();
        } finally {
        	bufferPool.releaseBuffer(colorBuffer);
        }
        return false;
    }

    private class ColumnSubstitutorPair
    {
        ColumnSubstitutorPair( ColumnSubstitutor scs, ColumnSubstitutorForGradient gcs )
        {
            this.scs = scs;
            this.gcs = gcs;
        }

        ColumnSubstitutor scs;
        ColumnSubstitutorForGradient gcs;
    }

    protected void render(Color3f[] colorBuffer, PixelStep step) throws RenderingInterruptedException {
        switch( dcsd.antiAliasingPattern ) {
            case OG_1x1: {
                renderWithoutAliasing(step);
                break;
            }
    		// all other antialiasing modes
            default:
                renderWithAliasing(colorBuffer, step);
        }
    }

    /**
     * Holds information needed for stepping through the pixels in the image
     *
     */
	private static class PixelStep {
		private final double u_start;
		private final double v_start;
		private final double u_incr;
		private final double v_incr;
		
		public final int width;
		public final int height;
		
		public double v;
		public double u;
		public double vOld;
		public double uOld;

		public int colorBufferIndex;
		private final int colorBufferVStep;
		
		public PixelStep(DrawcallStaticData dcsd, int xStart, int yStart, int width, int height) {
			RayCreator rayCreator = dcsd.rayCreator;
			Vector2d uInterval = rayCreator.getUInterval();
			Vector2d vInterval = rayCreator.getVInterval();
			this.u_start = rayCreator.transformU( ( xStart - 0.5 ) / ( dcsd.width - 1.0 ) );
			this.v_start = rayCreator.transformV( ( yStart - 0.5 ) / ( dcsd.height - 1.0 ) );
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
		}
		
		public void stepV() {
		    vOld = v;
		    v += v_incr;

			uOld = 0;
			u = u_start;
			colorBufferIndex += colorBufferVStep;
		}
	}

	private void renderWithAliasing(Color3f[] internalColorBuffer, PixelStep step) {
		// first sample canvas at pixel corners and cast primary rays
		ColumnSubstitutor scs = null;
		ColumnSubstitutorForGradient gcs = null;
		HashMap< java.lang.Double, ColumnSubstitutorPair > csp_hm = new HashMap< java.lang.Double, ColumnSubstitutorPair >();

	    int internalBufferIndex = 0;
		for( int y = 0; y < step.height; ++y )
		{
		    csp_hm.clear();
		    csp_hm.put( step.vOld, new ColumnSubstitutorPair( scs, gcs ) );

		    scs = dcsd.surfaceRowSubstitutor.setV( step.v );
		    gcs = dcsd.gradientRowSubstitutor.setV( step.v );
		    
		    csp_hm.put( step.v, new ColumnSubstitutorPair( scs, gcs ) );

		    for( int x = 0; x < step.width; ++x )
		    {
		        if( Thread.currentThread().isInterrupted() )
		            throw new RenderingInterruptedException();
		        
		        // trace rays corresponding to (u,v)-coordinates on viewing plane
		        internalColorBuffer[ internalBufferIndex ] = tracePolynomial( scs, gcs, step.u, step.v );
		        if( x > 0 && y > 0 )
		        {
		            Color3f urColor = internalColorBuffer[ internalBufferIndex ];
		            Color3f ulColor = internalColorBuffer[ internalBufferIndex - 1 ];
		            Color3f lrColor = internalColorBuffer[ internalBufferIndex - step.width ];
		            Color3f llColor = internalColorBuffer[ internalBufferIndex - step.width - 1 ];

		            dcsd.colorBuffer[ step.colorBufferIndex ] = antiAliasPixel( step.uOld, step.vOld, step.u_incr, step.v_incr, dcsd.antiAliasingPattern, ulColor, urColor, llColor, lrColor, csp_hm ).get().getRGB();
		        }
		        step.stepU();
		        internalBufferIndex++;
		    }
		    step.stepV();
		}
	}

	/** no antialising -> sample pixel center */
	private void renderWithoutAliasing(PixelStep step) {

		for( int y = 0; y < step.height; y++ )
		{
		    ColumnSubstitutor scs = dcsd.surfaceRowSubstitutor.setV( step.v );
		    ColumnSubstitutorForGradient gcs = dcsd.gradientRowSubstitutor.setV( step.v );
         
		    for( int x = 0; x < step.width; x++ )
		    {
		        if( Thread.currentThread().isInterrupted() )
		            throw new RenderingInterruptedException();

		        dcsd.colorBuffer[ step.colorBufferIndex ] = tracePolynomial( scs, gcs, step.u, step.v ).get().getRGB();
			    step.stepU();
		    }
			step.stepV();
		}
	}
	
    private Color3f antiAliasPixel( double ll_u, double ll_v, double u_incr, double v_incr, AntiAliasingPattern aap, Color3f ulColor, Color3f urColor, Color3f llColor, Color3f lrColor, HashMap< java.lang.Double, ColumnSubstitutorPair > csp_hm )
    {
        // first average pixel-corner colors
        Color3f finalColor;

        // adaptive supersampling
        float thresholdSqr = dcsd.antiAliasingThreshold * dcsd.antiAliasingThreshold;
        if( aap != AntiAliasingPattern.OG_2x2 && ( colorDiffSqr( ulColor, urColor ) >= thresholdSqr ||
            colorDiffSqr( ulColor, llColor ) >= thresholdSqr ||
            colorDiffSqr( ulColor, lrColor ) >= thresholdSqr ||
            colorDiffSqr( urColor, llColor ) >= thresholdSqr ||
            colorDiffSqr( urColor, lrColor ) >= thresholdSqr ||
            colorDiffSqr( llColor, lrColor ) >= thresholdSqr ) )
        {
            // anti-alias pixel with advanced sampling pattern
            finalColor = new Color3f();
            for( AntiAliasingPattern.SamplingPoint sp : aap )
            {
                if( Thread.currentThread().isInterrupted() )
                    throw new RenderingInterruptedException();

                Color3f ss_color;
                if( sp.getU() == 0.0 && sp.getV() == 0.0 )
                    ss_color = llColor;
                else if( sp.getU() == 0.0 && sp.getV() == 1.0 )
                    ss_color = ulColor;
                else if( sp.getU() == 1.0 && sp.getV() == 1.0 )
                    ss_color = urColor;
                else if( sp.getU() == 1.0 && sp.getV() == 0.0 )
                    ss_color = lrColor;
                else
                {
                    // color of this sample point is not known -> calculate
                    double v = ll_v + sp.getV() * v_incr;
                    double u = ll_u + sp.getU() * u_incr;
                    ColumnSubstitutorPair csp = csp_hm.get( v );
                    if( csp == null )
                    {
                        csp = new ColumnSubstitutorPair( dcsd.surfaceRowSubstitutor.setV( v ), dcsd.gradientRowSubstitutor.setV( v ) );
                        csp_hm.put( v, csp );
                    }
                    ss_color = tracePolynomial( csp.scs, csp.gcs, u, v );
                }
                finalColor.scaleAdd( sp.getWeight(), ss_color, finalColor );
                
                if( false )
                    return new Color3f( 0, 0, 0 ); // paint pixels, that are supposed to be anti-aliased in black
            }
        }
        else
        {
            finalColor = new Color3f( ulColor );
            finalColor.add( urColor );
            finalColor.add( llColor );
            finalColor.add( lrColor );
            finalColor.scale( 0.25f );
        }

        // clamp color, because floating point operations may yield values outside [0,1]
        finalColor.clamp( 0f, 1f );
        return finalColor;
    }

    private Color3f tracePolynomial( ColumnSubstitutor scs, ColumnSubstitutorForGradient gcs, double u, double v )
    {
        // create rays
        Ray ray = dcsd.rayCreator.createCameraSpaceRay( u, v );
        Ray clippingRay = dcsd.rayCreator.createClippingSpaceRay( u, v );
        Ray surfaceRay = dcsd.rayCreator.createSurfaceSpaceRay( u, v );

        UnivariatePolynomialVector3d gradientPolys = null;

        // optimize rays and root-finder parameters
        //optimizeRays( ray, clippingRay, surfaceRay );

        // clip ray
        List< Vector2d > intervals = dcsd.rayClipper.clipRay( clippingRay );
        if( !intervals.isEmpty() )
        {
            UnivariatePolynomial surfacePoly = scs.setU( u );
            for( Vector2d interval : intervals )
            {
                // adjust interval, so that it does not start before the eye point
                double eyeLocation = dcsd.rayCreator.getEyeLocationOnRay();
                if( interval.x < eyeLocation && eyeLocation < interval.y )
                    interval.x = Math.max( interval.x, eyeLocation );

                // intersect ray with surface and shade pixel
                double hit = dcsd.realRootFinder.findFirstRootIn( surfacePoly, interval.x, interval.y );
                if( !java.lang.Double.isNaN( hit ) && dcsd.rayClipper.clipPoint( surfaceRay.at( hit ), true ) )
                {
                    if( gradientPolys == null )
                        gradientPolys = gcs.setU( u );

                    Vector3d n_surfaceSpace = gradientPolys.setT( hit );
                    Vector3d n_cameraSpace = dcsd.rayCreator.surfaceSpaceNormalToCameraSpaceNormal( n_surfaceSpace );

                    return shade( ray, hit, n_cameraSpace );
                }
            }
        }
        return dcsd.backgroundColor;
    }

    /**
     * Calculates the shading in camera space
     */
    protected Color3f shade( Ray ray, double hit, Vector3d cameraSpaceNormal )
    {
        // normalize only if point is not singular
        float nLength = (float) cameraSpaceNormal.length();
        if( nLength != 0.0f )
            cameraSpaceNormal.scale( 1.0f / nLength );

        Vector3d view = new Vector3d(-ray.d.x, -ray.d.y, -ray.d.z);
        // TODO: not normalizing the view does not seem to affect the rendered result, maybe it can be avoided
        view.normalize();

        Shader shader = frontShader;
        if( cameraSpaceNormal.dot( view ) <= 0.0f ) {
        	shader = backShader;
            cameraSpaceNormal.negate();
        }

        return shader.shade(ray.at(hit), view, cameraSpaceNormal);
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

    private float colorDiffSqr( Color3f c1, Color3f c2 )
    {
        Vector3f diff = new Vector3f( c1 );
        diff.sub( c2 );
        return diff.dot( diff );
    }

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
    

