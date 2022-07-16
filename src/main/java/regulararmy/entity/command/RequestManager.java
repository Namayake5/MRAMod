package regulararmy.entity.command;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.EntityLiving;
import net.minecraft.util.math.BlockPos;
import regulararmy.entity.ai.EngineerRequest;
import regulararmy.entity.ai.EngineerRequest.RequestType;

public class RequestManager {
public List<EngineerRequest> requested=new ArrayList();

public void request(BlockPos c,boolean isSet,BlockPos waitingPoint){

	for(int i=0;i<requested.size();i++){
		EngineerRequest e=requested.get(i);
		if(e.coord.equals(c)){
			e.number++;
			//System.out.println("requested on:"+c.getX()+","+c.getY()+","+c.getZ()+" as "+(isSet?"set":"break")+" for "+e.number+" mobs");
			return;
		}
	}
	//System.out.println("requested on:"+c.getX()+","+c.getY()+","+c.getZ()+" as "+(isSet?"set":"break")+" for 1 mob");
	EngineerRequest newe=new EngineerRequest(c,waitingPoint,RequestType.BREAK,1,this);
	requested.add(newe);
	newe.isEnable=true;

}

public void delete(BlockPos c){
	for(int i=0;i<requested.size();i++){
		EngineerRequest e=requested.get(i);
		if(e.coord.equals(c)){
			if(e.number<=1){
				requested.remove(e);
				e.isEnable=false;
			}else{
				e.number--;
			}
			return;
		}
	}
}

public EngineerRequest getNearest(EntityLiving e){
	return getNearest(e.posX,e.posY,e.posZ);
}

public EngineerRequest getNearest(double posX,double posY,double posZ){
	if(requested.size()==0)return null;
	EngineerRequest e=requested.get(0);
	double n=e.getSquareDistance(posX, posY, posZ);
	for(int i=1;i<requested.size();i++){
		if(requested.get(i).getSquareDistance(posX, posY, posZ)<n)e=requested.get(i);
	}
	return e;
}

public EngineerRequest getNearest(BlockPos c){
	return getNearest(c.getX()+0.5,c.getY()+0.5,c.getZ()+0.5);
}
public EngineerRequest getEqual(BlockPos c){
	for(int i=0;i<requested.size();i++){
		EngineerRequest e=requested.get(i);
		if(e.coord.equals(c)){
			return e;
		}
	}
	return null;
}

public boolean isThereNotApproved(){
	for(int i=0;i<requested.size();i++){
		if(!requested.get(i).hasApproved)return true;
	}
	return false;
}

}
