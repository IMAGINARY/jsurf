package de.mfo.jsurf.rendering.cpu;

import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import de.mfo.jsurf.rendering.LightProducts;
import de.mfo.jsurf.rendering.LightSource;

public class Shader {

    private final Color3f ambientColor;
	private final LightSource[] lightSources;
	private final LightProducts[] lightProducts;

	public Shader(Color3f ambientColor, LightSource[] lightSources, LightProducts[] lightProducts) {
		this.ambientColor = ambientColor;
		this.lightSources = lightSources;
		this.lightProducts = lightProducts;
    }

    /**
     * Shades a point with the same algorithm used by the
     * {@link <a href="http://surf.sourceforge.net">surf raytracer</a>}.
     * @param hitPoint Intersection point.
     * @param v View vector (from intersection point to eye).
     * @param n Surface normal.
     * @param material Surface material.
     * @return
     */
    protected Color3f shade( Point3d hitPoint, Vector3d v, Vector3d n)
    {
        Vector3d l = new Vector3d();
        Vector3d h = new Vector3d();

        Color3f color = new Color3f( ambientColor );

        for( int i = 0; i < lightSources.length; i++ )
        {
            LightSource lightSource = lightSources[i];

            l.sub( lightSource.getPosition(), hitPoint );
            l.normalize();

            float lambertTerm = (float) n.dot( l );
            if( lambertTerm > 0.0f )
            {
                // compute diffuse color component
                color.scaleAdd( lambertTerm, lightProducts[i].getDiffuseProduct(), color );

                // compute specular color component
                h.add( l, v );
                h.normalize();

                color.scaleAdd( ( float ) Math.pow( Math.max( 0.0f, n.dot( h ) ), lightProducts[i].getMaterial().getShininess() ), lightProducts[i].getSpecularProduct(), color );
            }
        }

        color.clampMax( 1.0f );

        return color;
    }
}
