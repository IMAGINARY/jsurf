package de.mfo.jsurf.rendering.cpu;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Color3f;

/**
 * The buffers returned from this class will not have the exact size requested, but the
 * size of the minimum power of two higher (or equal) to it.<br/>
 * For example, if asked for a buffer for 457 colors, it will return one of size 512.<br/>
 * <br/>
 * This will waste memory, but allows to reuse the buffers for different parts, and also 
 * when the rendering size changes. It also keeps them nicely aligned in memory, which
 * could produce more speed improvements.
 * 
 * @author Sebastian
 *
 */
public class ColorBufferPool {
    
	// Should be an array of lists, but Java does not allow arrays of generic types
    private List<List<Color3f[]>> bufferPool;
    private boolean dontPool = false;

    // Usage statistics
    private int createdBuffers = 0;
    private int requestedBuffers = 0;
    private int totalBufferSize = 0;
    
    // the biggest buffer it can hold is of size 2^32 == 2^16 x 2^16, an area of 65536 x 65536
    private static final int MAX_POWER_OF_TWO = 32;

    public static ColorBufferPool createDummyPool() {
    	ColorBufferPool pool = new ColorBufferPool();
    	pool.dontPool = true;
    	return pool;
    }
    
    public ColorBufferPool() {
    	bufferPool = new ArrayList<List<Color3f[]>>(MAX_POWER_OF_TWO);
    	for (int i = 0 ; i < MAX_POWER_OF_TWO ; i++) {
    		bufferPool.add(new ArrayList<Color3f[]>());
    	}
    }

    public Color3f[] getBuffer(int size) {
    	size = powOf2Roundup(size);
    	int index = highestOneBit(size);
    	requestedBuffers++;

    	List<Color3f[]> bucket = bufferPool.get(index);
    	synchronized (bucket) {
        	if (dontPool || bucket.isEmpty()) {
            	createdBuffers++;
            	totalBufferSize += size;
        		return new Color3f[size];
        	} else
        		return bucket.remove(bucket.size() - 1);
    	}
    }
    
    /** Index of the highest bit set in the binary representation of an integer */
    public static int highestOneBit(int x) {
    	int index = 0;
    	if ((x & 0xFFFF0000) != 0) {
    		index += 16;
    		x >>= 16;
    	}
    	if ((x & 0xFF00) != 0) {
    		index += 8;
    		x >>= 8;
    	}
    	if ((x & 0xF0) != 0) {
    		index += 4;
    		x >>= 4;
    	}
    	if ((x & 0x0C) != 0) {
    		index += 2;
    		x >>= 2;
    	}
    	if ((x & 2) != 0)
    		index++;

    	return index;
    }
    
    // https://stackoverflow.com/questions/364985/algorithm-for-finding-the-smallest-power-of-two-thats-greater-or-equal-to-a-giv
    /** Minimum power of two bigger or equal to value */
    private int powOf2Roundup(int x) {
	    if (x < 0)
	        return 0;

	    --x;
	    x |= x >> 1;
	    x |= x >> 2;
	    x |= x >> 4;
	    x |= x >> 8;
	    x |= x >> 16;
	    return x+1;
	}

	public void releaseBuffer(Color3f[] buffer) {
    	int size = buffer.length;
    	int index = highestOneBit(size);
    	List<Color3f[]> bucket = bufferPool.get(index);
    	synchronized (bucket) {
        	bucket.add(buffer);
    	}
    }
	
	public String getPoolStatistics() {
		return "Requests / created: " + requestedBuffers + "/" + createdBuffers + ". Total size: " + totalBufferSize;		
	}
}
