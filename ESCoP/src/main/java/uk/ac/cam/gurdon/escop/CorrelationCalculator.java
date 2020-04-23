package uk.ac.cam.gurdon.escop;

import java.util.concurrent.atomic.AtomicInteger;

import ij.gui.Roi;

public class CorrelationCalculator implements Runnable{

	private Data3D A, B;
	private Axis axis;
	private int dx,dy,dz;
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

	public CorrelationCalculator(Data3D A, Data3D B, Roi roi, double thresholdA, double thresholdB) throws IllegalArgumentException{	//PCC without offset
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
			
			double valueA = A.get(i, dx,dy,dz);
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
