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

public class NewtonInterpolationCoefficientCalculator implements CoefficientCalculator
{
    private PolynomialOperation polynomialOperation;
    private int degree;
    private int size;
    
    public NewtonInterpolationCoefficientCalculator( PolynomialOperation polynomialOperation )
    {
        this.polynomialOperation = polynomialOperation;
        this.degree = polynomialOperation.accept( new DegreeCalculator(), ( Void ) null );
        this.size = this.degree + 1;
    }
       
    public UnivariatePolynomial calculateCoefficients( UnivariatePolynomial xPoly, UnivariatePolynomial yPoly, UnivariatePolynomial zPoly )
    {
        double[] x = new double[ size ];
        double[] y = new double[ size ];
        double[] newton_basis = new double[ size ];        
        ValueCalculator valueCalculator = new ValueCalculator();
        
        // DEGREE + 1 Stützpunkte auf Strahl eye + t * pos berechnen
        for( int i = 0; i <= degree; i++ )
        {
            x[ i ] = -1.0 + ( 2.0 * i ) / degree;
            valueCalculator.setX( xPoly.getCoeff( 0 ) + xPoly.getCoeff( 1 ) * x[ i ] );
            valueCalculator.setY( yPoly.getCoeff( 0 ) + yPoly.getCoeff( 1 ) * x[ i ] );
            valueCalculator.setZ( zPoly.getCoeff( 0 ) + zPoly.getCoeff( 1 ) * x[ i ] );
            y[ i ] = this.polynomialOperation.accept( valueCalculator, ( Void ) null );
        }

        // dividierte Differenzen berechen
        for( int i = 1; i <= degree; i++ )
            for( int j = degree; j >= i; j-- )
                y[ j ] = ( y[ j ] - y[ j - 1 ] ) / ( x[ j ] - x[ j - i ] );

        // schrittweise Koeffizienten mit Newton-Interpolationsformel berechnen
        double[] a = new double[ size ];

        newton_basis[ degree ] = 1.0;
        a[ 0 ] = y[ 0 ];

        for( int i = 1; i <= degree; i++ )
        {
            // ( ai*x^i + ... + a0 ) + ( ai*x^i + ... + a0 ) * ( x - x[ i ] ) * y[ i ] = ( ai*x^i + ... + a0 ) * x - x[ i ] * y[ i ] * ( ai*x^i + ... + a0 ) berechnen
            // 1. Koeffizienten der Newton-Basis um eine Potenz erhöhen (=shiften)
            newton_basis[ degree - i ] = 0.0;
            a[ i ] = 0.0;

            // 2. alte Koeffizienten der Newton-Basis multipliziert mit x[ i - 1 ] subtrahieren
            for( int j = degree - i; j < degree; j++ )
                newton_basis[ j ] = newton_basis[ j ] - newton_basis[ j + 1 ] * x[ i - 1 ];

            // 3. y[ i ] * ( neue Newton-Basis ) auf alte Koeffizienten addieren
            for( int j = 0; j <= i; j++ )
                a[ j ] += newton_basis[ degree - i + j ] * y[ i ];
        }
        
        return new UnivariatePolynomial( a );
    }
}
