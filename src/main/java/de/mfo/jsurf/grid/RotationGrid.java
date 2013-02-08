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

package de.mfo.jsurf.grid;

import de.mfo.jsurf.algebra.*;
import de.mfo.jsurf.parser.*;
import de.mfo.jsurf.rendering.*;
import de.mfo.jsurf.rendering.cpu.*;
import de.mfo.jsurf.util.*;
import static de.mfo.jsurf.rendering.cpu.CPUAlgebraicSurfaceRenderer.AntiAliasingMode;

import java.awt.Graphics2D;
import java.awt.image.*;
import java.awt.Point;
import java.awt.geom.*;
import javax.vecmath.*;
import javax.imageio.*;

// input/output
import java.net.URL;
import java.util.*;
import java.io.*;

import org.apache.commons.cli.*;

public class RotationGrid
{
    static CPUAlgebraicSurfaceRenderer asr;
    static Matrix4d additional_rotation;
    static Matrix4d basic_rotation;
    static Matrix4d scale;
    static int size;
    static AntiAliasingMode aam;
    static AntiAliasingPattern aap;
 
    public static BufferedImage renderAnimGrid( int xAngleMin, int xAngleMax, int xSteps, int yAngleMin, int yAngleMax, int ySteps  )
    {
    	BufferedImage grid = new BufferedImage( ySteps * size, xSteps * size, BufferedImage.TYPE_INT_RGB );
    	Graphics2D g2 = (Graphics2D) grid.getGraphics();
    	for( int x = 0; x < xSteps; ++x )
    	{
    		double xAngle = xAngleMin + ( xAngleMax - xAngleMin ) * ( xSteps == 1 ? 0.5 : ( x / ( double ) (xSteps - 1) ) );
    		Matrix4d matRotX = new Matrix4d();
    		matRotX.setIdentity();
    		matRotX.rotX( Math.toRadians( xAngle ) );
        	for( int y = 0; y < ySteps; ++y )
    		{
        		double yAngle = yAngleMin + ( yAngleMax - yAngleMin ) * ( ySteps == 1 ? 0.5 : ( y / ( double ) (ySteps - 1) ) );
        		Matrix4d matRotY = new Matrix4d();
        		matRotY.setIdentity();
        		matRotY.rotY( Math.toRadians( yAngle ) );
        		additional_rotation.mul( matRotY, matRotX );
        		BufferedImage bi = createBufferedImageFromRGB( draw( size, size, aam, aap ));
        		g2.drawImage( bi, new AffineTransformOp( new AffineTransform(), AffineTransformOp.TYPE_NEAREST_NEIGHBOR ), ( ySteps - 1 - y ) * size, x * size );
    		}
    	}
    	return grid;
    }

    public static ImgBuffer draw( int width, int height, CPUAlgebraicSurfaceRenderer.AntiAliasingMode aam, AntiAliasingPattern aap )
    {
        // create color buffer
        ImgBuffer ib = new ImgBuffer( width, height );

        // do rendering
        Matrix4d transform = new Matrix4d();
        transform.mul( basic_rotation, additional_rotation );
        asr.setTransform( transform );
        asr.setSurfaceTransform( scale );
        asr.setAntiAliasingMode( aam );
        asr.setAntiAliasingPattern( aap );
        setOptimalCameraDistance( asr.getCamera() );

        try
        {
            asr.draw( ib.rgbBuffer, width, height );
            return ib;
        }
        catch( RenderingInterruptedException rie )
        {
            return null;
        }
        catch( Throwable t )
        {
            t.printStackTrace();
            return null;
        }
    }

    public static void setScale( double scaleFactor )
    {
        scaleFactor= Math.pow( 10, scaleFactor);
        scale.setScale( scaleFactor );
    }
    
    static BufferedImage createBufferedImageFromRGB( ImgBuffer ib )
    {
        int w = ib.width;
        int h = ib.height;

        DirectColorModel colormodel = new DirectColorModel( 24, 0xff0000, 0xff00, 0xff );
        SampleModel sampleModel = colormodel.createCompatibleSampleModel( w, h );
        DataBufferInt data = new DataBufferInt( ib.rgbBuffer, w * h );
        WritableRaster raster = WritableRaster.createWritableRaster( sampleModel, data, new Point( 0, 0 ) );
        return new BufferedImage( colormodel, raster, false, null );
    }

