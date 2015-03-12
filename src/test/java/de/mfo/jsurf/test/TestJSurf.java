package de.mfo.jsurf.test;

import org.junit.*;

import de.mfo.jsurf.rendering.cpu.CPUAlgebraicSurfaceRenderer;
import de.mfo.jsurf.rendering.RenderingInterruptedException;
import de.mfo.jsurf.algebra.*;
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

	@Test
	public void XYZPolynomialAddShouldNotAffectMethodParameters()
	{
		XYZPolynomial X = new XYZPolynomial( XYZPolynomial.X );
		XYZPolynomial.X.add( XYZPolynomial.X );
		Assert.assertTrue( "XYZPolynomial.add changes values of its parameters", X.equals( XYZPolynomial.X ) );
	}
	
	@Test
	public void testDivisionInParser()
	{
	    PolynomialOperation div = new PolynomialDoubleDivision( new PolynomialVariable( PolynomialVariable.Var.x ), new DoubleValue( 2.0 ) );
	    PolynomialOperation mult = new PolynomialMultiplication( new PolynomialVariable( PolynomialVariable.Var.x ), new DoubleValue( 0.5 ) );
	    XYZPolynomial expanded_div = div.accept( new Expand(), ( Void ) null );
	    XYZPolynomial expanded_mult = mult.accept( new Expand(), ( Void ) null );	    
	    Assert.assertTrue( "x/2.0 and x*0.5 should both expand to 0.5x, but x/2.0 expands to " + expanded_div + " and x*0.5 expands to " + expanded_mult, expanded_div.equals( expanded_mult ) );
	}
}
