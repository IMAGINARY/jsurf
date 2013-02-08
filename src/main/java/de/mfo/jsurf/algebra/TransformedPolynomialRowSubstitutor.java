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

import javax.vecmath.*;

public class TransformedPolynomialRowSubstitutor implements RowSubstitutor {
    private XYZPolynomial tuvPolynomial;
    
    public TransformedPolynomialRowSubstitutor( PolynomialOperation po, PolynomialOperation rayXComponent, PolynomialOperation rayYComponent, PolynomialOperation rayZComponent )
    {
        ToStringVisitor tsv = new ToStringVisitor();
        //System.out.println();
        
        //System.out.println( "x=" + rayXComponent.accept( tsv, null ) );
        //System.out.println( "y=" + rayYComponent.accept( tsv, null ) );
        //System.out.println( "z=" + rayZComponent.accept( tsv, null ) );
        

        Expand expand = new Expand();
        XYZPolynomial p = po.accept( expand, ( Void ) null );
        //System.out.println( "p=" + p );

        XYZPolynomial x = rayXComponent.accept( expand, ( Void ) null );
        XYZPolynomial y = rayYComponent.accept( expand, ( Void ) null );
        XYZPolynomial z = rayZComponent.accept( expand, ( Void ) null );
        tuvPolynomial = p.substitute( x, y, z );
        //System.out.println( tuvPolynomial );
    }

    public TransformedPolynomialRowSubstitutor( XYZPolynomial p, XYZPolynomial rayXComponent, XYZPolynomial rayYComponent, XYZPolynomial rayZComponent )
    {
        tuvPolynomial = p.substitute( rayXComponent, rayYComponent, rayZComponent );
    }
    
    private static class myColumnSubstitutor implements ColumnSubstitutor
    {
        private XYPolynomial tuPolynomial;
        private double v;
        
        public myColumnSubstitutor( XYZPolynomial tvuPolynomial, double v )
        {
            this.v = v;
            this.tuPolynomial = tvuPolynomial.evaluateZ( v );
            //System.out.println( "coeffs at v=" + v + ":" + this.tuPolynomial );
        }
        
        public UnivariatePolynomial setU( double u )
        {
            //System.out.println( "coeffs at v=" +v+",u=" + u+  ":" + this.tuPolynomial.evaluateY( u ) );
            return this.tuPolynomial.evaluateY( u );
        }
    }
    
    public ColumnSubstitutor setV( double v )
    {
        return new myColumnSubstitutor( tuvPolynomial, v );
    }
}
