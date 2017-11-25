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

public interface Visitor< RETURN_TYPE, PARAM_TYPE >
{
    public RETURN_TYPE visit( PolynomialOperation pop, PARAM_TYPE param );
    public RETURN_TYPE visit( PolynomialAddition pa, PARAM_TYPE param );
    public RETURN_TYPE visit( PolynomialSubtraction ps, PARAM_TYPE param );
    public RETURN_TYPE visit( PolynomialMultiplication pm, PARAM_TYPE param );
    public RETURN_TYPE visit( PolynomialPower pp, PARAM_TYPE param );
    public RETURN_TYPE visit( PolynomialNegation pn, PARAM_TYPE param );
    public RETURN_TYPE visit( PolynomialDoubleDivision pdd, PARAM_TYPE param );
    public RETURN_TYPE visit( PolynomialVariable pv, PARAM_TYPE param );

    public RETURN_TYPE visit( DoubleOperation dop, PARAM_TYPE param );
    public RETURN_TYPE visit( DoubleBinaryOperation dbop, PARAM_TYPE param );
    public RETURN_TYPE visit( DoubleUnaryOperation duop, PARAM_TYPE param );
    public RETURN_TYPE visit( DoubleValue dv, PARAM_TYPE param );
    public RETURN_TYPE visit( DoubleVariable dv, PARAM_TYPE param );
}
