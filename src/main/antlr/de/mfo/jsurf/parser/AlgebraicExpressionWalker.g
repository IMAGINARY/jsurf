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

tree grammar AlgebraicExpressionWalker;

options { tokenVocab = AlgebraicExpression; ASTLabelType = CommonTree; }

@header
{
package de.mfo.jsurf.parser;

import de.mfo.jsurf.algebra.*;
}

@members
{
    public static PolynomialOperation createVariable( String name, boolean hasParentheses )
    {
        try
        {
            return new PolynomialVariable( PolynomialVariable.Var.valueOf( name ), hasParentheses );
        }
        catch( Exception e )
        {
            return new DoubleVariable( name, hasParentheses );
        }
    }

    public static int createInteger( String text )
    {
        try
        {
            return Integer.parseInt( text );
        }
        catch( NumberFormatException nfe )
        {
            return 0;
        }
    }
}

start returns [ PolynomialOperation op ]
    : e = expr { $op = $e.op; }
    ;

expr returns [ PolynomialOperation op, Integer decimal ]
	:  ( p = PARENTHESES )? ^( PLUS e1 = expr e2 = expr )
            {
                try
                {
                    $op = new DoubleBinaryOperation( DoubleBinaryOperation.Op.add, ( DoubleOperation ) $e1.op, ( DoubleOperation ) $e2.op, p != null );
                }
                catch( ClassCastException cce )
                {
                    $op = new PolynomialAddition( $e1.op, $e2.op, p != null );
                }
            }
        | ( p = PARENTHESES )? ^( MINUS e1 = expr ( e2 = expr )?  )
            {
                if( e2 != null )
                {
                    // subtraction
                    try
                    {
                        $op = new DoubleBinaryOperation( DoubleBinaryOperation.Op.sub, ( DoubleOperation ) $e1.op, ( DoubleOperation ) $e2.op, p != null );
                    }
                    catch( ClassCastException cce )
                    {
                        $op = new PolynomialSubtraction( $e1.op, $e2.op, p != null );
                    }
                }
                else
                {
                    try
                    {
                        $op = new DoubleUnaryOperation( DoubleUnaryOperation.Op.neg, ( DoubleOperation ) $e1.op, p != null );
                    }
                    catch( ClassCastException cce )
                    {
                        $op = new PolynomialNegation( $e1.op, p != null );
                    }
                }
            }
        | ( p = PARENTHESES )? ^( MULT e1 = expr e2 = expr )
            {
                try
                {
                    $op = new DoubleBinaryOperation( DoubleBinaryOperation.Op.mult, ( DoubleOperation ) $e1.op, ( DoubleOperation ) $e2.op );
                }
                catch( ClassCastException cce )
                {
                    $op = new PolynomialMultiplication( $e1.op, $e2.op, p != null );
                }
            }
        | ( p = PARENTHESES )? ^( DIV e1 = expr e2 = expr )
            {
                try
                {
                    $op = new DoubleBinaryOperation( DoubleBinaryOperation.Op.div, ( DoubleOperation ) $e1.op, ( DoubleOperation ) $e2.op, p != null );
                }
                catch( ClassCastException cce1 )
                {
                    try
                    {
                        $op = new PolynomialDoubleDivision( $e1.op, ( DoubleOperation ) $e2.op, p != null );
                    }
                    catch( ClassCastException cce2 )
                    {
                        throw new RecognitionException();
                    }
                }
            }
        | ( p = PARENTHESES )? ^( POW e1 = expr e2 = expr )
            {
                try
                {
                    $op = new DoubleBinaryOperation( DoubleBinaryOperation.Op.pow, ( DoubleOperation ) $e1.op, ( DoubleOperation ) $e2.op, p != null );
                }
                catch( ClassCastException cce )
                {
                    if( $e2.decimal == null )
                    {
                        throw new RecognitionException();
                    }
                    else
                    {
                        $op = new PolynomialPower( $e1.op, $e2.decimal, p != null );
                    }
                }
            }
        | ( p = PARENTHESES )? ^( id = IDENTIFIER e1 = expr ( e2 = expr )? )
            {
                if( e2 != null )
                {
                    try
                    {
                        $op = new DoubleBinaryOperation( DoubleBinaryOperation.Op.valueOf( $id.text ), ( DoubleOperation ) $e1.op, ( DoubleOperation ) $e2.op, p != null );
                    }
                    catch( ClassCastException cce )
                    {
                        throw new RecognitionException();
                    }
                    catch( IllegalArgumentException iae )
                    {
                        throw new RecognitionException();
                    }
                }
                else
                {
                    try
                    {
                        $op = new DoubleUnaryOperation( DoubleUnaryOperation.Op.valueOf( $id.text ), ( DoubleOperation ) $e1.op, p != null );
                    }
                    catch( ClassCastException cce )
                    {
                        throw new RecognitionException();
                    }
                    catch( IllegalArgumentException iae )
                    {
                        throw new RecognitionException();
                    }
                }
            }
        | pe = primary_expr { $op = $pe.op; $decimal = pe.decimal; }
	;

primary_expr returns [ PolynomialOperation op, Integer decimal ]
	: ( p = PARENTHESES )? i = DECIMAL_LITERAL { $op = new DoubleValue( $i.text, p != null ); $decimal = Integer.valueOf( createInteger( $i.text ) ); }
	| ( p = PARENTHESES )? f = FLOATING_POINT_LITERAL { $op = new DoubleValue( $f.text, p != null ); }
	| ( p = PARENTHESES )? id = IDENTIFIER { $op = createVariable( $id.text, p != null ); }
	;