    public static void saveToPNG( OutputStream os, BufferedImage bufferedImage )
            throws java.io.IOException
    {
        AffineTransform tx = AffineTransform.getScaleInstance(1, -1);
        tx.translate(0, -bufferedImage.getHeight(null));
        AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        bufferedImage = op.filter(bufferedImage, null);
        javax.imageio.ImageIO.write( bufferedImage, "png", os );
    }

    protected static void setOptimalCameraDistance( Camera c )
    {
        float cameraDistance;
        switch( c.getCameraType() )
        {
            case ORTHOGRAPHIC_CAMERA:
                cameraDistance = 1.0f;
                break;
            case PERSPECTIVE_CAMERA:
                cameraDistance = ( float ) ( 1.0 / Math.sin( ( Math.PI / 180.0 ) * ( c.getFoVY() / 2.0 ) ) );
                break;
            default:
                throw new RuntimeException();
        }
        c.lookAt( new Point3d( 0, 0, cameraDistance ), new Point3d( 0, 0, -1 ), new Vector3d( 0, 1, 0 ) );
    }

    public static void loadFromString( String s )
            throws Exception
    {
        Properties props = new Properties();
        props.load( new ByteArrayInputStream( s.getBytes() ) );
        loadFromProperties( props );
    }

    public static void loadFromFile( URL url )
            throws IOException, Exception
    {
        Properties props = new Properties();
        props.load( url.openStream() );
        loadFromProperties( props );
    }

    public static void loadFromProperties( Properties props )
            throws Exception
    {
        asr.setSurfaceFamily( props.getProperty( "surface_equation" ) );

        Set< Map.Entry< Object, Object > > entries = props.entrySet();
        String parameter_prefix = "surface_parameter_";
        for( Map.Entry< Object, Object > entry : entries )
        {
            String name = (String) entry.getKey();
            if( name.startsWith( parameter_prefix ) )
            {
                String parameterName = name.substring( parameter_prefix.length() );
                asr.setParameterValue( parameterName, Float.parseFloat( ( String ) entry.getValue() ) );
            }
        }

        asr.getCamera().loadProperties( props, "camera_", "" );
        asr.getFrontMaterial().loadProperties(props, "front_material_", "");
        asr.getBackMaterial().loadProperties(props, "back_material_", "");
        for( int i = 0; i < asr.MAX_LIGHTS; i++ )
        {
            asr.getLightSource( i ).setStatus(LightSource.Status.OFF);
            asr.getLightSource( i ).loadProperties( props, "light_", "_" + i );
        }
        asr.setBackgroundColor( BasicIO.fromColor3fString( props.getProperty( "background_color" ) ) );
        setScale( Float.parseFloat( props.getProperty( "scale_factor" ) ) );
        basic_rotation = BasicIO.fromMatrix4dString( props.getProperty( "rotation_matrix" ) );
    }
    
