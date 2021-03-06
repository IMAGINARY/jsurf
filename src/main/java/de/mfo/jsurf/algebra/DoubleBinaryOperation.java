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

public class DoubleBinaryOperation implements DoubleOperation
{
    public enum Op { add, sub, mult, div, pow; }

    private Op operator;
    private DoubleOperation firstOperand;
    private DoubleOperation secondOperand;
    private boolean hasParentheses;

    public DoubleBinaryOperation( Op operator, DoubleOperation firstOperand, DoubleOperation secondOperand )
    {
        this( operator, firstOperand, secondOperand, false );
    }

    public DoubleBinaryOperation( Op operator, DoubleOperation firstOperand, DoubleOperation secondOperand, boolean hasParentheses )
    {
        this.operator = operator;
        this.firstOperand = firstOperand;
        this.secondOperand = secondOperand;
        this.hasParentheses = hasParentheses;
    }

    public Op getOperator() { return operator; }
    public DoubleOperation getFirstOperand() { return firstOperand; }
    public DoubleOperation getSecondOperand() { return secondOperand; }
    public boolean hasParentheses() { return hasParentheses; }

    public < RETURN_TYPE, PARAM_TYPE > RETURN_TYPE accept( Visitor< RETURN_TYPE, PARAM_TYPE > visitor, PARAM_TYPE arg )
    {
        return visitor.visit( this, arg );
    }
}
