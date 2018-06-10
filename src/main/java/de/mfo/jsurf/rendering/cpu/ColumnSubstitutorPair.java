package de.mfo.jsurf.rendering.cpu;

import de.mfo.jsurf.algebra.ColumnSubstitutor;
import de.mfo.jsurf.algebra.ColumnSubstitutorForGradient;

class ColumnSubstitutorPair
{
    public final ColumnSubstitutor scs;
    public final ColumnSubstitutorForGradient gcs;
    
    ColumnSubstitutorPair( ColumnSubstitutor scs, ColumnSubstitutorForGradient gcs )
    {
        this.scs = scs;
        this.gcs = gcs;
    }
}