package de.mfo.jsurf.rendering.cpu;

import javax.vecmath.Color3f;
import javax.vecmath.Vector3f;

import de.mfo.jsurf.rendering.RenderingInterruptedException;

class AntiAliasedPixelRenderer extends PixelRenderStrategy {
	private final Color3f[] internalColorBuffer;
	private final float thresholdSqr;
	private final AntiAliasingPattern aap;
	private final ColumnSubstitutorPairProvider cspProvider;

	public AntiAliasedPixelRenderer(DrawcallStaticData dcsd, Color3f[] internalColorBuffer, PolynomialTracer polyTracer, ColumnSubstitutorPairProvider cspProvider) {
		super(dcsd, polyTracer);
		this.internalColorBuffer = internalColorBuffer;
		this.thresholdSqr = dcsd.antiAliasingThreshold * dcsd.antiAliasingThreshold;
		this.aap = dcsd.antiAliasingPattern;
		this.cspProvider = cspProvider;
	}
	
	@Override
	public void renderPixel(int x, int y, PixelStep step, ColumnSubstitutorPair csp) {
		internalColorBuffer[ step.internalBufferIndex ] = tracePolynomial( csp.scs, csp.gcs, step.u, step.v );
		if( x > 0 && y > 0 )
		{
		    Color3f urColor = internalColorBuffer[ step.internalBufferIndex ];
		    Color3f ulColor = internalColorBuffer[ step.internalBufferIndex - 1 ];
		    Color3f lrColor = internalColorBuffer[ step.internalBufferIndex - step.width ];
		    Color3f llColor = internalColorBuffer[ step.internalBufferIndex - step.width - 1 ];

		    colorBuffer[ step.colorBufferIndex ] = antiAliasPixel( step, ulColor, urColor, llColor, lrColor ).get().getRGB();
		}
	}

    private Color3f antiAliasPixel( PixelStep step, Color3f ulColor, Color3f urColor, Color3f llColor, Color3f lrColor )
    {
        // first average pixel-corner colors
        Color3f finalColor;

        // adaptive supersampling
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
                if( sp.u == 0.0 && sp.v == 0.0 )
                    ss_color = llColor;
                else if( sp.u == 0.0 && sp.v == 1.0 )
                    ss_color = ulColor;
                else if( sp.u == 1.0 && sp.v == 1.0 )
                    ss_color = urColor;
                else if( sp.u == 1.0 && sp.v == 0.0 )
                    ss_color = lrColor;
                else
                {
                    // color of this sample point is not known -> calculate
                    double v = step.vOld + sp.v * step.v_incr;
                    double u = step.uOld + sp.u * step.u_incr;
                    ColumnSubstitutorPair csp = cspProvider.get( v );
                    ss_color = tracePolynomial( csp.scs, csp.gcs, u, v );
                }
                finalColor.scaleAdd( sp.weight, ss_color, finalColor );
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

    private float colorDiffSqr( Color3f c1, Color3f c2 )
    {
        Vector3f diff = new Vector3f( c1 );
        diff.sub( c2 );
        return diff.dot( diff );
    }
}