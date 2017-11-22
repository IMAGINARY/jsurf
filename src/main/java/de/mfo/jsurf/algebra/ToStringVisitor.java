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

public class ToStringVisitor extends AbstractVisitor< String, Void >
{
    final static String LPAR = "(";
    final static String RPAR = ")";

    private boolean onlyExplicitParentheses;

    public ToStringVisitor()
    {
        this( false );
    }

    public ToStringVisitor( boolean onlyExplicitParentheses )
    {
        this.onlyExplicitParentheses = onlyExplicitParentheses;
    }

    protected boolean getOnlyExplicitParentheses() { return onlyExplicitParentheses; }

    String lp( PolynomialOperation pop )
    {
        return !onlyExplicitParentheses || pop.hasParentheses() ? LPAR : "";
    }

    String rp( PolynomialOperation pop )
    {
        return !onlyExplicitParentheses || pop.hasParentheses() ? RPAR : "";
    }

    public String visit( PolynomialOperation pop, Void param )
    {
        return lp(pop) + pop.toString() + rp(pop);
    }

    public String visit( PolynomialAddition pa, Void param )
    {
        return lp(pa) + pa.getFirstOperand().accept( this, ( Void ) null ) + "+" + pa.getSecondOperand().accept( this, ( Void ) null ) + rp(pa);
    }

    public String visit( PolynomialSubtraction ps, Void param )
    {
        return lp(ps) + ps.getFirstOperand().accept( this, ( Void ) null ) + "-" + ps.getSecondOperand().accept( this, ( Void ) null ) + rp(ps);
    }

    public String visit( PolynomialMultiplication pm, Void param )
    {
        return lp(pm) + pm.getFirstOperand().accept( this, ( Void ) null ) + "*" + pm.getSecondOperand().accept( this, ( Void ) null ) + rp(pm);
    }

    public String visit( PolynomialPower pp, Void param )
    {
        return lp(pp) + pp.getBase().accept( this, ( Void ) null ) + "^" + pp.getExponent() + rp(pp);
    }

    public String visit( PolynomialNegation pn, Void param )
    {
        return lp(pn) + "-" + pn.getOperand().accept( this, ( Void ) null ) + rp(pn);
    }

    public String visit( PolynomialDoubleDivision pdd, Void param )
    {
        return lp(pdd) + pdd.getDividend().accept( this,( Void ) null ) + "/" + pdd.getDivisor().accept( this,( Void ) null ) + rp(pdd);
    }

    public String visit( PolynomialVariable pv, Void param )
    {
        return lp(pv) + pv.getVariable().toString() + rp(pv);
    }

    public String visit( DoubleOperation dop, Void param )
    {
        return lp(dop) + dop.toString() + rp(dop);
    }

    public String visit( DoubleBinaryOperation dbop, Void param )
    {
        String lp = lp(dbop);
        String rp = lp(dbop);
        String firstOperand = dbop.getFirstOperand().accept( this, ( Void ) null );
        String secondOperand = dbop.getSecondOperand().accept( this, ( Void ) null );

        switch( dbop.getOperator() )
        {
            case add:
                return lp + firstOperand + "+" + secondOperand + rp;
            case sub:
                return lp + firstOperand + "-" + secondOperand + rp;
            case mult:
                return lp + firstOperand + "*" + secondOperand + rp;
            case div:
                return lp + firstOperand + "/" + secondOperand + rp;
            case pow:
                return lp + firstOperand + "^" + secondOperand + rp;
            default:
                throw new UnsupportedOperationException();
        }
    }

    public String visit( DoubleUnaryOperation duop, Void param )
    {
        String lp = lp(duop);
        String rp = lp(duop);
        String operand = duop.getOperand().accept( this, ( Void ) null );

        switch( duop.getOperator() )
        {
            case neg:
                return lp + "-" + operand + rp;
            case sin:
                return "sin" + lp + operand + rp;
            case cos:
                return "cos" + lp + operand + rp;
            case tan:
                return "tan" + lp + operand + rp;
            case asin:
                return "asin" + lp + operand + rp;
            case acos:
                return "acos" + lp + operand + rp;
            case atan:
                return "atan" + lp + operand + rp;
            case exp:
                return "exp" + lp + operand + rp;
            case log:
                return "log" + lp + operand + rp;
            case sqrt:
                return "sqrt" + lp + operand + rp;
            case ceil:
                return "ceil" + lp + operand + rp;
            case floor:
                return "floor" + lp + operand + rp;
            case abs:
                return "abs" + lp + operand + rp;
            case sign:
                return "signum" + lp + operand + rp;
            default:
                throw new UnsupportedOperationException();
        }
    }

    public String visit( DoubleValue dv, Void param )
    {
        return lp(dv) + dv.toString() + rp(dv);
    }

    public String visit( DoubleVariable dv, Void param )
    {
        return lp(dv) + dv.getName() + rp(dv);
    }
}
