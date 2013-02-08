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

package de.mfo.jsurf.pointcloud;

import javax.vecmath.*;
import java.io.*;

public class OBJExporter implements Exporter
{
    PrintStream ps;
    int counter;
    
    public OBJExporter( File file )
        throws IOException
    {
        ps = new PrintStream( new FileOutputStream( file ) );
        counter = 1;
    }

    public void startExport() {}
    
    public void export( Point3d p, Vector3d normal )
    {
        double nLength = normal.length();
        ps.println( "v " + p.x + " " + p.y + " " + p.z );
        ps.println( "vn " + normal.x / nLength + " " + normal.y / nLength + " " + normal.z / nLength );
        ps.println( "p " + counter + "//" + counter );
        counter++;
    }
    
    public void exportBBox( double size )
    {
        double[][] points = {
            { 1, 1, 1 },
            { 1, 1, -1 },
            { 1, -1, 1 },
            { 1, -1, -1 },
            { -1, 1, 1 },
            { -1, 1, -1 },
            { -1, -1, 1 },
            { -1, -1, -1 }
        };
        
        // export vertices
        ps.println();
        for( int i = 0; i < points.length; i++ )
            ps.println( "v " + points[ i ][ 0 ] + " " + points[ i ][ 1 ] + " " + points[ i ][ 2 ] );
        
        // export faces
        exportBBoxFace( counter, 0, 4, 6, 2 ); // front face
        exportBBoxFace( counter, 1, 3, 7, 5 ); // back face
        exportBBoxFace( counter, 5, 7, 6, 4 ); // left face
        exportBBoxFace( counter, 0, 2, 3, 1 ); // right face
        exportBBoxFace( counter, 2, 6, 7, 3 ); // bottom face
        exportBBoxFace( counter, 0, 1, 5, 4 ); // top face
        
        counter += 8;
    }
    
    private void exportBBoxFace( int offset, int i1, int i2, int i3, int i4 )
    {
        i1 += offset;
        i2 += offset;
        i3 += offset;
        i4 += offset;
        
        ps.println( "f " + i1 + " " + i2 + " " + i3 + " " + i4  );
    }
    
    public void finishExport()
    {
        ps.flush();
        ps.close();
    }
}
