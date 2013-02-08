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

package de.mfo.jsurf.algebra;

public interface RealRootFinder
{
    /**
     * Find all real roots of p.
     * @param p
     * @return
     */
    public double[] findAllRoots( UnivariatePolynomial p );
    
    /**
     * Find all real roots of p within lowerBound and upperBound (bounds may or may not be included).
     * @param p
     * @param lowerBound
     * @param upperBound
     * @return
     */
    public double[] findAllRootsIn( UnivariatePolynomial p, double lowerBound, double upperBound );
    
    /**
     * Find the smallest real root of p within lowerBound and upperBound (bounds may or may not be included).
     * If no real root exists in this interval, Double.NaN ist returned.
     * @param p
     * @param lowerBound
     * @param upperBound
     * @return
     */
    public double findFirstRootIn( UnivariatePolynomial p, double lowerBound, double upperBound );
}
