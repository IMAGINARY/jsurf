package de.mfo.jsurf.rendering.cpu;

import de.mfo.jsurf.rendering.cpu.RenderingTask.PixelStep;

class BasicPixelRenderer extends PixelRenderStrategy {
	public BasicPixelRenderer(DrawcallStaticData dcsd, PolynomialTracer polyTracer) {
		super(dcsd, polyTracer);
	}

	@Override public void renderPixel(int x, int y, PixelStep step, ColumnSubstitutorPair csp) {
		colorBuffer[ step.colorBufferIndex ] = tracePolynomial(csp.scs, csp.gcs, step.u, step.v ).get().getRGB();
	}
}