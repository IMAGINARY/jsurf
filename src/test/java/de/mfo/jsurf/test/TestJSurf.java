package de.mfo.jsurf.test;

import org.junit.*;

import de.mfo.jsurf.rendering.cpu.CPUAlgebraicSurfaceRenderer;
import de.mfo.jsurf.rendering.RenderingInterruptedException;
import de.mfo.jsurf.algebra.*;
import de.mfo.jsurf.parser.*;
import de.mfo.jsurf.util.FileFormat;
import java.util.Properties;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

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
			public boolean interrupted = false;

		   @Override
		   public Void doInBackground() {
		   		try {
		   			asr.draw( new int[ 3 * width * height ], width, height );
		   		}
		   		catch( RenderingInterruptedException rie )
		   		{
		   			System.out.println( "Rendering interrupted" );
					interrupted = true;
		   		}
		    	return null;
		   }
		}

		BackgroundRenderer br = new BackgroundRenderer();
		System.out.println( "Starting background rendering" );
		br.execute();

    	long t = System.currentTimeMillis();
		System.out.println( "Attempting to stop rendering" );
		for (int i = 0; i < 100; ) {
			try
			{
				asr.stopDrawing();
				br.get(100, TimeUnit.MILLISECONDS);
				break;
			}
			catch (InterruptedException e) {}
			catch (ExecutionException e) {}
			catch (TimeoutException e) {}
		}

    	t = System.currentTimeMillis() - t;
    	System.out.println( "Render method finished after " + t + "ms" );

		Assert.assertTrue( "stopDrawing must interrupt and stop the rendering process", br.interrupted );
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

    @Test
    public void testIfParenthesesAreCorrectlyRecognized() throws Exception
    {
        String input = "(x)";
        PolynomialOperation op = AlgebraicExpressionParser.parse( input );
        Assert.assertNotNull( "op must not be null", op );
        Assert.assertTrue(
            "Tree node should have parentheses",
            op.hasParentheses()
        );
    }

    @Test
    public void testParserInputAndToStringVisitorOutputShouldMatch() throws Exception
    {
        String[] inputs = {
            "x-(x)",
            "sin(1)",
            "x+y+z+x*(y+x)",
            "a*0.99*(64*(0.5*z)^7-112*(0.5*z)^5+56*(0.5*z)^3-7*(0.5*z)-1)+(0.7818314825-0.3765101982*y-0.7818314825*x)*(0.7818314824-0.8460107361*y-0.1930964297*x)*(0.7818314825-0.6784479340*y+0.5410441731*x)*(0.7818314825+0.8677674789*x)*(0.7818314824+0.6784479339*y+0.541044172*x)*(0.7818314824+0.8460107358*y-0.193096429*x)*(0.7818314821+0.3765101990*y-0.781831483*x)"
        };

        for( String input : inputs )
        {
            String output = AlgebraicExpressionParser.parse( input ).accept( new ToStringVisitor( true ), ( Void ) null );
            Assert.assertEquals(
                "Output of ToStringVisitor should be identical to input",
                input,
                output
            );
        }
    }
}
