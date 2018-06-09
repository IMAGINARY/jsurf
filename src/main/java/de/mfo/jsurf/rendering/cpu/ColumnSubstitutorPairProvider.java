package de.mfo.jsurf.rendering.cpu;

import java.util.HashMap;

import de.mfo.jsurf.algebra.RowSubstitutor;
import de.mfo.jsurf.algebra.RowSubstitutorForGradient;

class ColumnSubstitutorPairProvider {
	private final HashMap< java.lang.Double, ColumnSubstitutorPair > csp_hm;
    private final RowSubstitutor surfaceRowSubstitutor;
    private final RowSubstitutorForGradient gradientRowSubstitutor;

	public ColumnSubstitutorPairProvider(DrawcallStaticData dcsd) {
        this.csp_hm = new HashMap< java.lang.Double, ColumnSubstitutorPair >();
        this.surfaceRowSubstitutor = dcsd.surfaceRowSubstitutor;
        this.gradientRowSubstitutor = dcsd.gradientRowSubstitutor;
	}

	public ColumnSubstitutorPair get(double v) {
		ColumnSubstitutorPair csp = csp_hm.get(v);
		if (csp == null) {
			csp = new ColumnSubstitutorPair(surfaceRowSubstitutor.setV( v ), gradientRowSubstitutor.setV( v ));
			csp_hm.put(v, csp);
		}
		return csp;
	}
}