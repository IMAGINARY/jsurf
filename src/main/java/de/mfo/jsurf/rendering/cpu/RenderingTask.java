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

import java.util.concurrent.Callable;
import javax.vecmath.Color3f;

import de.mfo.jsurf.rendering.RenderingInterruptedException;

public class RenderingTask implements Callable<Boolean>
{
    static ColorBufferPool bufferPool = new ColorBufferPool();
	
    // initialized by the constructor
    private final DrawcallStaticData dcsd;
    private final PixelStep step;
    private final ColumnSubstitutorPairProvider cspProvider;
    private final PolynomialTracer polyTracer;

    public RenderingTask( DrawcallStaticData dcsd, int xStart, int yStart, int xEnd, int yEnd )
    {
        this.dcsd = dcsd;
		this.step = new PixelStep(dcsd, xStart, yStart, xEnd - xStart + 2, yEnd - yStart + 2);
        this.cspProvider = new ColumnSubstitutorPairProvider(dcsd);
        this.polyTracer = new PolynomialTracer(dcsd);
    }

    public Boolean call() {
		Color3f[] colorBuffer = null;
        try {
        	PixelRenderStrategy pixelRenderer; 
        	if (useAntiAliasing()) {
        		colorBuffer = bufferPool.getBuffer(step.width * step.height);
        		pixelRenderer = new AntiAliasedPixelRenderer(dcsd, colorBuffer, polyTracer, cspProvider);
        	} else {
        		pixelRenderer = new BasicPixelRenderer(dcsd, polyTracer);
        	}
        	renderImage(pixelRenderer);
            return true;
        } catch( RenderingInterruptedException rie ) { // rendering interrupted .. that's ok
        } catch( Throwable t ) {
            t.printStackTrace();
        } finally {
        	if (colorBuffer != null)
        		bufferPool.releaseBuffer(colorBuffer);
        }
        return false;
    }

    private boolean useAntiAliasing() {
    	return dcsd.antiAliasingPattern != AntiAliasingPattern.OG_1x1;
    }

	private void renderImage(PixelRenderStrategy pixelRenderer) {
		for( int y = 0; y < step.height; ++y )
		{
			ColumnSubstitutorPair csp = cspProvider.get(step.v);

		    for( int x = 0; x < step.width; ++x )
		    {
		        if( Thread.currentThread().isInterrupted() )
		            throw new RenderingInterruptedException();
		        
		        pixelRenderer.renderPixel(x, y, step, csp);
		        step.stepU();
		    }
		    step.stepV();
		}
	}
}
