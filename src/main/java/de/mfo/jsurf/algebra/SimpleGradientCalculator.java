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

import javax.vecmath.*;

@Deprecated
public class SimpleGradientCalculator implements GradientCalculator
{
    private PolynomialOperation gradientXExpression;
    private PolynomialOperation gradientYExpression;
    private PolynomialOperation gradientZExpression;
    
    public SimpleGradientCalculator( PolynomialOperation gradientXExpression, PolynomialOperation gradientYExpression, PolynomialOperation gradientZExpression )
    {
        this.gradientXExpression = gradientXExpression;
        this.gradientYExpression = gradientYExpression;
        this.gradientZExpression = gradientZExpression;
    }
    
    public Vector3d calculateGradient( Point3d p )
    {
        ValueCalculator vc = new ValueCalculator( p.x, p.y, p.z );

        double x = gradientXExpression.accept( vc, ( Void ) null );
        double y = gradientYExpression.accept( vc, ( Void ) null );
        double z = gradientZExpression.accept( vc, ( Void ) null );

        return new Vector3d( x, y, z );
    }
    
    public Vector3f calculateGradient( Point3f p )
    {
        Vector3d g = calculateGradient( new Point3d( p.x, p.y, p.z ) );
        return new Vector3f( ( float ) g.x, ( float ) g.y, ( float ) g.z );
    }
}
