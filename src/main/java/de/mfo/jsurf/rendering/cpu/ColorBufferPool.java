package de.mfo.jsurf.rendering.cpu;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Color3f;

public class ColorBufferPool {
    
    private List<List<Color3f[]>> bufferPool;
    private int createdBuffers = 0;
    private int getBufferCalls = 0;
    private int totalBufferSize = 0;

    public ColorBufferPool() {
    	bufferPool = new ArrayList<List<Color3f[]>>(32);
    	for (int i = 0 ; i < 32 ; i++) {
    		bufferPool.add(new ArrayList<Color3f[]>());
    	}
    }

    public synchronized Color3f[] getBuffer(int size) {
    	size = powOf2Roundup(size);
    	int index = highestOneBit(size);
    	getBufferCalls++;

    	List<Color3f[]> bucket = bufferPool.get(index);
    	if (bucket.isEmpty()) {
        	createdBuffers++;
        	totalBufferSize += size;
        	System.out.println("Created color buffers size " + size + ". Call / created: " + getBufferCalls + "/" + createdBuffers + ". Total size: " + totalBufferSize);
    		return new Color3f[size];
    	} else
    		return bucket.remove(0);
    }
    
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

	public synchronized void releaseBuffer(Color3f[] buffer) {
    	int size = buffer.length;
    	int index = highestOneBit(size);
//    	clearBuffer(buffer);
    	bufferPool.get(index).add(buffer);
    }
	
	private void clearBuffer(Color3f[] buffer) {
		for (int i = 0 ; i < buffer.length ; i++)
			buffer[i].set(0f, 0f, 0f);
	}
	
}
