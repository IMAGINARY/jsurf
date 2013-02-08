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

public class LightProducts {
   
    private LightSource lightSource;
    private Material material;
    private Color3f diffuseProduct;
    private Color3f specularProduct;
    
    public LightProducts( LightSource lightSource, Material material )
    {
        this.lightSource = lightSource;
        this.material = material;
        
        this.diffuseProduct = new Color3f( material.getColor() );
        this.diffuseProduct.x *= lightSource.getColor().x;
        this.diffuseProduct.y *= lightSource.getColor().y;
        this.diffuseProduct.z *= lightSource.getColor().z;
        this.diffuseProduct.scale( material.getDiffuseIntensity() * lightSource.getIntensity() );

        this.specularProduct = new Color3f( lightSource.getColor() );
        this.specularProduct.scale( material.getSpecularIntensity() * lightSource.getIntensity() );
    }
    
    public LightSource getLightSource()
    {
        return this.lightSource;
    }
    
    public Material getMaterial()
    {
        return this.material;
    }
    
    public Color3f getDiffuseProduct()
    {
        return this.diffuseProduct;
    }
    
    public Color3f getSpecularProduct()
    {
        return this.specularProduct;
    }
}
