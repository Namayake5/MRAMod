package regulararmy.entity.ai;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import regulararmy.entity.command.RequestManager;

public class EngineerRequest {
	public BlockPos coord;
	public RequestType isSet;
	public int number;
	public boolean hasApproved;
	public boolean isEnable;
	public RequestManager manager;
	public BlockPos waitingPoint;
	public EnumFacing dir;

	public EngineerRequest(BlockPos c,BlockPos waitingPoint,RequestType isSet,int number,RequestManager manager){
		this.coord=c;
		this.waitingPoint=waitingPoint;
		this.isSet=isSet;
		this.number=number;
		this.manager=manager;
	}

	public EngineerRequest(BlockPos c,RequestType isSet){
		this.coord=c;
		this.isSet=isSet;
	}

	public double getSquareDistance(double posX,double posY,double posZ){
		double i=this.coord.getX()+0.5-posX;
		double j=this.coord.getY()+0.5-posY;
		double k=this.coord.getZ()+0.5-posZ;
		return i*i+j*j+k*k;
	}

	public double getSquareDistance(BlockPos c){
		return getSquareDistance(c.getX()+0.5,c.getY()+0.5,c.getZ()+0.5);
	}

	public void approve(){
		this.hasApproved=true;
	}

	public void fulfill(){
		manager.requested.remove(this);
		this.isEnable=false;
	}

	@Override
	public String toString(){
		String s="";
		switch(this.isSet){
		case PUT_BLOCK:
			s="PutBlock";
			break;
		case BREAK:
			s="Break";
			break;
		case PUT_LADDER:
			s="PutLadder";
			break;
		default:
			break;
		}
		return s+" at "+this.coord.toString();
	}

	public static enum RequestType{
		PUT_BLOCK,PUT_LADDER,BREAK
	}
}
