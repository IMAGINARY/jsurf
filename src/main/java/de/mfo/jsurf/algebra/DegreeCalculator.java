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

public class DegreeCalculator extends AbstractVisitor< Integer, Void >
{
    public Integer visit( PolynomialAddition pa, Void param )
    {
        return Math.max( pa.getFirstOperand().accept( this, ( Void ) null ), pa.getSecondOperand().accept( this, ( Void ) null ) );
    }

    public Integer visit( PolynomialSubtraction ps, Void param )
    {
        return Math.max( ps.getFirstOperand().accept( this, ( Void ) null ), ps.getSecondOperand().accept( this, ( Void ) null ) );
    }

    public Integer visit( PolynomialMultiplication pm, Void param )
    {
        return pm.getFirstOperand().accept( this, ( Void ) null ) + pm.getSecondOperand().accept( this, ( Void ) null );
    }

    public Integer visit( PolynomialPower pp, Void param )
    {
        return pp.getExponent() * pp.getBase().accept( this, ( Void ) null );
    }

    public Integer visit( PolynomialNegation pn, Void param )
    {
        return pn.getOperand().accept( this, ( Void ) null );
    }

    public Integer visit( PolynomialDoubleDivision pdd, Void param )
    {
        return pdd.getDividend().accept( this, ( Void ) null );
    }

    public Integer visit( PolynomialVariable pv, Void param )
    {
        return 1;
    }

    public Integer visit( DoubleBinaryOperation dbop, Void param )
    {
        return 0;
    }

    public Integer visit( DoubleUnaryOperation duop, Void param )
    {
        return 0;
    }

    public Integer visit( DoubleValue dv, Void param )
    {
        return 0;
    }

    public Integer visit( DoubleVariable dv, Void param )
    {
        return 0;
    }
}
