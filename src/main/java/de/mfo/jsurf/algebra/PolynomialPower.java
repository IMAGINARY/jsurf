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

public class PolynomialPower implements PolynomialOperation
{
    private PolynomialOperation base;
    private int exponent;
    private boolean hasParentheses;

    public PolynomialPower( PolynomialOperation base, int exponent )
    {
        this( base, exponent, false );
    }

    public PolynomialPower( PolynomialOperation base, int exponent, boolean hasParentheses )
    {
        if( exponent < 0 )
            throw new IllegalArgumentException( "exponent must be >= 0" );

        this.base = base;
        this.exponent = exponent;
        this.hasParentheses = hasParentheses;
    }

    public PolynomialOperation getBase() { return base; }
    public int getExponent() { return exponent; }
    public boolean hasParentheses() { return hasParentheses; }

    public < RETURN_TYPE, PARAM_TYPE > RETURN_TYPE accept( Visitor< RETURN_TYPE, PARAM_TYPE > visitor, PARAM_TYPE arg )
    {
        return visitor.visit( this, arg );
    }
}
