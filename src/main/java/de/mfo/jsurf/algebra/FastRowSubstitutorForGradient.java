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

package de.mfo.jsurf.algebra;

import de.mfo.jsurf.rendering.cpu.Ray;
import de.mfo.jsurf.rendering.cpu.RayCreator;

import javax.vecmath.*;

public class FastRowSubstitutorForGradient implements RowSubstitutorForGradient
{
    private XYZPolynomial gradientXPoly;
    private XYZPolynomial gradientYPoly;
    private XYZPolynomial gradientZPoly;
    private RayCreator rc;

    public FastRowSubstitutorForGradient( PolynomialOperation gradientXExpression, PolynomialOperation gradientYExpression, PolynomialOperation gradientZExpression, RayCreator rc )
    {
        this.rc = rc;
        Expand e = new Expand();

        try
        {
        this.gradientXPoly = gradientXExpression.accept( e, ( Void ) null );
        this.gradientYPoly = gradientYExpression.accept( e, ( Void ) null );
        this.gradientZPoly = gradientZExpression.accept( e, ( Void ) null );
        }
        catch( Throwable t )
        {
            t.printStackTrace();
        }
    }

    public ColumnSubstitutorForGradient setV( double v )
    {
        return new FastColumnSubstitutorForGradient( v );
    }

    class FastColumnSubstitutorForGradient implements ColumnSubstitutorForGradient
    {
        double v;

        public FastColumnSubstitutorForGradient( double v ) { this.v = v; }
        public UnivariatePolynomialVector3d setU( double u )
        {
            return new myUnivariatePolynomialVector3d( rc.createSurfaceSpaceRay( u, v ) );
        }
    }

    class myUnivariatePolynomialVector3d implements UnivariatePolynomialVector3d
    {
        Ray r;

        public myUnivariatePolynomialVector3d( Ray r )
        {
            this.r = r;
        }

        @Override
        public Vector3d setT( double t )
        {
            Point3d p = r.at( t );
            Vector3d result = new Vector3d();
            result.x = gradientXPoly.evaluateXYZ( p.x, p.y, p.z );
            result.y = gradientYPoly.evaluateXYZ( p.x, p.y, p.z );
            result.z = gradientZPoly.evaluateXYZ( p.x, p.y, p.z );
            return result;
        }
    }
}
