package uk.ac.cam.gurdon.escop;

public class P3i {

	int x,y,z;
	
	public P3i(int x, int y, int z){
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	@Override
	public int hashCode(){
		return 7 * x + 13 * y + 19 * z;
	}
	
	@Override
	public boolean equals(Object other){
		if (other == null) return false;
		if (this == other) return true;
        if (getClass() != other.getClass()) return false;
        
        P3i op = (P3i) other;
        if(op.x==x&&op.y==y&&op.z==z) return true;
        
        return false;
	}
	
}
