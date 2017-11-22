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

public class CloneVisitor extends AbstractVisitor< PolynomialOperation, Void > {

    public PolynomialOperation visit( PolynomialAddition pa, Void param )
    {
        return new PolynomialAddition( pa.getFirstOperand().accept( this, ( Void ) null ), pa.getSecondOperand().accept( this, ( Void ) null ) );
    }

    public PolynomialOperation visit( PolynomialSubtraction ps, Void param )
    {
        return new PolynomialSubtraction( ps.getFirstOperand().accept( this, ( Void ) null ), ps.getSecondOperand().accept( this, ( Void ) null ) );
    }

    public PolynomialOperation visit( PolynomialMultiplication pm, Void param )
    {
    	return new PolynomialMultiplication( pm.getFirstOperand().accept( this, ( Void ) null ), pm.getSecondOperand().accept( this, ( Void ) null ) );
    }

    public PolynomialOperation visit( PolynomialPower pp, Void param )
    {
        return new PolynomialPower( pp.getBase().accept( this, ( Void ) null ), pp.getExponent() );
    }

    public PolynomialOperation visit( PolynomialNegation pn, Void param )
    {
        return new PolynomialNegation( pn.getOperand().accept( this, ( Void ) null ) );
    }

    public PolynomialOperation visit( PolynomialDoubleDivision pdd, Void param )
    {
        return new PolynomialDoubleDivision( pdd.getDividend().accept( this, ( Void ) null ), ( DoubleOperation ) pdd.getDivisor().accept( this, ( Void ) null ) );
    }

    public PolynomialOperation visit( PolynomialVariable pv, Void param )
    {
        return pv;
    }

    public DoubleOperation visit( DoubleBinaryOperation dbop, Void param )
    {
        DoubleOperation firstOperand = ( DoubleOperation ) dbop.getFirstOperand().accept( this, ( Void ) null );
        DoubleOperation secondOperand = ( DoubleOperation ) dbop.getSecondOperand().accept( this, ( Void ) null );
        return new DoubleBinaryOperation( dbop.getOperator(), firstOperand, secondOperand );
    }

    public DoubleOperation visit( DoubleUnaryOperation duop, Void param )
    {
        return new DoubleUnaryOperation( duop.getOperator(), ( DoubleOperation ) duop.getOperand().accept( this, ( Void ) null ) );
    }

    public DoubleOperation visit( DoubleValue dv, Void param )
    {
        return dv;
    }

    public DoubleOperation visit( DoubleVariable dv, Void param )
    {
        return new DoubleVariable( dv.getName() );
    }
}
