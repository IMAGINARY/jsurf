package de.mfo.jsurf.rendering.cpu;

import javax.vecmath.Color3f;
import javax.vecmath.Vector3d;

import de.mfo.jsurf.algebra.ColumnSubstitutor;
import de.mfo.jsurf.algebra.ColumnSubstitutorForGradient;
import de.mfo.jsurf.algebra.UnivariatePolynomialVector3d;
import de.mfo.jsurf.rendering.cpu.RenderingTask.PixelStep;

public abstract class PixelRenderStrategy {
	private final PolynomialTracer polyTracer;
	private final RayCreator rayCreator;
	private final Shader frontShader;
	private final Shader backShader;
	private final Color3f backgroundColor;

	protected final int[] colorBuffer;

	public PixelRenderStrategy(DrawcallStaticData dcsd, PolynomialTracer polyTracer) {
		this.polyTracer = polyTracer;
        this.frontShader = new Shader(dcsd.frontAmbientColor, dcsd.lightSources, dcsd.frontLightProducts);
        this.backShader = new Shader(dcsd.backAmbientColor, dcsd.lightSources, dcsd.backLightProducts);
		this.backgroundColor = dcsd.backgroundColor;
		this.rayCreator = dcsd.rayCreator;
		this.colorBuffer = dcsd.colorBuffer;
	}
	
	public abstract void renderPixel(int x, int y, PixelStep step, ColumnSubstitutorPair csp);
	
    protected Color3f tracePolynomial( ColumnSubstitutor scs, ColumnSubstitutorForGradient gcs, double u, double v )
    {
    	double hit = polyTracer.findClosestHit(scs, gcs, u, v);

    	if (Double.isNaN(hit))
            return backgroundColor;

        UnivariatePolynomialVector3d gradientPolys = gcs.setU( u );
	    Vector3d n_surfaceSpace = gradientPolys.setT( hit );
	    Vector3d n_cameraSpace = rayCreator.surfaceSpaceNormalToCameraSpaceNormal( n_surfaceSpace );
	
        Ray ray = rayCreator.createCameraSpaceRay( u, v );
	    return shade( ray, hit, n_cameraSpace );
    }

    /**
     * Calculates the shading in camera space
     */
    protected Color3f shade( Ray ray, double hit, Vector3d cameraSpaceNormal )
    {
        // normalize only if point is not singular
        float nLength = (float) cameraSpaceNormal.length();
        if( nLength != 0.0f )
            cameraSpaceNormal.scale( 1.0f / nLength );

        Vector3d view = new Vector3d(-ray.d.x, -ray.d.y, -ray.d.z);
        // TODO: not normalizing the view does not seem to affect the rendered result, maybe it can be avoided
        view.normalize();

        Shader shader = frontShader;
        if( cameraSpaceNormal.dot( view ) <= 0.0f ) {
        	shader = backShader;
            cameraSpaceNormal.negate();
        }

        return shader.shade(ray.at(hit), view, cameraSpaceNormal);
    }
}