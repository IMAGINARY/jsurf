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

public class DoubleVariableRenameVisitor extends AbstractVisitor< PolynomialOperation, Void >
{
	private Map< String, String > m;
	
	public DoubleVariableRenameVisitor( Map< String, String > m ) { this.m = m; }
	
    public PolynomialOperation visit( PolynomialAddition pa, Void param )
    {
        pa.firstOperand.accept( this, ( Void ) null );
        pa.secondOperand.accept( this, ( Void ) null );
        return pa;
    }

    public PolynomialOperation visit( PolynomialSubtraction ps, Void param )
    {
        ps.firstOperand.accept( this, ( Void ) null );
        ps.secondOperand.accept( this, ( Void ) null );
        return ps;
    }

    public PolynomialOperation visit( PolynomialMultiplication pm, Void param )
    {
        pm.firstOperand.accept( this, ( Void ) null );
        pm.secondOperand.accept( this, ( Void ) null );
        return pm;
    }

    public PolynomialOperation visit( PolynomialPower pp, Void param )
    {
        return pp.base.accept( this, ( Void ) null );
    }

    public PolynomialOperation visit( PolynomialNegation pn, Void param )
    {
        pn.operand.accept( this, ( Void ) null );
        return pn;
    }

    public PolynomialOperation visit( PolynomialDoubleDivision pdd, Void param )
    {
        pdd.dividend.accept( this, ( Void ) null );
        pdd.divisor.accept( this, ( Void ) null );
        return pdd;
    }

    public PolynomialOperation visit( PolynomialVariable pv, Void param )
    {
        return pv;
    }

    public PolynomialOperation visit( DoubleBinaryOperation dbop, Void param )
    {
        dbop.firstOperand.accept( this, ( Void ) null );
        dbop.secondOperand.accept( this, ( Void ) null );
        return dbop;
    }

    public PolynomialOperation visit( DoubleUnaryOperation duop, Void param )
    {
        return duop.operand.accept( this, ( Void ) null );
    }

    public PolynomialOperation visit( DoubleValue dv, Void param )
    {
        return dv;
    }

    public PolynomialOperation visit( DoubleVariable dv, Void param )
    {	
    	String new_name = m.get( dv.name );
    	dv.name = new_name != null ? new_name : dv.name;
        return dv;
    }
}
