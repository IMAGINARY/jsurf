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

package de.mfo.jsurf.util;

import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;

import de.mfo.jsurf.rendering.AlgebraicSurfaceRenderer;
import de.mfo.jsurf.rendering.LightSource;

public class FileFormat {

	public static Properties addDefaults( Properties jsurf, Properties defaults )
	{
		Properties result = new Properties( jsurf );
		for( Map.Entry me : defaults.entrySet() )
		{
			if( !jsurf.containsKey( me.getKey() ) )				
				jsurf.setProperty( ( String ) me.getKey(), ( String ) me.getValue() );
		}
		return jsurf;	
	}
	
	public static void load( Properties jsurf, AlgebraicSurfaceRenderer asr )
		throws Exception
	{			
		asr.setSurfaceFamily( jsurf.getProperty( "surface_equation" ) );

        Set< Map.Entry< Object, Object > > entries = jsurf.entrySet();
        String parameter_key_prefix = "surface_parameter_";
        for( Map.Entry< Object, Object > entry : entries )
        {
            String name = (String) entry.getKey();
            if( name.startsWith( parameter_key_prefix ) )
            {
                String parameterName = name.substring( parameter_key_prefix.length() );
                asr.setParameterValue( parameterName, Float.parseFloat( ( String ) entry.getValue() ) );
            }
        }

        asr.getCamera().loadProperties( jsurf, "camera_", "" );
        asr.getFrontMaterial().loadProperties(jsurf, "front_material_", "");
        asr.getBackMaterial().loadProperties(jsurf, "back_material_", "");
        for( int i = 0; i < AlgebraicSurfaceRenderer.MAX_LIGHTS; i++ )
        {
            asr.getLightSource( i ).setStatus(LightSource.Status.OFF);
            asr.getLightSource( i ).loadProperties( jsurf, "light_", "_" + i );
        }
        asr.setBackgroundColor( BasicIO.fromColor3fString( jsurf.getProperty( "background_color" ) ) );
        
        Matrix4d transform;
        try
        {
        	transform = BasicIO.fromMatrix4dString( jsurf.getProperty( "transform_matrix" ) );
        }
        catch( Exception e )
        {
        	// if the property isn't there initialize with identity matrix
        	transform = new Matrix4d();
        	transform.setIdentity();
        }
        try
        {
        	Matrix4d rotation_matrix = BasicIO.fromMatrix4dString( jsurf.getProperty( "rotation_matrix" ) );
        	transform.mul( rotation_matrix );
        }
        catch( Exception e )
        {
        }
        asr.setTransform( transform );
        
        Matrix4d surface_transform;
        try
        {
        	surface_transform = BasicIO.fromMatrix4dString( jsurf.getProperty( "surface_transform_matrix" ) );
        }
        catch( Exception e )
        {
        	// if the property isn't there initialize with identity matrix
        	surface_transform = new Matrix4d();
        	surface_transform.setIdentity();
        }
        try
        {
        	Matrix4d scale_matrix = new Matrix4d();
        	scale_matrix.setIdentity();
        	scale_matrix.setScale( Math.pow( 10, Double.parseDouble( jsurf.getProperty( "scale_factor" ) ) ) );
        	surface_transform.mul( scale_matrix );
        }
        catch( Exception e )
        {
        }
        
        asr.setSurfaceTransform( surface_transform );
	}
	
	public static Properties save( AlgebraicSurfaceRenderer asr )
	{
		Properties jsurf = new Properties();
        jsurf.setProperty( "surface_equation", asr.getSurfaceFamilyString() );

        Set< String > paramNames = asr.getAllParameterNames();
        for( String paramName : paramNames )
        {
            try
            {
                jsurf.setProperty( "surface_parameter_" + paramName, "" + asr.getParameterValue( paramName ) );
            }
            catch( Exception e ) {}
        }

        asr.getCamera().saveProperties( jsurf, "camera_", "" );
        asr.getFrontMaterial().saveProperties(jsurf, "front_material_", "");
        asr.getBackMaterial().saveProperties(jsurf, "back_material_", "");
        for( int i = 0; i < AlgebraicSurfaceRenderer.MAX_LIGHTS; i++ )
            asr.getLightSource( i ).saveProperties( jsurf, "light_", "_" + i );
        jsurf.setProperty( "background_color", BasicIO.toString( asr.getBackgroundColor() ) );

        jsurf.setProperty( "transform_matrix", BasicIO.toString( asr.getTransform() ) );
        jsurf.setProperty( "surface_transform_matrix", BasicIO.toString( asr.getSurfaceTransform() ) );
        
        return jsurf;
	}
}
