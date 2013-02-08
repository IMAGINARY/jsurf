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

public class CSVPointExporter implements Exporter
{
    PrintStream ps;
    
    public CSVPointExporter( File file )
        throws IOException
    {
        ps = new PrintStream( new FileOutputStream( file ) );
    }
    
    public void startExport() {}
    
    public void export( Point3d p, Vector3d normal )
    {
        ps.println( p.x + "," + p.y + "," + p.z );
    }
    
    public void exportBBox( double size ) {}
    
    public void finishExport()
    {
        ps.flush();
        ps.close();
    }    
}
