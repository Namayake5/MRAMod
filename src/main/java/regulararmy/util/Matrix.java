package regulararmy.util;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class Matrix {
	/**[row][column]*/
	public float[][] value;
	
	
	public Matrix(int row,int column){
		this.value=new float[column][row];
	}
	
	public Matrix(float... f){
		this.value=new float[f.length][1];
		for(int i=0;i<f.length;i++){
			this.value[i][0]=f[i];
		}
	}
	
	/**[row][column]*/
	public Matrix(float[][] value){
		this.value=value;
	}
	
	public Matrix(Vec3d vec3){
		float[][] value={{(float) vec3.x},{(float) vec3.y},{(float) vec3.z}};
		this.value=value;
	}
	
	/**euler angle (radian). y-x-y*/
	public Matrix(float alpha,float beta,float gumma){
		float cosA=MathHelper.cos(alpha);
		float cosB=MathHelper.cos(beta);
		float cosC=MathHelper.cos(gumma);
		float sinA=MathHelper.sin(alpha);
		float sinB=MathHelper.sin(beta);
		float sinC=MathHelper.sin(gumma);
		
		float[][] value={{cosA*cosB*cosC-sinA*sinC,-cosA*cosB*sinC-sinA*cosC,cosA*sinB},
				{sinA*cosB*cosC+cosA*sinC,-sinA*cosB*sinC+cosA*cosC,sinA*sinC},
				{-sinB*cosC,sinB*sinC,cosB}};
		this.value=value;
		
	}
	
	public Matrix add(Matrix matrix){
		if(this.value.length!=matrix.value.length){
			throw new ArithmeticException
			("Its row ("+this.value.length+")and argument's row ("+matrix.value.length+")must be same when add each other");
		}
		if(this.value[0].length!=matrix.value[0].length){
			throw new ArithmeticException
			("Its column ("+this.value[0].length+")and argument's column ("+matrix.value[0].length+")must be same when add each other");
		}
		float[][] result=new float[this.value.length][this.value[0].length];
		for(int i=0;i<this.value.length;i++){
			for(int j=0;j<this.value[0].length;j++){
				result[i][j]=this.value[i][j]+matrix.value[i][j];
			}
		}
		return new Matrix(result);
	}
	
	public Matrix subtract(Matrix matrix){
		if(this.value.length!=matrix.value.length){
			throw new ArithmeticException
			("Its row ("+this.value.length+")and argument's row ("+matrix.value.length+")must be same when subtract each other");
		}
		if(this.value[0].length!=matrix.value[0].length){
			throw new ArithmeticException
			("Its column ("+this.value[0].length+")and argument's column ("+matrix.value[0].length+")must be same when subtract each other");
		}
		float[][] result=new float[this.value.length][this.value[0].length];
		for(int i=0;i<this.value.length;i++){
			for(int j=0;j<this.value[0].length;j++){
				result[i][j]=this.value[i][j]-matrix.value[i][j];
			}
		}
		return new Matrix(result);
	}
	
	public Matrix scalarProduct(float f){
		float[][] result=new float[this.value.length][this.value[0].length];
		for(int i=0;i<this.value.length;i++){
			for(int j=0;j<this.value[0].length;j++){
				result[i][j]=this.value[i][j]*f;
			}
		}
		return new Matrix(result);
	}
	
	/** This matrix * argument matrix .
	 * You can use it to rotate matrix of argument. */
	public Matrix product(Matrix matrix){
		float[][] result=new float[this.value.length][matrix.value[0].length];
		if(this.value.length!=matrix.value[0].length){
			throw new ArithmeticException
			("Its row ("+this.value.length+")and argument's column ("+matrix.value[0].length+")must be same when product each other");
		}
		for(int i=0;i<result.length;i++){
			for(int j=0;j<result[i].length;j++){
				for(int k=0;k<matrix.value.length;k++){
					result[i][j]+=this.value[i][k]*matrix.value[k][j];
				}
			}
		}
		return new Matrix(result);
	}
	
	public Vec3d toVec3d(){
		if(this.value.length!=3){
			throw new ArithmeticException("Its row ("+this.value.length+")must be 3");
		}
		if(this.value[0].length!=1){
			throw new ArithmeticException("Its column ("+this.value.length+")must be 1 when turn it into Vec3d");
		}
		return new Vec3d(this.value[0][0], this.value[1][0], this.value[2][0]);
	}
	
	@Override
	public String toString(){
		StringBuffer bs=new StringBuffer();
		for(int i=0;i<this.value.length;i++){
			bs.append(this.value[i].toString());
			if(i<this.value.length-1){
				bs.append(",");
			}
		}
		return bs.toString();
	}
}
