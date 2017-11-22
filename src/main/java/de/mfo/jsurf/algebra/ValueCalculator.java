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

public class ValueCalculator extends AbstractVisitor< java.lang.Double, Void >
{
    private double x;
    private double y;
    private double z;

    private Map< String, java.lang.Double > dict;

    public ValueCalculator()
    {
        this( 0.0, 0.0, 0.0 );
    }

    public ValueCalculator( double x, double y, double z )
    {
        this.x = x;
        this.y = y;
        this.z = z;

        this.dict = new HashMap< String, java.lang.Double >();
    }

    public double getX()
    {
        return this.x;
    }

    public double getY()
    {
        return this.y;
    }

    public double getZ()
    {
        return this.z;
    }

    public void setX( double x )
    {
        this.x = x;
    }

    public void setY( double y )
    {
        this.y = y;
    }

    public void setZ( double z )
    {
        this.z = z;
    }

    public void setXYZ( double x, double y, double z )
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double getParameterValue( String name )
    {
        try
        {
            return this.dict.get( name );
        }
        catch( NullPointerException npe )
        {
            return java.lang.Double.NaN;
        }
    }

    public Set< String > getParameters()
    {
        return this.dict.keySet();
    }

    public void setParameterValue( String name, double value )
    {
        this.dict.put( name, value );
    }

    public java.lang.Double visit( PolynomialAddition pa, Void param )
    {
        return pa.getFirstOperand().accept( this, ( Void ) null ) + pa.getSecondOperand().accept( this, ( Void ) null );
    }

    public java.lang.Double visit( PolynomialSubtraction ps, Void param )
    {
        return ps.getFirstOperand().accept( this, ( Void ) null ) - ps.getSecondOperand().accept( this, ( Void ) null );
    }

    public java.lang.Double visit( PolynomialMultiplication pm, Void param )
    {
        return pm.getFirstOperand().accept( this, ( Void ) null ) * pm.getSecondOperand().accept( this, ( Void ) null );
    }

    public java.lang.Double visit( PolynomialPower pp, Void param )
    {
        return Math.pow( pp.getBase().accept( this, ( Void ) null ), ( double ) pp.getExponent() );
    }

    public java.lang.Double visit( PolynomialNegation pn, Void param )
    {
        return -pn.getOperand().accept( this, ( Void ) null );
    }

    public java.lang.Double visit( PolynomialDoubleDivision pdd, Void param )
    {
        return pdd.getDividend().accept( this,( Void ) null ) / pdd.getDivisor().accept( this,( Void ) null );
    }

    public java.lang.Double visit( PolynomialVariable pv, Void param )
    {
        switch( pv.getVariable() )
        {
            case x:
                return this.x;
            case y:
                return this.y;
            case z:
                return this.z;
            default:
                throw new UnsupportedOperationException();
        }
    }

    public java.lang.Double visit( DoubleBinaryOperation dbop, Void param )
    {
        double firstOperand = dbop.getFirstOperand().accept( this, ( Void ) null );
        double secondOperand = dbop.getSecondOperand().accept( this, ( Void ) null );

        switch( dbop.getOperator() )
        {
            case add:
                return firstOperand + secondOperand;
            case sub:
                return firstOperand - secondOperand;
            case mult:
                return firstOperand * secondOperand;
            case div:
                return firstOperand / secondOperand;
            case pow:
                return Math.pow( firstOperand, secondOperand );
            default:
                throw new UnsupportedOperationException();
        }
    }

    public java.lang.Double visit( DoubleUnaryOperation duop, Void param )
    {
        double operand = duop.getOperand().accept( this, ( Void ) null );

        switch( duop.getOperator() )
        {
            case neg:
                return -operand;
            case sin:
                return Math.sin( operand );
            case cos:
                return Math.cos( operand );
            case tan:
                return Math.tan( operand );
            case asin:
                return Math.asin( operand );
            case acos:
                return Math.acos( operand );
            case atan:
                return Math.atan( operand );
            case exp:
                return Math.exp( operand );
            case log:
                return Math.log( operand );
            case sqrt:
                return Math.sqrt( operand );
            case ceil:
                return Math.ceil( operand );
            case floor:
                return Math.floor( operand );
            case abs:
                return Math.abs( operand );
            case sign:
                return Math.signum( operand );
            default:
                throw new UnsupportedOperationException();
        }
    }

    public java.lang.Double visit( DoubleVariable dv, Void param )
    {
        java.lang.Double d = this.dict.get( dv.getName() );
        if( d == null )
            throw new UnsupportedOperationException( "no value has been assigned to parameter '" + dv.getName() + "'" );
        return d;
    }

    public java.lang.Double visit( DoubleValue dv, Void param )
    {
        return dv.getValue();
    }
}
