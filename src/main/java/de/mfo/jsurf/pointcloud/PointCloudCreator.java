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

import de.mfo.jsurf.algebra.*;
import de.mfo.jsurf.parser.*;

import javax.vecmath.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.filechooser.*;

public class PointCloudCreator // implements Runnable
{
/*     SimpleGUI gui;
 *     
 *     public void run()
 *     {
 *         gui = new SimpleGUI();
 *         gui.setVisible( true );
 *     }
 *     
 */
    public static void createPointCloud( String expression, int numSamples, double scale, boolean clipToSphere, boolean showProgress, PointContainer container )
    {
        PolynomialOperation po;
        PolynomialOperation gradientX;
        PolynomialOperation gradientY;
        PolynomialOperation gradientZ;
        try
        {
            Simplificator simplificator = new Simplificator();

            po = AlgebraicExpressionParser.parse( expression );
            po = po.accept( simplificator, ( Void ) null );

            // calculate grad_x and simplify
            gradientX = po.accept( new Differentiator( PolynomialVariable.Var.x ), ( Void ) null );
            gradientX = gradientX.accept( simplificator, ( Void ) null );

            // calculate grad_y and simplify
            gradientY = po.accept( new Differentiator( PolynomialVariable.Var.y ), ( Void ) null );
            gradientY = gradientY.accept( simplificator, ( Void ) null );

            // calculate grad_z and simplify
            gradientZ = po.accept( new Differentiator( PolynomialVariable.Var.z ), ( Void ) null );
            gradientZ = gradientZ.accept( simplificator, ( Void ) null );            
        }
        catch( Throwable t )
        {
            throw new RuntimeException( "Error in Expression", t );
        }
               
        PolynomialExpansionCoefficientCalculator pecc = new PolynomialExpansionCoefficientCalculator( po );
        DChainRootFinder dcrf = new DChainRootFinder();
        GradientCalculator gc = new SimpleGradientCalculator( gradientX, gradientY, gradientZ );

        Vector3d dir;
        Point3d origin;
        Vector3d uDir;
        Vector3d vDir;

        Random r = new Random( System.currentTimeMillis() );
        double pixelDiscRadius = 0.75 / ( numSamples - 1 );

        long maxSamples = 3 * numSamples * numSamples;
        long currentSample = 0;
/*
        ProgressMonitor progressMonitor = null;
        if( showProgress )
        {
            progressMonitor = new ProgressMonitor( null, "Constructing point cloud ...", "sample 0 of " + maxSamples, 0, 100 );
            progressMonitor.setMillisToDecideToPopup( 0 );
            progressMonitor.setMillisToPopup( 0 );
        }
  */      
        for( int run = 0; run < 3; run++ )
        {
            switch( run )
            {
                case 0:
                    dir = new Vector3d( 0.0, 0.0, 1.0 ); 
                    uDir = new Vector3d( 1.0, 0.0, 0.0 );
                    vDir = new Vector3d( 0.0, 1.0, 0.0 );
                    break;
                case 1:
                    dir = new Vector3d( 0.0, 1.0, 0.0 ); 
                    uDir = new Vector3d( 1.0, 0.0, 0.0 );
                    vDir = new Vector3d( 0.0, 0.0, 1.0 );
                    break;
                case 2:
                    dir = new Vector3d( 1.0, 0.0, 0.0 ); 
                    uDir = new Vector3d( 0.0, 1.0, 0.0 );
                    vDir = new Vector3d( 0.0, 0.0, 1.0 );
                    break;
                default:
                    throw new RuntimeException( "This should never happen." );
            }
            for( int u = 0; u < numSamples; u++ )
            {
                double ud =  -1.0 + 2.0 * ( ( double ) u ) / ( numSamples - 1 );
                for( int v = 0; v < numSamples; v++ )
                {
                    double vd  = -1.0 + 2.0 * ( ( double ) v ) / ( numSamples - 1 );

                    double uRandomOffset = r.nextGaussian() * pixelDiscRadius;
                    double vRandomOffset = r.nextGaussian() * pixelDiscRadius;

                    origin = new Point3d( uDir );
                    origin.scale( ud + uRandomOffset );
                    origin.scaleAdd( vd + vRandomOffset, vDir, origin );

                    Vector2d searchInterval = new Vector2d( -1.0, 1.0 );
                    if( !clipToSphere || clipToSphere( origin, dir, searchInterval ) )
                    {                        
                        UnivariatePolynomial xPoly = new UnivariatePolynomial( origin.x / scale, dir.x / scale );
                        UnivariatePolynomial yPoly = new UnivariatePolynomial( origin.y / scale, dir.y / scale );
                        UnivariatePolynomial zPoly = new UnivariatePolynomial( origin.z / scale, dir.z / scale );
                        UnivariatePolynomial p = pecc.calculateCoefficients( xPoly, yPoly, zPoly );
                        double[] roots = dcrf.findAllRootsIn( p, searchInterval.x, searchInterval.y );
                        for( int i = 0; i < roots.length; i++ )
                        {
                            Point3d surfacePoint = new Point3d();
                            surfacePoint.scaleAdd( roots[ i ], dir, origin );

                            Vector3d normal = gc.calculateGradient( surfacePoint );
                            container.add( surfacePoint, normal );
                        }
                    }                    
                    currentSample++;
/*
                    if( showProgress )
                    {
                        long progress = ( 100 * currentSample ) / ( maxSamples - 1 );
                        if( progress != ( 100 * ( currentSample - 1 ) ) / ( maxSamples - 1 ) )
                            setProgress( progressMonitor, ( int ) progress, "sample " + currentSample + " of " + maxSamples );
                    }
*/
                }
            }
        }
/*
        if( showProgress )
            progressMonitor.setProgress( 100 );
*/
    }
/*
    private static void setProgress( final ProgressMonitor progressMonitor, final int progress, final String note )
    {
        SwingUtilities.invokeLater(
            new Runnable()
            {
                public void run()
                {
                    progressMonitor.setProgress( progress );
                    progressMonitor.setNote( note );
                }
            } );
    }
*/
    public static void export( String expression, int numSamples, double scale, boolean clipToSphere )
    {
        FileNameExtensionFilter csvFilter = new FileNameExtensionFilter( "Comma separated points (.csv)", "csv" );
        FileNameExtensionFilter objFilter = new FileNameExtensionFilter( "WaveFront OBJ (.obj)", "obj" );
        FileNameExtensionFilter ntpsFilter = new FileNameExtensionFilter( "PoissonRecon (.npts)", "npts" );
        
        JFileChooser fc = new JFileChooser();
        fc.setAcceptAllFileFilterUsed( false );
        fc.addChoosableFileFilter( csvFilter );
        fc.addChoosableFileFilter( objFilter );
        fc.addChoosableFileFilter( ntpsFilter );
        fc.setFileFilter( csvFilter );
        
        if( fc.showSaveDialog( null ) == JFileChooser.APPROVE_OPTION )
        {
            File file = fc.getSelectedFile();
            
            if( file.exists() )
                if( JOptionPane.CANCEL_OPTION == JOptionPane.showConfirmDialog( fc, "Overwrite existing file?", "Confirm Overwrite", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE ) )
                    return;
            
            final Exporter exporter;
            try
            {
                String defaultExtension = "." + ( ( FileNameExtensionFilter ) fc.getFileFilter() ).getExtensions()[ 0 ];
                if( !file.getPath().endsWith( defaultExtension ) )
                    file = new File( file.getPath() + defaultExtension );
                
                if( fc.getFileFilter() == csvFilter )
                    exporter = new CSVPointExporter( file ); 
                else if( fc.getFileFilter() == objFilter )
                    exporter = new OBJExporter( file ); 
                else if( fc.getFileFilter() == ntpsFilter )
                    exporter = new PoissonReconExporter( file );
                else
                    return;
            }
            catch( IOException ioe )
            {
                JOptionPane.showMessageDialog( null, "Could not write to file " + file.getPath() ); 
                return;
            }

            // Point container, that writes the points immediately into the exporter
            PointContainer pc = new PointContainer()
            {
                public void add( Point3d p, Vector3d n ) { exporter.export( p, n ); }
            };
            createPointCloud( expression, numSamples, scale, clipToSphere, true, pc );
            exporter.finishExport();
        }
    }
/*    
    public static void main( String[] args )
    {
        try
        {
            UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
        }
        catch (Exception e)
        {
        }        
        
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater( new PointCloudCreator() );        
    }
*/    
    public static boolean clipToSphere( Point3d o, Vector3d d, Vector2d interval )
    {
        Vector3d my_o = new Vector3d( o );
        Vector3d my_d = new Vector3d( d );
        double length = my_d.length();
        my_d.scale( 1.0f / length );

        // solve algebraic
        double B = -my_o.dot( my_d );
        double C = my_o.dot( my_o ) - 1.0f;
        double D = B * B - C;

        if( D < 0.0 )
            return false;

        double sqrtD =  Math.sqrt( D );
        interval.set( B - sqrtD, B + sqrtD );
        interval.scale( 1.0f / length );
        return true;
    }    
}
