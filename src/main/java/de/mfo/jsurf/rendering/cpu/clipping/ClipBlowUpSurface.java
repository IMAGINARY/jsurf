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

package de.mfo.jsurf.rendering.cpu.clipping;

import javax.vecmath.*;

public class ClipBlowUpSurface extends ClipToTorus
{
    public ClipBlowUpSurface() { super(); }
    public ClipBlowUpSurface( double R, double r ) { super( R, r ); }

    @Override
    public boolean clipPoint( Point3d p )
    {
	double u = p.x;
	double tmp = Math.sqrt( p.y*p.y + p.z*p.z );
	double v = R + tmp;
	double v_inside, v_outside;
	if( u * u + v * v < R * R )
	{
		v_inside = v;
		v_outside = R - tmp;
	}
	else
	{
		v_inside = R - tmp;
		v_outside = v;
	}

	if( u * u + v_inside * v_inside > r * r )
		return false; // outside of cylinder/torus

	double blowup_eps = 0.0001;
	if( false )
	{ // first variant
		double f = blowup_f( u, v_inside );
		double g = blowup_g( u, v_inside );
		return Math.max( Math.abs( p.z * ( g*g + f*f ) - 2.0 * ( R - v_inside ) * f * g ), Math.abs( p.y * ( g*g + f*f ) + ( R - v_inside ) * ( g * g - f * f ) ) ) <= blowup_eps * ( g*g + f*f );
	}
	else
	{ // second variant

            double f = blowup_f( u, v_outside );
            double g = blowup_g( u, v_outside );
            boolean p_inside = Math.max( Math.abs( p.z * ( g*g + f*f ) - 2.0 * ( R - v_outside ) * f * g ), Math.abs( p.y * ( g*g + f*f ) + ( R - v_outside ) * ( g * g - f * f ) ) ) >= blowup_eps * ( g*g + f*f );

            if( p_inside )
                    return true;
            else
            {
                    // p seems to originate from part of surface out the disc
                    // -> if gradient is small, we might be at a self intersection
                    //return length( gradient( p ) ) <= 0.001;
                    return false;
            }
	}
    }

    double blowup_f( double u, double v ) { return u * u - 0.25; }
    double blowup_g( double u, double v ) { return v * v - 0.25; }

//    abstract double blowup_f( double u, double v );
//    abstract double blowup_g( double u, double v );

    @Override
    public boolean pointClippingNecessary() { return true; }
}
