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

package de.mfo.jsurf.rendering.cpu;

import javax.vecmath.*;

public class Ray
{
    public Point3d o;
    public Vector3d d;

    public Ray( Point3d o, Vector3d d )
    {
        this.o = o;
        this.d = d;
    }

    public Point3d at( double t )
    {
        return Helper.interpolate1D( o, d, t );
    }

    public String toString()
    {
        return o.toString() + "+t*" + d.toString();
    }
}
