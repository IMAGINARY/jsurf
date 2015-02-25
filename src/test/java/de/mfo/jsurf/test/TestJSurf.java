package de.mfo.jsurf.test;

import org.junit.*;

import de.mfo.jsurf.rendering.cpu.CPUAlgebraicSurfaceRenderer;
import de.mfo.jsurf.rendering.RenderingInterruptedException;
import de.mfo.jsurf.util.FileFormat;
import java.util.Properties;
import javax.swing.SwingWorker;

public class TestJSurf
{
	@Test
	public void stopDrawingShouldInterruptRendering()
		throws java.io.IOException, Exception
	{
		final CPUAlgebraicSurfaceRenderer asr = new CPUAlgebraicSurfaceRenderer();		

		Properties jsurf = new Properties();
		jsurf.load( this.getClass().getResourceAsStream( "tutorial_wuerfel.jsurf" ) );
    	FileFormat.load( jsurf, asr );

    	final int width = 1024;
    	final int height = 1024;

		class BackgroundRenderer extends SwingWorker< Void, Void >
		{
		   @Override
		   public Void doInBackground() {
		   		try {
		   			asr.draw( new int[ 3 * width * height ], width, height );
		   		}
		   		catch( RenderingInterruptedException rie ) 
		   		{
		   			System.out.println( "Rendering interrupted" );
		   		}
		    	return null; 
		   }
		}

		BackgroundRenderer br = new BackgroundRenderer();
		System.out.println( "Starting background rendering" );
		br.execute();
    	
    	long t = System.currentTimeMillis();
    	Thread.sleep( 100 );

		System.out.println( "Attempting to stop rendering" );
    	asr.stopDrawing();

    	br.get();
    	t = System.currentTimeMillis() - t;
    	System.out.println( "Render method finished after " + t + "ms" );

		Assert.assertTrue( "stopDrawing must interrupt and stop the rendering process", t < 200 );
	}
}