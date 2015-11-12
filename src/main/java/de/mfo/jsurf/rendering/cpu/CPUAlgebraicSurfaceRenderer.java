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

import de.mfo.jsurf.algebra.*;
import de.mfo.jsurf.debug.*;
import de.mfo.jsurf.rendering.*;
import de.mfo.jsurf.rendering.cpu.clipping.*;

import javax.vecmath.*;

import java.util.concurrent.*;
import java.util.*;

public class CPUAlgebraicSurfaceRenderer extends AlgebraicSurfaceRenderer
{
    ExecutorService threadPoolExecutor;
    List< FutureTask< Boolean > > renderingTasks; 
    
    synchronized DrawcallStaticData collectDrawCallStaticData( int[] colorBuffer, int width, int height )
    {
        DrawcallStaticData dcsd = new DrawcallStaticData();
        
        dcsd.colorBuffer = colorBuffer;
        dcsd.width = width;
        dcsd.height = height;
        
        dcsd.coefficientCalculator = new PolynomialExpansionCoefficientCalculator( getSurfaceExpression() );
        if( this.getSurfaceTotalDegree() < 2 )
            dcsd.realRootFinder = new ClosedFormRootFinder();
        else
//        dcsd.realRootFinder = new DChainRootFinder();
//        dcsd.realRootFinder = new SturmChainRootFinder();
        dcsd.realRootFinder = new DescartesRootFinder( false );
        //dcsd.realRootFinder = new EVALRootFinder( false );
        //dcsd.realRootFinder = new ClosedFormRootFinder();
//        dcsd.realRootFinder = new GPUSuitableDescartesRootFinder2( false );
        //dcsd.realRootFinder = new BernsteinDescartesRootFinder( false );

        dcsd.frontAmbientColor = new Color3f( getFrontMaterial().getColor() );
        dcsd.frontAmbientColor.scale( getFrontMaterial().getAmbientIntensity() );

        dcsd.backAmbientColor = new Color3f( getBackMaterial().getColor() );
        dcsd.backAmbientColor.scale( getFrontMaterial().getAmbientIntensity() );

        int numOfLightSources = 0;
        for( int i = 0; i < MAX_LIGHTS; i++ )
            if( getLightSource( i ) != null && getLightSource( i ).getStatus() == LightSource.Status.ON )
                numOfLightSources++;
        dcsd.lightSources = new LightSource[ numOfLightSources ];
        dcsd.frontLightProducts = new LightProducts[ numOfLightSources ];
        dcsd.backLightProducts = new LightProducts[ numOfLightSources ];
        int lightSourceIndex = 0;
        for( int i = 0; i < MAX_LIGHTS; i++ )
        {
            LightSource lightSource = getLightSource( i );
            if( lightSource != null && lightSource.getStatus() == LightSource.Status.ON )
            {
                dcsd.lightSources[lightSourceIndex] = lightSource;
                dcsd.frontLightProducts[lightSourceIndex] = new LightProducts( lightSource, getFrontMaterial() );
                dcsd.backLightProducts[lightSourceIndex] = new LightProducts( lightSource, getBackMaterial() );

                lightSourceIndex++;
            }
        }
        
        dcsd.backgroundColor = getBackgroundColor();
        
        dcsd.antiAliasingPattern = getAntiAliasingPattern();
        dcsd.antiAliasingThreshold = aaThreshold;
                
        dcsd.rayCreator = RayCreator.createRayCreator( getTransform(), getSurfaceTransform(), getCamera(), width, height );
        dcsd.rayClipper = new ClipToSphere();
        //dcsd.rayClipper = new ClipToTorus( 0.5, 0.5 );
        //dcsd.rayClipper = new ClipBlowUpSurface( 1.0, 1.0 );
        //dcsd.someA = new PolynomialExpansionRowSubstitutor( getSurfaceExpression(), dcsd.rayCreator.getXForSomeA(), dcsd.rayCreator.getYForSomeA(), dcsd.rayCreator.getZForSomeA() );
        dcsd.surfaceRowSubstitutor = new TransformedPolynomialRowSubstitutor( getSurfaceExpression(), dcsd.rayCreator.getXForSomeA(), dcsd.rayCreator.getYForSomeA(), dcsd.rayCreator.getZForSomeA() );
        dcsd.gradientRowSubstitutor = new TransformedPolynomialRowSubstitutorForGradient( getGradientXExpression(), getGradientYExpression(), getGradientZExpression(), dcsd.rayCreator.getXForSomeA(), dcsd.rayCreator.getYForSomeA(), dcsd.rayCreator.getZForSomeA() );
        //dcsd.gradientRowSubstitutor = new FastRowSubstitutorForGradient( getGradientXExpression(), getGradientYExpression(), getGradientZExpression(), dcsd.rayCreator );

        //System.out.println( getSurfaceExpression().accept( new ToStringVisitor(), null ) );

        // fill img with bg color
    	int bg = dcsd.backgroundColor.get().getRGB();	
    	java.util.Arrays.fill( dcsd.colorBuffer, bg );
        
        return dcsd;
    }

