package uk.ac.cam.gurdon.escop;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import ij.gui.Roi;


public class CorrelationCalculator implements Runnable{

	private Data3D A, B;
	private Axis axis;
	private int dx,dy,dz;
	private HashMap<P3i,P3i> shuffleCoords;
	private Roi roi;
	private double pearson, manders, li, thresholdA, thresholdB;
	private AtomicInteger count;
	public boolean stop;
	
	public enum Method{
		Pearson(),Manders(),Li();
	}
	
	public enum Axis{
		X(),Y(),Z();
	}

	public CorrelationCalculator(Data3D A, Data3D B, Roi roi, double thresholdA, double thresholdB, int offset, Axis axis, AtomicInteger count) throws IllegalArgumentException{	//offset for CCF
		if(A.size()!=B.size()){
			throw new IllegalArgumentException("Data3D are different sizes.");
		}
		this.A = A;
		this.B = B;
		this.roi = roi;
		this.thresholdA = thresholdA;
		this.thresholdB = thresholdB;
		this.axis = axis;
		this.count = count;
		switch(axis){
			case X:
				this.dx = offset;
				break;
			case Y:
				this.dy = offset;
				break;
			case Z:
				this.dz = offset;
				break;
			default:
				throw new IllegalArgumentException("Unknown Axis "+axis);
		}
		this.pearson = Double.NEGATIVE_INFINITY;
		this.manders = Double.NEGATIVE_INFINITY;
		this.li = Double.NEGATIVE_INFINITY;
	}
	
	public CorrelationCalculator(Data3D A, Data3D B, Roi roi, double thresholdA, double thresholdB, int cubeW, int cubeH, int cubeD) throws IllegalArgumentException{	//PCC with shuffle
		if(A.size()!=B.size()){
			throw new IllegalArgumentException("Data3D are different sizes.");
		}
		this.A = A;
		this.B = B;
		this.roi = roi;
		this.thresholdA = thresholdA;
		this.thresholdB = thresholdB;
		this.pearson = Double.NEGATIVE_INFINITY;
		this.manders = Double.NEGATIVE_INFINITY;
		this.li = Double.NEGATIVE_INFINITY;
		
		shuffleCoords = new HashMap<P3i,P3i>();
		
		//get one instance of each 3D cube coordinate in a random order
		ArrayList<P3i> random = new ArrayList<P3i>();
		for(int cz=0;cz<A.Z;cz+=cubeD){
			for(int cy=0;cy<A.H;cy+=cubeH){
				for(int cx=0;cx<A.W;cx+=cubeW){
					random.add(new P3i(cx,cy,cz));
				}
			}
		}
		Collections.shuffle(random);
		int ri = 0;
		
		for(int cz=0;cz<A.Z;cz+=cubeD){
			for(int cy=0;cy<A.H;cy+=cubeH){
				for(int cx=0;cx<A.W;cx+=cubeW){
					
					//coordinates of random cube
					P3i randomCube = random.get(ri++);

					//for coordinates in cube and random cube
					for(int iz=0;iz<cubeD;iz++){
						for(int iy=0;iy<cubeH;iy++){
							for(int ix=0;ix<cubeW;ix++){
								
								/*float v = get(cx+ix, cy+iy, cz+iz);				//get from original coordinates
								shuffle.set( randomCube.x+ix, randomCube.y+iy, randomCube.z+iz, v );			//assign to other random cube coordinates
								
								float s = get(randomCube.x+ix, randomCube.y+iy, randomCube.z+iz);				//get from swap coordinates
								shuffle.set( cx+ix, cy+iy, cz+iz, s );			//assign to original coordinates
								*/
								
								P3i orig = new P3i( cx+ix, cy+iy, cz+iz );
								P3i swap = new P3i( randomCube.x+ix, randomCube.y+iy, randomCube.z+iz );
								shuffleCoords.put( orig, swap );
								
							}
						}
					}
					
				}
			}
		}
		
	}
	
	@Override
	public void run() {
		double covar = 0.0;
		double varA = 0.0;
		double varB = 0.0;
		double AB = 0.0;
		double AA = 0.0;
		double BB = 0.0;
		double icq = 0.0;
		long n = 0;
		for(long i=0;i<A.size();i++){
			if(stop) return;
			
			if(roi!=null){
				int x = (int) (i%A.W);
				int y = (int) ((i-x)/A.W);
				if(!roi.contains(x,y)){
					continue;
				}
			}
			
			double valueA = Double.NaN;
			if(shuffleCoords!=null){	//if a map of coordinate pairs for Costes randomisation has been set
				int x = (int) (i % A.W);
				int y = (int) ((i/A.W) % A.H);
				int z = (int) (i / (A.W*A.H));
				P3i orig = new P3i(x,y,z);
				P3i target = shuffleCoords.get(orig);	//get random shuffled cube coordinate stored for this original coodinate
				valueA = A.get(target.x,target.y,target.z);	//get value from shuffled cube position
			}
			else{
				valueA = A.get(i, dx,dy,dz);	//get value with offset
			}
			
			if(valueA<thresholdA) continue;
			double valueB = B.get(i);
			if(valueB<thresholdB) continue;
			
			covar += (valueA-A.mean) * (valueB-B.mean);
			varA += (valueA-A.mean) * (valueA-A.mean);
			varB += (valueB-B.mean) * (valueB-B.mean);
			
			AB += valueA * valueB;
			AA += valueA * valueA;
			BB += valueB * valueB;
			
			if( (valueA-A.mean) * (valueB-B.mean) > 0 ){
				icq++;
			}
			n++;
		}
		pearson = covar/Math.sqrt(varA*varB);
		manders = AB/Math.sqrt(AA*BB);
		li = icq/(float)n - 0.5;
		
		if(count!=null){	//no count for non-shifting calculator
			count.getAndIncrement();
		}

		A = null;
		B = null;
		
	}
	
	public int getOffset(){
		if(axis==Axis.X) return dx;
		else if(axis==Axis.Y) return dy;
		else if(axis==Axis.Z) return dz;
		else return 0;
	}
	
	public Axis getAxis(){
		return axis;
	}
	
	public double getCorrelationCoefficient(Method method) throws Exception{
		if(pearson<-1) throw new Exception("Calculation has not been run.");
		switch(method){
			case Pearson: return pearson;
			case Manders: return manders;
			case Li: return li;
			default: throw new IllegalArgumentException("Unknown correlation coefficient method: "+method);
		}
	}
	
}
