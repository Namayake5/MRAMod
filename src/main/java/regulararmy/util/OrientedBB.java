package regulararmy.util;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class OrientedBB {
	public Vec3d origin;
	public Vec3d centreVec;
	public Vec3d widthVec,heightVec,depthVec;
	public float width,height,depth;
	public float rotateX, rotateY,rotateZ;

	public OrientedBB(Vec3d position,Vec3d rotationPoint, float width,
			float height, float depth,float rotateX,float rotateY,float rotateZ) {
		this.centreVec = rotationPoint.subtract(position);
		this.origin=rotationPoint;
		this.width = width;
		this.height = height;
		this.depth = depth;
		this.rotateX=rotateX;
		this.rotateY=rotateY;
		this.rotateZ=rotateZ;

		this.widthVec=this.centreVec.addVector(width/2, 0, 0);
		this.widthVec.rotatePitch(rotateX);
		this.widthVec.rotateYaw(rotateY);
		this.rotateRoll(this.widthVec,rotateZ);
		this.heightVec=this.centreVec.addVector(0,height/2,0);
		this.heightVec.rotatePitch(rotateX);
		this.heightVec.rotateYaw(rotateY);
		this.rotateRoll(this.heightVec,rotateZ);
		this.depthVec=this.centreVec.addVector(0,0,depth/2);
		this.depthVec.rotatePitch(rotateX);
		this.depthVec.rotateYaw(rotateY);
		this.rotateRoll(this.depthVec,rotateZ);
		this.centreVec.rotatePitch(rotateX);
		this.centreVec.rotateYaw(rotateY);
		this.rotateRoll(this.centreVec,rotateZ);
	}

    public Vec3d rotateRoll(Vec3d vec,float roll)
    {
        float f = MathHelper.cos(roll);
        float f1 = MathHelper.sin(roll);
        double d0 = vec.x * (double)f + vec.y * (double)f1;
        double d1 = vec.y * (double)f - vec.x * (double)f1;
        double d2 = vec.z;
        return new Vec3d(d0, d1, d2);
    }

	public OrientedBB(AxisAlignedBB aabb){
		this(new Vec3d((aabb.maxX+aabb.minX)/2,(aabb.maxY+aabb.minY)/2,(aabb.maxZ+aabb.minZ)/2),
				new Vec3d((aabb.maxX+aabb.minX)/2,(aabb.maxY+aabb.minY)/2,(aabb.maxZ+aabb.minZ)/2),
				(float)(aabb.maxX-aabb.minX),(float)(aabb.maxY-aabb.minY),(float)(aabb.maxZ-aabb.minZ),0f,0f,0f);
	}
	public OrientedBB(AxisAlignedBB aabb,Vec3d rotationPoint,float rotateX,float rotateY,float rotateZ){
		this(new Vec3d((aabb.maxX+aabb.minX)/2,(aabb.maxY+aabb.minY)/2,(aabb.maxZ+aabb.minZ)/2),
				rotationPoint,
				(float)(aabb.maxX-aabb.minX),(float)(aabb.maxY-aabb.minY),(float)(aabb.maxZ-aabb.minZ),rotateX,rotateY,rotateZ);
	}

	public boolean isCollidingWith(OrientedBB obb){
		Vec3d Ae1=this.centreVec.subtract(this.widthVec);
		Vec3d Ae2=this.centreVec.subtract(this.heightVec);
		Vec3d Ae3=this.centreVec.subtract(this.depthVec);
		Vec3d NAe1=Ae1.normalize();
		Vec3d NAe2=Ae2.normalize();
		Vec3d NAe3=Ae3.normalize();

		Vec3d Be1=obb.centreVec.subtract(obb.widthVec);
		Vec3d Be2=obb.centreVec.subtract(obb.heightVec);
		Vec3d Be3=obb.centreVec.subtract(obb.depthVec);
		Vec3d NBe1=Be1.normalize();
		Vec3d NBe2=Be2.normalize();
		Vec3d NBe3=Be3.normalize();

		Vec3d totc=this.origin.addVector(this.centreVec.x, this.centreVec.y, this.centreVec.z);
		Vec3d oooc=obb.origin.addVector(obb.centreVec.x, obb.centreVec.y, obb.centreVec.z);

		Vec3d Interval=totc.subtract(oooc);
		//System.out.println(" interval:"+D3DXVec3dLength(Interval));
		//System.out.println(" this.centre:"+D3DXVec3dLength(centreVec)+" this.origin:"+D3DXVec3dLength(origin)+
			//	" obb.centre:"+D3DXVec3dLength(obb.centreVec)+" obb.origin:"+D3DXVec3dLength(obb.origin));

		// 分離軸 : Ae1
		float rA = D3DXVec3dLength( Ae1 );
		float rB = LenSegOnSeparateAxis( NAe1, Be1, Be2, Be3 );
		float L = MathHelper.abs(D3DXVec3dDot( Interval, NAe1 ));
		if( L > rA + rB )
			return false; // 衝突していない

		// 分離軸 : Ae2
		rA = D3DXVec3dLength( Ae2 );
		rB = LenSegOnSeparateAxis( NAe2, Be1, Be2, Be3 );
		L = MathHelper.abs(D3DXVec3dDot( Interval, NAe2 ));
		if( L > rA + rB )
			return false;

		// 分離軸 : Ae3
		rA = D3DXVec3dLength( Ae3 );
		rB = LenSegOnSeparateAxis( NAe3, Be1, Be2, Be3 );
		L = MathHelper.abs(D3DXVec3dDot( Interval, NAe3 ));
		if( L > rA + rB )
			return false;

		// 分離軸 : Be1
		rA = LenSegOnSeparateAxis( NBe1, Ae1, Ae2, Ae3 );
		rB = D3DXVec3dLength( Be1 );
		L = MathHelper.abs(D3DXVec3dDot( Interval, NBe1 ));
		if( L > rA + rB )
			return false;

		// 分離軸 : Be2
		rA = LenSegOnSeparateAxis( NBe2, Ae1, Ae2, Ae3 );
		rB = D3DXVec3dLength( Be2 );
		L = MathHelper.abs(D3DXVec3dDot( Interval, NBe2 ));
		if( L > rA + rB )
			return false;

		// 分離軸 : Be3
		rA = LenSegOnSeparateAxis( NBe3, Ae1, Ae2, Ae3 );
		rB = D3DXVec3dLength( Be3 );
		L = MathHelper.abs(D3DXVec3dDot( Interval, NBe3 ));
		if( L > rA + rB )
			return false;

		// 分離軸 : C11
		Vec3d Cross=null;
		Cross=D3DXVec3dCross(  NAe1, NBe1 );
		rA = LenSegOnSeparateAxis( Cross, Ae2, Ae3 );
		rB = LenSegOnSeparateAxis( Cross, Be2, Be3 );
		L = MathHelper.abs(D3DXVec3dDot( Interval, Cross ));
		if( L > rA + rB )
			return false;

		// 分離軸 : C12
		Cross=D3DXVec3dCross( NAe1, NBe2 );
		rA = LenSegOnSeparateAxis( Cross, Ae2, Ae3 );
		rB = LenSegOnSeparateAxis( Cross, Be1, Be3 );
		L = MathHelper.abs(D3DXVec3dDot( Interval, Cross ));
		if( L > rA + rB )
			return false;

		// 分離軸 : C13
		Cross=D3DXVec3dCross( NAe1, NBe3 );
		rA = LenSegOnSeparateAxis( Cross, Ae2, Ae3 );
		rB = LenSegOnSeparateAxis( Cross, Be1, Be2 );
		L = MathHelper.abs(D3DXVec3dDot( Interval, Cross ));
		if( L > rA + rB )
			return false;

		// 分離軸 : C21
		Cross=D3DXVec3dCross(NAe2, NBe1 );
		rA = LenSegOnSeparateAxis( Cross, Ae1, Ae3 );
		rB = LenSegOnSeparateAxis( Cross, Be2, Be3 );
		L = MathHelper.abs(D3DXVec3dDot( Interval, Cross ));
		if( L > rA + rB )
			return false;

		// 分離軸 : C22
		Cross=D3DXVec3dCross(NAe2, NBe2 );
		rA = LenSegOnSeparateAxis( Cross, Ae1, Ae3 );
		rB = LenSegOnSeparateAxis( Cross, Be1, Be3 );
		L = MathHelper.abs(D3DXVec3dDot( Interval, Cross ));
		if( L > rA + rB )
			return false;

		// 分離軸 : C23
		Cross=D3DXVec3dCross( NAe2, NBe3 );
		rA = LenSegOnSeparateAxis( Cross, Ae1, Ae3 );
		rB = LenSegOnSeparateAxis( Cross, Be1, Be2 );
		L = MathHelper.abs(D3DXVec3dDot( Interval, Cross ));
		if( L > rA + rB )
			return false;

		// 分離軸 : C31
		Cross=D3DXVec3dCross( NAe3, NBe1 );
		rA = LenSegOnSeparateAxis( Cross, Ae1, Ae2 );
		rB = LenSegOnSeparateAxis( Cross, Be2, Be3 );
		L = MathHelper.abs(D3DXVec3dDot( Interval, Cross ));
		if( L > rA + rB )
			return false;

		// 分離軸 : C32
		Cross=D3DXVec3dCross( NAe3, NBe2 );
		rA = LenSegOnSeparateAxis( Cross, Ae1, Ae2 );
		rB = LenSegOnSeparateAxis( Cross, Be1, Be3 );
		L = MathHelper.abs(D3DXVec3dDot( Interval, Cross ));
		if( L > rA + rB )
			return false;

		// 分離軸 : C33
		Cross=D3DXVec3dCross( NAe3, NBe3 );
		rA = LenSegOnSeparateAxis( Cross, Ae1, Ae2 );
		rB = LenSegOnSeparateAxis( Cross, Be1, Be2 );
		L = MathHelper.abs(D3DXVec3dDot( Interval, Cross ));
		if( L > rA + rB )
			return false;

		// 分離平面が存在しないので「衝突している」
		return true;
	}

	public float LenSegOnSeparateAxis( Vec3d Sep, Vec3d e1, Vec3d e2){
		return LenSegOnSeparateAxis(Sep,e1,e2,null);
	}

	// 分離軸に投影された軸成分から投影線分長を算出
	public float LenSegOnSeparateAxis( Vec3d Sep, Vec3d e1, Vec3d e2, Vec3d e3)
	{
		// 3つの内積の絶対値の和で投影線分長を計算
		// 分離軸Sepは標準化されていること
		float r1 = MathHelper.abs(D3DXVec3dDot( Sep, e1 ));
		float r2 = MathHelper.abs(D3DXVec3dDot( Sep, e2 ));
		float r3 = e3!=null ? (MathHelper.abs(D3DXVec3dDot( Sep, e3 ))) : 0;
		return r1 + r2 + r3;
	}

	public float D3DXVec3dLength(Vec3d vec){
		return (float) vec.lengthVector();
	}

	public float D3DXVec3dDot(Vec3d vec1,Vec3d vec2){
		return (float) vec1.dotProduct(vec2);
	}

	public Vec3d D3DXVec3dCross(Vec3d vec1,Vec3d vec2){

		return vec1.crossProduct(vec2);
	}
}
