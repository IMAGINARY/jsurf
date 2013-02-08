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

public class PolynomialExpansionRowSubstitutor implements RowSubstitutor {
    private PolynomialOperation po;
    private XYZPolynomial xyzp;
    private XYZPolynomial x3, y3, z3;

    public PolynomialExpansionRowSubstitutor( PolynomialOperation po, PolynomialOperation rayXComponent, PolynomialOperation rayYComponent, PolynomialOperation rayZComponent )
    {
        Expand expand = new Expand();
        this.po = po;
        this.xyzp = po.accept( expand, ( Void ) null );
        x3 = rayXComponent.accept( expand, ( Void ) null );
        y3 = rayYComponent.accept( expand, ( Void ) null );
        z3 = rayZComponent.accept( expand, ( Void ) null );
    }

    private static class myColumnSubstitutor implements ColumnSubstitutor
    {
        private PolynomialOperation po;
        private XYZPolynomial xyzp;
        private XYPolynomial x2, y2, z2;

        private double v;

        public myColumnSubstitutor( PolynomialOperation po, XYZPolynomial xyzp, XYZPolynomial x3, XYZPolynomial y3, XYZPolynomial z3, double v )
        {
            this.v = v;
            this.po = po;
            this.xyzp = xyzp;

            this.x2 = x3.evaluateZ( v );
            this.y2 = y3.evaluateZ( v );
            this.z2 = z3.evaluateZ( v );
        }

        @Override
        public UnivariatePolynomial setU( double u )
        {
            UnivariatePolynomial x = this.x2.evaluateY( u );
            UnivariatePolynomial y = this.y2.evaluateY( u );
            UnivariatePolynomial z = this.z2.evaluateY( u );
            //return this.po.accept( new UnivariatePolynomialExpansion( x, y, z ), ( Void ) null );
            return xyzp.substitute( x, y, z );
        }
    }

    public ColumnSubstitutor setV( double v )
    {
        return new myColumnSubstitutor( po, xyzp, x3, y3, z3, v );
    }
}
