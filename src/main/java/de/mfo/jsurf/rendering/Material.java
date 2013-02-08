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

package de.mfo.jsurf.rendering;

import javax.vecmath.*;
import java.util.Properties;

import de.mfo.jsurf.util.BasicIO;

public class Material {

    private Color3f color;
    private float ambientIntensity;
    private float diffuseIntensity;
    private float specularIntensity;
    private float shininess;

    public Material() {
        this.color = new Color3f( 0.5f, 0.5f, 0.5f );
        this.ambientIntensity = 0.1f;
        this.diffuseIntensity = 0.23232f;
        this.specularIntensity = 0.9f;    
        this.shininess = 1.0f;
    }

    public Color3f getColor() {
        return color;
    }

    public void setColor(Color3f color)
            throws NullPointerException {
        if (color == null) {
            throw new NullPointerException();
        }
        this.color = color;
    }

    public float getAmbientIntensity() {
        return this.ambientIntensity;
    }

    public void setAmbientIntensity(float intensity) {
        this.ambientIntensity = intensity;
    }

    public float getDiffuseIntensity() {
        return this.diffuseIntensity;
    }

    public void setDiffuseIntensity(float intensity) {
        this.diffuseIntensity = intensity;
    }

    public float getSpecularIntensity() {
        return specularIntensity;
    }

    public void setSpecularIntensity(float specularIntensity) {
        this.specularIntensity = specularIntensity;
    }
    
    public float getShininess() {
        return this.shininess;
    }

    public void setShininess(float shininess) {
        this.shininess = shininess;
    }
    
    private static float lerp( float f1, float f2, float t ) { return f1 * ( 1.0f - t ) + t * f2; }
    
    public static Material lerp( Material m1, Material m2, float t )
    {
    	Material m = new Material();
    	
    	m.color.interpolate( m1.color, m2.color, t );
    	m.ambientIntensity = lerp( m1.ambientIntensity, m2.ambientIntensity, t );
    	m.diffuseIntensity = lerp( m1.diffuseIntensity, m2.diffuseIntensity, t );
    	m.specularIntensity = lerp( m1.specularIntensity, m2.specularIntensity, t );
    	m.shininess = lerp( m1.shininess, m2.shininess, t );
    	
    	return m;
    }

    public Properties saveProperties( Properties props, String prefix, String suffix )
    {
        props.setProperty( prefix + "color" + suffix, BasicIO.toString( color ) );
        props.setProperty( prefix + "ambient_intensity" + suffix, "" + ambientIntensity );
        props.setProperty( prefix + "diffuse_intensity" + suffix, "" + diffuseIntensity );
        props.setProperty( prefix + "specular_intensity" + suffix, "" + specularIntensity );
        props.setProperty( prefix + "shininess" + suffix, "" + shininess );
        return props;
    }

    public void loadProperties( Properties props, String prefix, String suffix )
    {
        String color_key = prefix + "color" + suffix;
        if( props.containsKey( color_key ) )
            color = BasicIO.fromColor3fString( props.getProperty( color_key ) );

        String ambient_intensity_key = prefix + "ambient_intensity" + suffix;
        if( props.containsKey( ambient_intensity_key ) )
            ambientIntensity = Float.parseFloat( props.getProperty( ambient_intensity_key ) );

        String diffuse_intensity_key = prefix + "diffuse_intensity" + suffix;
        if( props.containsKey( diffuse_intensity_key ) )
            diffuseIntensity = Float.parseFloat( props.getProperty( diffuse_intensity_key ) );

        String specular_intensity_key = prefix + "specular_intensity" + suffix;
        if( props.containsKey( specular_intensity_key ) )
            specularIntensity = Float.parseFloat( props.getProperty( specular_intensity_key ) );

        String shininess_key = prefix + "shininess" + suffix;
        if( props.containsKey( shininess_key ) )
            shininess = Float.parseFloat( props.getProperty( shininess_key ) );
    }
}
