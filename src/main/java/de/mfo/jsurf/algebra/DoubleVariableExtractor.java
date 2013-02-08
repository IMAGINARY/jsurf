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

public class DoubleVariableExtractor extends AbstractVisitor< Set< String >, Void >
{
    public Set< String > visit( PolynomialAddition pa, Void param )
    {
        Set< String > s = pa.firstOperand.accept( this, ( Void ) null );
        s.addAll( pa.secondOperand.accept( this, ( Void ) null ) );
        return s;
    }

    public Set< String > visit( PolynomialSubtraction ps, Void param )
    {
        Set< String > s = ps.firstOperand.accept( this, ( Void ) null );
        s.addAll( ps.secondOperand.accept( this, ( Void ) null ) );
        return s;
    }

    public Set< String > visit( PolynomialMultiplication pm, Void param )
    {
        Set< String > s = pm.firstOperand.accept( this, ( Void ) null );
        s.addAll( pm.secondOperand.accept( this, ( Void ) null ) );
        return s;
    }

    public Set< String > visit( PolynomialPower pp, Void param )
    {
        return pp.base.accept( this, ( Void ) null );
    }

    public Set< String > visit( PolynomialNegation pn, Void param )
    {
        return pn.operand.accept( this, ( Void ) null );
    }

    public Set< String > visit( PolynomialDoubleDivision pdd, Void param )
    {
        Set< String > s = pdd.dividend.accept( this, ( Void ) null );
        s.addAll( pdd.divisor.accept( this, ( Void ) null ) );
        return s;
    }

    public Set< String > visit( PolynomialVariable pv, Void param )
    {
        return new HashSet< String >();
    }

    public Set< String > visit( DoubleBinaryOperation dbop, Void param )
    {
        Set< String > s = dbop.firstOperand.accept( this, ( Void ) null );
        s.addAll( dbop.secondOperand.accept( this, ( Void ) null ) );
        return s;
    }

    public Set< String > visit( DoubleUnaryOperation duop, Void param )
    {
        return duop.operand.accept( this, ( Void ) null );
    }

    public Set< String > visit( DoubleValue dv, Void param )
    {
        return new HashSet< String >();
    }

    public Set< String > visit( DoubleVariable dv, Void param )
    {
        HashSet< String > s = new HashSet< String >();
        s.add( dv.name );
        return s;
    }
}
