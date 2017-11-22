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

public class Differentiator extends AbstractVisitor< PolynomialOperation, Void >
{
    private PolynomialVariable.Var var;

    public Differentiator( PolynomialVariable.Var var )
    {
        this.var = var;
    }

    public PolynomialVariable.Var getVariable()
    {
       return this.var;
    }

    public void setVariable( PolynomialVariable.Var var )
    {
        this.var = var;
    }

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
        PolynomialOperation u = pm.getFirstOperand();
        PolynomialOperation v = pm.getSecondOperand();
        PolynomialOperation udiff = u.accept( this, ( Void ) null );
        PolynomialOperation vdiff = v.accept( this, ( Void ) null );

        return new PolynomialAddition( new PolynomialMultiplication( udiff, v ), new PolynomialMultiplication( u, vdiff ) );
    }

    public PolynomialOperation visit( PolynomialPower pp, Void param )
    {
        switch( pp.getExponent() )
        {
            case 1:
                return new DoubleValue( 1.0 );
            case 2:
            {
                PolynomialOperation u = pp.getBase();
                PolynomialOperation udiff = u.accept( this, ( Void ) null );
                return new PolynomialMultiplication( new PolynomialMultiplication( new DoubleValue( ( double ) pp.getExponent() ), u ), udiff );
            }
            default:
            {
                PolynomialOperation u = pp.getBase();
                PolynomialOperation udiff = u.accept( this, ( Void ) null );
                return new PolynomialMultiplication( new PolynomialMultiplication( new DoubleValue( ( double ) pp.getExponent() ), new PolynomialPower( u, pp.getExponent() - 1 ) ), udiff );
            }
        }
    }

    public PolynomialOperation visit( PolynomialNegation pn, Void param )
    {
        return new PolynomialNegation( pn.getOperand().accept( this,( Void ) null ) );
    }

    public PolynomialOperation visit( PolynomialDoubleDivision pdd, Void param )
    {
        return new PolynomialDoubleDivision( pdd.getDividend().accept( this,( Void ) null ), pdd.getDivisor() );
    }

    public PolynomialOperation visit( PolynomialVariable pv, Void param )
    {
        if( pv.getVariable() == this.var )
            return new DoubleValue( 1.0 );
        else
            return new DoubleValue( 0.0 );
    }

    public PolynomialOperation visit( DoubleBinaryOperation dbop, Void param )
    {
        return new DoubleValue( 0.0 );
    }

    public PolynomialOperation visit( DoubleUnaryOperation duop, Void param )
    {
        return new DoubleValue( 0.0 );
    }

    public PolynomialOperation visit( DoubleValue dv, Void param )
    {
        return new DoubleValue( 0.0 );
    }

    public PolynomialOperation visit( DoubleVariable dv, Void param )
    {
        return new DoubleValue( 0.0 );
    }
}
