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

package de.mfo.jsurf.util;

import java.awt.Point;
import javax.vecmath.*;

public class RotateSphericalDragger
{
    Point lastLocation;
    Matrix4d rotation;
    double xSpeed;
    double ySpeed;
    
    public RotateSphericalDragger()
    {
        this( 1, 1 );
    }
    
    public RotateSphericalDragger( double xSpeed, double ySpeed )
    {
        lastLocation = new Point();
        rotation = new Matrix4d();
        rotation.setIdentity();
        this.xSpeed = xSpeed;
        this.ySpeed = ySpeed;
    }
    
    public void startDrag( Point p )
    {
        lastLocation = new Point( p );
    }
    
    public void dragTo( Point p )
    {
        double xAngle = ( lastLocation.x - p.x ) * xSpeed;
        double yAngle = ( lastLocation.y - p.y ) * ySpeed;
        
        Matrix4d rotX = new Matrix4d();
        rotX.setIdentity();
        rotX.rotX( ( Math.PI / 180.0 ) * yAngle );

        Matrix4d rotY = new Matrix4d();
        rotY.setIdentity();
        rotY.rotY( ( Math.PI / 180.0 ) * xAngle );
        
        rotation.mul( rotX );
        rotation.mul( rotY );
        
        lastLocation = new Point( p );
    }
    
    public Matrix4d getRotation()
    {
        return new Matrix4d( rotation );
    }
    
    public void setRotation( Matrix4d m )
    {
        rotation = new Matrix4d( m );
    }
    
    public double getXSpeed()
    {
        return xSpeed;
    }
    
    public void setXSpeed( double xSpeed )
    {
        this.xSpeed = xSpeed;
    }
    
    public double getYSpeed()
    {
        return ySpeed;
    }
    
    public void setYSpeed( double ySpeed )
    {
        this.ySpeed = ySpeed;
    }
}
