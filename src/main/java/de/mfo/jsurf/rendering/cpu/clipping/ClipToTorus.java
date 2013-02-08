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

package de.mfo.jsurf.rendering.cpu.clipping;

import java.util.List;
import java.util.LinkedList;
import javax.vecmath.*;

import de.mfo.jsurf.algebra.*;
import de.mfo.jsurf.rendering.cpu.*;

public class ClipToTorus extends Clipper
{
    double R;
    double r;
    double Rsqr;
    double rsqr;
    ClosedFormRootFinder cfrf;

    public ClipToTorus() { this( 1.0, 1.0 ); }
    public ClipToTorus( double R, double r )
    {
        super();
        this.R = R;
        this.r = r;
        this.Rsqr = R * R;
        this.rsqr = r * r;
        this.cfrf = new ClosedFormRootFinder();
    }

    public double get_R() { return R; }
    public double get_r() { return r; }

    @Override
    public List< Vector2d > clipRay( Ray r )
    { 
        UnivariatePolynomial x = new UnivariatePolynomial( r.o.x, r.d.x );
        UnivariatePolynomial y = new UnivariatePolynomial( r.o.y, r.d.y );
        UnivariatePolynomial z = new UnivariatePolynomial( r.o.z, r.d.z );
        UnivariatePolynomial xsqr = x.mult( x );
        UnivariatePolynomial ysqr = y.mult( y );
        UnivariatePolynomial zsqr = z.mult( z );

        UnivariatePolynomial torusPolynomial = xsqr.add( ysqr ).add( zsqr ).add( Rsqr-rsqr );
        torusPolynomial = torusPolynomial.mult( torusPolynomial ).sub( ysqr.add( zsqr ).mult( 4 * Rsqr ) );

        double[] roots = cfrf.findAllRoots( torusPolynomial ); // will return 0, 2 or 4 roots, even if there are multiple roots
        LinkedList< Vector2d > result = new LinkedList< Vector2d >();
        for( int i = 0; i < roots.length; i += 2 )
            result.add( new Vector2d( roots[ i ], roots[ i + 1 ] ) );

        return result;
    }

    @Override
    public boolean clipPoint( Point3d p )
    {
        double term1 = ( p.x*p.x+p.y*p.y+p.z*p.z+R*R-r*r );
        return term1 * term1 <= 4 * R * R *( p.y*p.y+p.z*p.z );
    }

    @Override
    public boolean pointClippingNecessary() { return false; }
}
