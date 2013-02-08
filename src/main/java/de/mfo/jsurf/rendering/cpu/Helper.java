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

final class Helper
{
    static Point3d interpolate1D( Point3d p, Vector3d d, double t )
    {
        return new Point3d( p.x + d.x * t, p.y + d.y * t, p.z + d.z * t );
    }

    static Point3d interpolate2D( Point3d p, Vector3d dx, Vector3d dy, double u, double v )
    {
        return new Point3d( p.x + dx.x * u + dy.x * v, p.y + dx.y * u + dy.y * v, p.z + dx.z * u + dy.z * v );
    }
}
