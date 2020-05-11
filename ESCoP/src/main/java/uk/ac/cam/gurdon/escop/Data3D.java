package uk.ac.cam.gurdon.escop;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import ij.ImagePlus;
import ij.ImageStack;

public class Data3D {

	private float[][] voxels;
	int W,H,Z;
	int bitDepth;
	
	double min,max,mean;
	
	public Data3D(int W, int H, int Z, float[][] voxels, double mean){	//make directly
		this.W = W;
		this.H = H;
		this.Z = Z;
		this.voxels = voxels;
		this.mean = mean;
	}
	
	public Data3D(int W, int H, int Z){	//make empty
		this.W = W;
		this.H = H;
		this.Z = Z;
		this.voxels = new float[Z][W*H];
	}
	
	public Data3D(ImagePlus imp, int channel, int frame) throws Exception{	//make from ImagePlus
		this.W = imp.getWidth();
		this.H = imp.getHeight();
		this.Z = imp.getNSlices();

		ImageStack stack = imp.getImageStack();
		bitDepth = stack.getBitDepth();
		Object[] slicePixels = stack.getImageArray();
		voxels = new float[Z][W*H];
		double sum = 0.0;
		min = Double.POSITIVE_INFINITY;
		max = Double.NEGATIVE_INFINITY;
		for(int z=0;z<Z;z++){
			int index = imp.getStackIndex(channel, z+1, frame)-1;
			switch(bitDepth){
				case 8:
					byte[] byteCast = (byte[]) slicePixels[index];
					for(int i=0;i<W*H;i++){
						voxels[z][i] = byteCast[i]&0xff;
						sum += voxels[z][i];
					}
					break;
				case 16:
					short[] shortCast = (short[]) slicePixels[index];
					for(int i=0;i<W*H;i++){
						voxels[z][i] = shortCast[i]&0xffff;
						sum += voxels[z][i];
					}
					break;
				case 32:
					float[] floatCast = (float[]) slicePixels[index];
					for(int i=0;i<W*H;i++){
						voxels[z][i] = floatCast[i];
						sum += voxels[z][i];
					}
					break;
				default:
					throw new Exception("Unsupported bit-depth: "+bitDepth);
			}
		}
		this.mean = sum/(double)(W*H*Z);
		
	}
	
	public float get(int x, int y, int z){
		//if(x<0||x>W-1||y<0||y>H-1||z<0||z>Z-1){	//get zero if out of range
		//	return 0;
		//}
		
		x = Math.max(0, Math.min(W-1, x));	//extend edge values
		y = Math.max(0, Math.min(H-1, y));
		z = Math.max(0, Math.min(Z-1, z));

		return voxels[z][y*W+x];
	}
	
	public float get(int x, int y, int z, int xd, int yd, int zd){
		return get(x+xd, y+yd, z+zd);
	}
	
	public float get(long index){
		int x = (int) (index % W);
		int y = (int) ((index/W) % H);
		int z = (int) (index / (W*H));
		return get(x,y,z);
	}
	
	public float get(long index, int xd, int yd, int zd){
		int x = (int) (index % W);
		int y = (int) ((index/W) % H);
		int z = (int) (index / (W*H));
		return get(x+xd, y+yd, z+zd);
	}
	
	public void set(int x, int y, int z, float value){
		if(x<0||x>W-1||y<0||y>H-1||z<0||z>Z-1){	//ignore assignment out of range
			return;
		}
		voxels[z][y*W+x] = value;
	}
	
	public long size(){
		return W*H*Z;
	}

	public Data3D duplicate(){
		float[][] dupVoxels = new float[voxels.length][];	//make deep copy of voxels
		for(int z=0;z<Z;z++){
			dupVoxels[z] = Arrays.copyOf(voxels[z], voxels[z].length);
		}
		Data3D dup = new Data3D(W,H,Z, dupVoxels, mean);
		return dup;
	}
	
	public void display(String title){
		ImageStack stack = new ImageStack(W,H,Z);
		for(int z=0;z<Z;z++){
			//ShortProcessor ip = new ShortProcessor(W,H);
			stack.setPixels( voxels[z], z+1 );
		}
		new ImagePlus(title, stack).show();
	}
	
	public Data3D getShuffled(int cubeW, int cubeH, int cubeZ){
		Data3D shuffle = new Data3D(W, H, Z);
		
		//get one instance of each 3D cube coordinate in a random order
		ArrayList<P3i> random = new ArrayList<P3i>();
		for(int cz=0;cz<Z;cz+=cubeZ){
			for(int cy=0;cy<H;cy+=cubeH){
				for(int cx=0;cx<W;cx+=cubeW){
					random.add(new P3i(cx,cy,cz));
				}
			}
		}
		Collections.shuffle(random);
		int ri = 0;
		
		for(int cz=0;cz<Z;cz+=cubeZ){
			for(int cy=0;cy<H;cy+=cubeH){
				for(int cx=0;cx<W;cx+=cubeW){
					
					//coordinates of random cube
					P3i randomCube = random.get(ri++);

					//for coordinates in cube and random cube
					for(int iz=0;iz<cubeZ;iz++){
						for(int iy=0;iy<cubeH;iy++){
							for(int ix=0;ix<cubeW;ix++){
								
								float v = get(cx+ix, cy+iy, cz+iz);				//get from original coordinates
								shuffle.set( randomCube.x+ix, randomCube.y+iy, randomCube.z+iz, v );			//assign to other random cube coordinates
								
								float s = get(randomCube.x+ix, randomCube.y+iy, randomCube.z+iz);				//get from swap coordinates
								shuffle.set( cx+ix, cy+iy, cz+iz, s );			//assign to original coordinates
								
							}
						}
					}
					
				}
			}
		}
		
		return shuffle;
	}
	
}
