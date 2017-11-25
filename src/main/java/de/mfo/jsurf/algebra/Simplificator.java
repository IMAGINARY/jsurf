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

import java.util.*;

public class Simplificator extends AbstractVisitor< PolynomialOperation, Void >
{
    private Map< String, java.lang.Double > dict;

    public Simplificator()
    {
        this.dict = new HashMap< String, java.lang.Double >();
    }

    public double getParameterValue( String name )
    {
        return this.dict.get( name );
    }

    public Set< String > getKnownParameterNames()
    {
        return this.dict.keySet();
    }

    public Set< Map.Entry< String, java.lang.Double > > getKnownParameters()
    {
        return this.dict.entrySet();
    }

    public void setParameterValue( String name, double value )
    {
        this.dict.put( name, value );
    }

    public void unsetParameterValue( String name )
    {
        this.dict.remove(name);
    }

    public PolynomialOperation visit( PolynomialAddition pa, Void param )
    {
        PolynomialOperation firstOperand = pa.getFirstOperand().accept( this, ( Void ) null );
        PolynomialOperation secondOperand = pa.getSecondOperand().accept( this, ( Void ) null );

        try
        {
            if( ( ( DoubleValue ) firstOperand ).getValue() == 0.0 )
                return secondOperand;
        }
        catch( ClassCastException cce )
        {
        }
        try
        {
            if( ( ( DoubleValue ) secondOperand ).getValue() == 0.0 )
                return firstOperand;
        }
        catch( ClassCastException cce )
        {
        }
        try
        {
            return new DoubleBinaryOperation( DoubleBinaryOperation.Op.add, ( DoubleOperation ) firstOperand, ( DoubleOperation ) secondOperand );
        }
        catch( ClassCastException cce )
        {
        }
        return new PolynomialAddition( firstOperand, secondOperand );
    }

    public PolynomialOperation visit( PolynomialSubtraction ps, Void param )
    {
        PolynomialOperation firstOperand = ps.getFirstOperand().accept( this, ( Void ) null );
        PolynomialOperation secondOperand = ps.getSecondOperand().accept( this, ( Void ) null );

        try
        {
            if( ( ( DoubleValue ) firstOperand ).getValue() == 0.0 )
                return new PolynomialNegation( secondOperand ).accept( this, ( Void ) null );
        }
        catch( ClassCastException cce )
        {
        }
        try
        {
            if( ( ( DoubleValue ) secondOperand ).getValue() == 0.0 )
                return firstOperand;
        }
        catch( ClassCastException cce )
        {
        }
        try
        {
            return new DoubleBinaryOperation( DoubleBinaryOperation.Op.sub, ( DoubleOperation ) firstOperand, ( DoubleOperation ) secondOperand );
        }
        catch( ClassCastException cce )
        {
        }
        return new PolynomialSubtraction( firstOperand, secondOperand );
    }

    public PolynomialOperation visit( PolynomialMultiplication pm, Void param )
    {
        PolynomialOperation firstOperand = pm.getFirstOperand().accept( this, ( Void ) null );
        PolynomialOperation secondOperand = pm.getSecondOperand().accept( this, ( Void ) null );

        try
        {
            if( ( ( DoubleValue ) firstOperand ).getValue() == 0.0 )
                return firstOperand;
            else if( ( ( DoubleValue ) firstOperand ).getValue() == 1.0 )
                return secondOperand;
        }
        catch( ClassCastException cce )
        {
        }
        try
        {
            if( ( ( DoubleValue ) secondOperand ).getValue() == 0.0 )
                return secondOperand;
            else if( ( ( DoubleValue ) secondOperand ).getValue() == 1.0 )
                return firstOperand;
        }
        catch( ClassCastException cce )
        {
        }
        try
        {
            return new DoubleBinaryOperation( DoubleBinaryOperation.Op.mult, ( DoubleOperation ) firstOperand, ( DoubleOperation ) secondOperand );
        }
        catch( ClassCastException cce )
        {
        }
        return new PolynomialMultiplication( firstOperand, secondOperand );
    }

    public PolynomialOperation visit( PolynomialPower pp, Void param )
    {
        PolynomialOperation base = pp.getBase().accept( this, ( Void ) null );
        if( pp.getExponent() == 0 )
        {
            return new DoubleValue( 1.0 );
        }
        else if( pp.getExponent() == 1 )
        {
            return base;
        }
        else
        {
        }
        try
        {
            double dBase = ( ( DoubleValue ) base ).getValue();
            return new DoubleValue( Math.pow( dBase, pp.getExponent() ) );
        }
        catch( ClassCastException cce )
        {
        }
        return new PolynomialPower( base, pp.getExponent() );
    }

