package de.mfo.jsurf.rendering.cpu;

import de.mfo.jsurf.algebra.RowSubstitutor;
import de.mfo.jsurf.algebra.RowSubstitutorForGradient;

class ColumnSubstitutorPairProvider {
    private final RowSubstitutor surfaceRowSubstitutor;
    private final RowSubstitutorForGradient gradientRowSubstitutor;
    
    private final ColumnSubstitutorPair[] csps;
    private final double vMult;
    private final double vInc;

	public ColumnSubstitutorPairProvider(DrawcallStaticData dcsd, PixelStep step) {
        this.surfaceRowSubstitutor = dcsd.surfaceRowSubstitutor;
        this.gradientRowSubstitutor = dcsd.gradientRowSubstitutor;
        this.vInc = -step.v_start;
        this.vMult = (double)dcsd.antiAliasingPattern.vSteps / step.v_incr;
        this.csps = new ColumnSubstitutorPair[(dcsd.antiAliasingPattern.vSteps + 1) * (step.height + 1)];
	}

	public ColumnSubstitutorPair get(double v) {
		int index = (int)((v + vInc) * vMult);
	
		if (csps[index] == null)
			csps[index] = new ColumnSubstitutorPair(surfaceRowSubstitutor.setV( v ), gradientRowSubstitutor.setV( v ));
		
		return csps[index];
	}
}