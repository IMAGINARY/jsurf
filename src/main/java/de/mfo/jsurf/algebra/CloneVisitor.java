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
        return new PolynomialAddition( pa.firstOperand.accept( this, ( Void ) null ), pa.secondOperand.accept( this, ( Void ) null ) );
    }

    public PolynomialOperation visit( PolynomialSubtraction ps, Void param )
    {
        return new PolynomialSubtraction( ps.firstOperand.accept( this, ( Void ) null ), ps.secondOperand.accept( this, ( Void ) null ) );
    }

    public PolynomialOperation visit( PolynomialMultiplication pm, Void param )
    {
    	return new PolynomialMultiplication( pm.firstOperand.accept( this, ( Void ) null ), pm.secondOperand.accept( this, ( Void ) null ) );
    }

    public PolynomialOperation visit( PolynomialPower pp, Void param )
    {
        return new PolynomialPower( pp.base.accept( this, ( Void ) null ), pp.exponent );
    }

    public PolynomialOperation visit( PolynomialNegation pn, Void param )
    {
        return new PolynomialNegation( pn.operand.accept( this, ( Void ) null ) );
    }

    public PolynomialOperation visit( PolynomialDoubleDivision pdd, Void param )
    {
        return new PolynomialDoubleDivision( pdd.dividend.accept( this, ( Void ) null ), ( DoubleOperation ) pdd.divisor.accept( this, ( Void ) null ) );
    }

    public PolynomialOperation visit( PolynomialVariable pv, Void param )
    {
        return pv;
    }

    public DoubleOperation visit( DoubleBinaryOperation dbop, Void param )
    {
        DoubleOperation firstOperand = ( DoubleOperation ) dbop.firstOperand.accept( this, ( Void ) null );
        DoubleOperation secondOperand = ( DoubleOperation ) dbop.secondOperand.accept( this, ( Void ) null );
        return new DoubleBinaryOperation( dbop.operator, firstOperand, secondOperand );
    }

    public DoubleOperation visit( DoubleUnaryOperation duop, Void param )
    {
        return new DoubleUnaryOperation( duop.operator, ( DoubleOperation ) duop.operand.accept( this, ( Void ) null ) );
    }

    public DoubleOperation visit( DoubleValue dv, Void param )
    {
        return dv;
    }

    public DoubleOperation visit( DoubleVariable dv, Void param )
    {
        return new DoubleVariable( dv.name );
    }
}