    public PolynomialOperation visit( PolynomialNegation pn, Void param )
    {
        PolynomialOperation operand = pn.getOperand().accept( this, ( Void ) null );
        try
        {
            return new DoubleValue( -( ( DoubleValue ) operand ).getValue() );
        }
        catch( ClassCastException cce )
        {
            return new PolynomialNegation( operand );
        }
    }

    public PolynomialOperation visit( PolynomialDoubleDivision pdd, Void param )
    {
        PolynomialOperation dividend = pdd.getDividend().accept( this, ( Void ) null );
        PolynomialOperation divisor = pdd.getDivisor().accept( this, ( Void ) null );
        try
        {
            return new DoubleValue( ( ( DoubleValue ) dividend ).getValue() / ( ( DoubleValue ) divisor ).getValue() );
        }
        catch( ClassCastException cce1 )
        {
        }
        try
        {
            return new DoubleBinaryOperation( DoubleBinaryOperation.Op.div, ( DoubleOperation ) dividend, ( DoubleOperation ) divisor );
        }
        catch( ClassCastException cce2 )
        {
            return new PolynomialDoubleDivision( dividend, ( DoubleOperation ) divisor );
        }
    }

    public PolynomialOperation visit( PolynomialVariable pv, Void param )
    {
        return pv;
    }

    public DoubleOperation visit( DoubleBinaryOperation dbop, Void param )
    {
        DoubleOperation firstOperand = ( DoubleOperation ) dbop.getFirstOperand().accept( this, ( Void ) null );
        DoubleOperation secondOperand = ( DoubleOperation ) dbop.getSecondOperand().accept( this, ( Void ) null );

        try
        {
            double firstValue = ( ( DoubleValue ) firstOperand ).getValue();
            double secondValue = ( ( DoubleValue ) secondOperand ).getValue();
            double result;
            switch( dbop.getOperator() )
            {
                case add:
                    result = firstValue + secondValue;
                    break;
                case sub:
                    result = firstValue - secondValue;
                    break;
                case mult:
                    result = firstValue * secondValue;
                    break;
                case div:
                    result = firstValue / secondValue;
                    break;
                case pow:
                    result = Math.pow( firstValue, secondValue );
                    break;
                default:
                    throw new UnsupportedOperationException();
            }
            return new DoubleValue( result );
        }
        catch( ClassCastException cce )
        {
            return new DoubleBinaryOperation( dbop.getOperator(), firstOperand, secondOperand );
        }
    }

    public DoubleOperation visit( DoubleUnaryOperation duop, Void param )
    {
        DoubleOperation operand = ( DoubleOperation ) duop.getOperand().accept( this, ( Void ) null );
        try
        {
            double value = ( ( DoubleValue ) operand ).getValue();
            double result;
            switch( duop.getOperator() )
            {
                case neg:
                    result = -value;
                    break;
                case sin:
                    result = Math.sin( value );
                    break;
                case cos:
                    result = Math.cos( value );
                    break;
                case tan:
                    result = Math.tan( value );
                    break;
                case asin:
                    result = Math.asin( value );
                    break;
                case acos:
                    result = Math.acos( value );
                    break;
                case atan:
                    result = Math.atan( value );
                    break;
                case exp:
                    result = Math.exp( value );
                    break;
                case log:
                    result = Math.log( value );
                    break;
                case sqrt:
                    result = Math.sqrt( value );
                    break;
                case ceil:
                    result = Math.ceil( value );
                    break;
                case floor:
                    result = Math.floor( value );
                    break;
                case abs:
                    result = Math.abs( value );
                    break;
                case sign:
                    result = Math.signum( value );
                    break;
                default:
                    throw new UnsupportedOperationException();
            }
            return new DoubleValue( result );
        }
        catch( ClassCastException cce )
        {
            return new DoubleUnaryOperation( duop.getOperator(), operand );
        }
    }

    public DoubleOperation visit( DoubleValue dv, Void param )
    {
        return dv;
    }

    public DoubleOperation visit( DoubleVariable dv, Void param )
    {
        try
        {
            return new DoubleValue( this.dict.get( dv.getName() ) );
        }
        catch( NullPointerException npe )
        {
            return dv;
        }
    }
}
