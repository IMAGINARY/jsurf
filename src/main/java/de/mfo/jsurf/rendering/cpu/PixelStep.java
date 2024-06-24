package de.mfo.jsurf.rendering.cpu;

import javax.vecmath.Vector2d;

/**
 * Holds information needed for stepping through the pixels in the image
 */
class PixelStep {
	private final double u_start;
	final double v_start;
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
	
	public PixelStep(DrawcallStaticData dcsd, int xStart, int yStart, int xEnd, int yEnd) {
		RayCreator rayCreator = dcsd.rayCreator;
		Vector2d uInterval = rayCreator.getUInterval();
		Vector2d vInterval = rayCreator.getVInterval();
		double displace = (dcsd.antiAliasingPattern == AntiAliasingPattern.OG_1x1) ? 0 : 0.5;
		this.u_start = rayCreator.transformU( ( xStart - displace ) / ( dcsd.width - 1.0 ) );
		this.v_start = rayCreator.transformV( ( yStart - displace ) / ( dcsd.height - 1.0 ) );
		this.u_incr = ( uInterval.y - uInterval.x ) / ( dcsd.width - 1.0 );
		this.v_incr = ( vInterval.y - vInterval.x ) / ( dcsd.height - 1.0 );
		
		this.width = xEnd - xStart + 2;
		this.height = yEnd - yStart + 2;
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