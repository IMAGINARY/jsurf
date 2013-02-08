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

import javax.vecmath.Vector3d;

public class TransformedPolynomialRowSubstitutorForGradient implements RowSubstitutorForGradient
{
    TransformedPolynomialRowSubstitutor tprs_px;
    TransformedPolynomialRowSubstitutor tprs_py;
    TransformedPolynomialRowSubstitutor tprs_pz;

    public TransformedPolynomialRowSubstitutorForGradient( PolynomialOperation pox, PolynomialOperation poy, PolynomialOperation poz, PolynomialOperation rayXComponent, PolynomialOperation rayYComponent, PolynomialOperation rayZComponent )
    {
        Expand expand = new Expand();
        XYZPolynomial px = pox.accept( expand, ( Void ) null );
        XYZPolynomial py = poy.accept( expand, ( Void ) null );
        XYZPolynomial pz = poz.accept( expand, ( Void ) null );

        XYZPolynomial x = rayXComponent.accept( expand, ( Void ) null );
        XYZPolynomial y = rayYComponent.accept( expand, ( Void ) null );
        XYZPolynomial z = rayZComponent.accept( expand, ( Void ) null );

        tprs_px = new TransformedPolynomialRowSubstitutor( px, x, y, z );
        tprs_py = new TransformedPolynomialRowSubstitutor( py, x, y, z );
        tprs_pz = new TransformedPolynomialRowSubstitutor( pz, x, y, z );
    }

    class TransformedPolynomialColumnSubstitutorForGradient implements ColumnSubstitutorForGradient
    {
        double v;
        ColumnSubstitutor cs_px;
        ColumnSubstitutor cs_py;
        ColumnSubstitutor cs_pz;

        public TransformedPolynomialColumnSubstitutorForGradient( RowSubstitutor rs_x, RowSubstitutor rs_y, RowSubstitutor rs_z, double v )
        {
            this.v = v;
            this.cs_px = rs_x.setV( v );
            this.cs_py = rs_y.setV( v );
            this.cs_pz = rs_z.setV( v );
        }

        class myUnivariatePolynomialVector3d implements UnivariatePolynomialVector3d
        {
            private UnivariatePolynomial px;
            private UnivariatePolynomial py;
            private UnivariatePolynomial pz;

            public myUnivariatePolynomialVector3d( UnivariatePolynomial px, UnivariatePolynomial py, UnivariatePolynomial pz )
            {
                this.px = px;
                this.py = py;
                this.pz = pz;
            }

            public Vector3d setT( double t )
            {
                return new Vector3d( px.evaluateAt( t ), py.evaluateAt( t ), pz.evaluateAt( t ) );
            }
        }

        public UnivariatePolynomialVector3d setU( double u )
        {
            return new myUnivariatePolynomialVector3d( cs_px.setU( u ), cs_py.setU( u ), cs_pz.setU( u ) );
        }
    }



    public ColumnSubstitutorForGradient setV( double v )
    {
        return new TransformedPolynomialColumnSubstitutorForGradient( tprs_px, tprs_py, tprs_pz, v );
    }
}
