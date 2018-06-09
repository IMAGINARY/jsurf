package de.mfo.jsurf.rendering.cpu;

import javax.vecmath.Color3f;

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
        boolean doSuperSampling = 
        	aap != AntiAliasingPattern.OG_2x2 &&
        	(areTooDifferent( ulColor, urColor ) ||
    		 areTooDifferent( ulColor, llColor ) ||
    		 areTooDifferent( ulColor, lrColor ) ||
    		 areTooDifferent( urColor, llColor ) ||
    		 areTooDifferent( urColor, lrColor ) ||
    		 areTooDifferent( llColor, lrColor ));

        // first average pixel-corner colors. Weight depends on whether more samples will be taken
        Color3f accumulator = new Color3f( ulColor );
        accumulator.add( urColor );
        accumulator.add( llColor );
        accumulator.add( lrColor );
        accumulator.scale( doSuperSampling ? aap.cornerWeight : 0.25f);

        if (doSuperSampling)
        {
            for( AntiAliasingPattern.SamplingPoint sp : aap )
            {
                if( Thread.currentThread().isInterrupted() )
                    throw new RenderingInterruptedException();

                // corners already accumulated above
                if (sp.isCorner)
                	continue;
                
                double v = step.vOld + sp.v * step.v_incr;
                double u = step.uOld + sp.u * step.u_incr;
                ColumnSubstitutorPair csp = cspProvider.get( v );
                Color3f sample = tracePolynomial( csp.scs, csp.gcs, u, v );
                accumulator.scaleAdd( sp.weight, sample, accumulator );
            }
        }

        // clamp color, because floating point operations may yield values outside [0,1]
        accumulator.clamp( 0f, 1f );
        return accumulator;
    }
    
    private boolean areTooDifferent(Color3f c1, Color3f c2) {
    	float x = c1.x - c2.x;
    	float y = c1.y - c2.y;
    	float z = c1.z - c2.z;
    	return (x * x) + (y * y) + (z * z) >= thresholdSqr;
    }
}