    public CPUAlgebraicSurfaceRenderer()
    {
        super();

        this.setAntiAliasingMode( AntiAliasingMode.ADAPTIVE_SUPERSAMPLING );
        this.setAntiAliasingPattern( AntiAliasingPattern.OG_4x4 );
        
    	final ThreadGroup tg = new ThreadGroup( "Group of rendering threads of " + this );
        class PriorityThreadFactory implements ThreadFactory
        {
            public Thread newThread(Runnable r) {
                Thread t = new Thread( tg, r );
                t.setDaemon( true );
                t.setPriority( Thread.MIN_PRIORITY );
                return t;
            }
        }   
        
        this.threadPoolExecutor = Executors.newFixedThreadPool( 2 * Runtime.getRuntime().availableProcessors(), new PriorityThreadFactory() );     
        this.renderingTasks = new LinkedList< FutureTask< Boolean > >();
    }

    public enum AntiAliasingMode
    {
        SUPERSAMPLING,
        ADAPTIVE_SUPERSAMPLING;
    }
    private AntiAliasingMode aaMode;
    private float aaThreshold;
    private AntiAliasingPattern aaPattern;

    public void setAntiAliasingMode( AntiAliasingMode mode )
    {
        this.aaMode = mode;
        if( mode == AntiAliasingMode.SUPERSAMPLING )
            this.aaThreshold = 0.0f;
        else
            this.aaThreshold = 0.3f;
    }

    public AntiAliasingMode getAntiAliasingMode()
    {
        return this.aaMode;
    }

    public void setAntiAliasingPattern( AntiAliasingPattern pattern )
    {
        this.aaPattern = pattern;
    }

    public AntiAliasingPattern getAntiAliasingPattern()
    {
        return this.aaPattern;
    }

    public synchronized void draw( int[] colorBuffer, int width, int height )
    {
    	if( width == 0 || height == 0 )
    		return;
    	
    	DrawcallStaticData dcsd = collectDrawCallStaticData( colorBuffer, width, height );
    	
        int xStep = width / Math.min( width, Math.max( 2, Runtime.getRuntime().availableProcessors() ) );
        int yStep = height / Math.min( height, 3 );//Math.max( 2, Runtime.getRuntime().availableProcessors() ) );

        boolean success = true;
        
        LinkedList< FutureTask< Boolean > > tasks = new LinkedList< FutureTask< Boolean > >();
    	for( int x = 0; x < width; x += xStep )
            for( int y = 0; y < height; y += yStep )
            	tasks.add( new FutureTask< Boolean >( new RenderingTask( dcsd, x, y, Math.min( x + xStep, width - 1 ), Math.min( y + yStep, height - 1 ) ) ) );
        
        renderingTasks = tasks;
        
		try
        {
            for( FutureTask< Boolean > task : tasks )
            	threadPoolExecutor.execute( task );
        	for( FutureTask< Boolean > task : tasks )
            	success = success && task.get();
        }
        catch( ExecutionException ie )
        {
        	success = false;   	
        }
        catch( InterruptedException ie )
        {
        	success = false;
        }
        catch( RejectedExecutionException ree )
        {
        	success = false;
        }
        catch( CancellationException ree )
        {
        	success = false;
        }
        finally
        {
        	if( !success || Thread.interrupted() )
	            	throw new RenderingInterruptedException( "Rendering interrupted" );
        }
    }

    public void stopDrawing()
    {
    	for( FutureTask< Boolean > f : renderingTasks )
    		f.cancel( true );
    }
}