    public static void main( String args[] )
	{
    	size = 100;
    	int xAngleMin = -90;
    	int xAngleMax = 90;
    	int xSteps = 11;
    	int yAngleMin = -90;
    	int yAngleMax = 90;
    	int ySteps = 11;
    	String jsurf_filename = "";
    	String output_filename = null;
    	
    	Options options = new Options();
    	
    	options.addOption("s","size", true, "width (and height) of a tile image (default: " + size + ")");
    	options.addOption("minXAngle", true, "minimum rotation in x direction (default: " + xAngleMin + ")");
    	options.addOption("maxXAngle", true, "maximum rotation in x direction (default: " + xAngleMax + ")");
    	options.addOption("stepsX",true,"number of steps in x range of rotation (default: " + ySteps + ")");
    	options.addOption("minYAngle", true, "minimum rotation in y direction (default: " + yAngleMin + ")");
    	options.addOption("maxYAngle", true, "maximum rotation in y direction (default: " + yAngleMax + ")");
    	options.addOption("stepsY",true,"number of steps in y range of rotation (default: " + ySteps + ")");
    	options.addOption("q","quality",true,"quality of the rendering: 0 (low), 1 (medium, default), 2 (high), 3 (extreme)");
    	options.addOption("o","output",true,"output PNG into this file (otherwise STDOUT)");
    	
    	

    	CommandLineParser parser = new PosixParser();
		HelpFormatter formatter = new HelpFormatter();
    	String help_header = "RotationGrid [options] jsurf_file";
    	try
    	{
    		CommandLine cmd = parser.parse( options, args );
    		
    		if( cmd.getArgs().length > 0)
    			jsurf_filename = cmd.getArgs()[ 0 ];
    		else
    		{
    			formatter.printHelp( help_header, options );
    			return;
    		}
    		
    		if( cmd.hasOption( "output" ) )
    			output_filename = cmd.getOptionValue("output");
    		
    		if( cmd.hasOption("size") )
    			size = Integer.parseInt( cmd.getOptionValue("size") );
    		
    		if( cmd.hasOption("minXAngle") )
    			xAngleMin = Integer.parseInt( cmd.getOptionValue("minXAngle") );
    		
    		if( cmd.hasOption("maxXAngle") )
    			xAngleMax = Integer.parseInt( cmd.getOptionValue("maxXAngle") );
    	
    		if( cmd.hasOption("stepsX") )
    			xSteps = Integer.parseInt( cmd.getOptionValue("stepsX") );
    		
    		if( cmd.hasOption("minYAngle") )
    			yAngleMin = Integer.parseInt( cmd.getOptionValue("minYAngle") );

    		if( cmd.hasOption("maxYAngle") )
    			yAngleMax = Integer.parseInt( cmd.getOptionValue("maxYAngle") );

    		if( cmd.hasOption("stepsY") )
    			ySteps = Integer.parseInt( cmd.getOptionValue("stepsY") );

    		if( cmd.hasOption("stepsY") )
    			ySteps = Integer.parseInt( cmd.getOptionValue("stepsY") );
    		
    		int quality = 1;
    		if( cmd.hasOption("quality") )
    			quality = Integer.parseInt( cmd.getOptionValue( "quality" ) );
			switch( quality )
			{
			case 0:
		    	aam = AntiAliasingMode.ADAPTIVE_SUPERSAMPLING;
		    	aap = AntiAliasingPattern.OG_1x1;
		    	break;
			case 2:
		    	aam = AntiAliasingMode.ADAPTIVE_SUPERSAMPLING;
		    	aap = AntiAliasingPattern.OG_4x4;
		    	break;
			case 3:
		    	aam = AntiAliasingMode.SUPERSAMPLING;
		    	aap = AntiAliasingPattern.OG_4x4;
		    	break;
			case 1:
		    	aam = AntiAliasingMode.ADAPTIVE_SUPERSAMPLING;
		    	aap = AntiAliasingPattern.QUINCUNX;
			}
    	}
    	catch( ParseException exp ) {
    	    System.out.println( "Unexpected exception:" + exp.getMessage() );
    	}
    	catch( NumberFormatException nfe )
    	{
    		formatter.printHelp( "RotationGrid", options );
    	}
    	
        asr = new CPUAlgebraicSurfaceRenderer();

        scale = new Matrix4d();   
        scale.setIdentity();
        setScale(0.0);
        
        basic_rotation = new Matrix4d();
        basic_rotation.setIdentity();
        
        additional_rotation = new Matrix4d();
        additional_rotation.setIdentity();
        
        try
        {
        	loadFromFile( new File( jsurf_filename ).toURI().toURL() );
        }
        catch( Exception e )
        {
        	e.printStackTrace();
        }
        setOptimalCameraDistance( asr.getCamera() );
        try
        {
        	BufferedImage bi = renderAnimGrid( xAngleMin, xAngleMax, xSteps, yAngleMin, yAngleMax, ySteps );
        	if( output_filename == null )
        		saveToPNG( System.out, bi );
        	else
        		saveToPNG( new FileOutputStream( new File( "test.png" ) ), bi );
        }
        catch( Exception e )
        {
        	e.printStackTrace();
        }
	}
}

class ImgBuffer
{
    public int[] rgbBuffer;
    public int width;
    public int height;

    public ImgBuffer( int w, int h ) { rgbBuffer = new int[ 3 * w * h ]; width = w; height = h; }
}
