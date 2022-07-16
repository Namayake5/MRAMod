package regulararmy.pathfinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import regulararmy.entity.ai.EngineerRequest;
import regulararmy.entity.ai.EngineerRequest.RequestType;
import regulararmy.entity.command.RegularArmyLeader;

public class AStarPathEntity
{
    /** The actual points in the path */
    public final AStarPathPoint[] points;

    /** PathEntity Array Index the Entity is currently targeting */
    private int currentPathIndex;

    /** The total length of the path */
    private int pathLength;

    private IPathFindRequester ai;

    public Map<Integer,Float> crowdCost=new HashMap();

    public boolean arrival;

    public int entitySize;

    public AStarPathEntity(AStarPathPoint[] par1ArrayOfAStarPathPoint,IPathFindRequester ai2,boolean isArrival,int entitySize)
    {
        this.points = par1ArrayOfAStarPathPoint;
        this.pathLength = par1ArrayOfAStarPathPoint.length;
        this.ai=ai2;
        this.arrival=isArrival;
        this.entitySize=entitySize;
    }

    /**
     * Directs this path to the next point in its array
     */
    public void incrementPathIndex()
    {
        ++this.currentPathIndex;
    }

    /**
     * Returns true if this path has reached the end
     */
    public boolean isFinished()
    {
        return this.currentPathIndex >= this.pathLength;
    }

    /**
     * returns the last AStarPathPoint of the Array
     */
    public AStarPathPoint getFinalPathPoint()
    {
        return this.pathLength > 0 ? this.points[this.pathLength - 1] : null;
    }

    /**
     * return the AStarPathPoint located at the specified PathIndex, usually the current one
     */
    public AStarPathPoint getPathPointFromIndex(int par1)
    {
        return this.points[par1];
    }

    public int getCurrentPathLength()
    {
        return this.pathLength;
    }

    public void setCurrentPathLength(int par1)
    {
        this.pathLength = par1;
    }

    public int getCurrentPathIndex()
    {
        return this.currentPathIndex;
    }

    public AStarPathPoint getCurrentPoint(){
    	return this.points[this.currentPathIndex];
    }

    public void setCurrentPathIndex(int par1)
    {
        this.currentPathIndex = par1;
    }

    /**
     * Gets the vector of the AStarPathPoint associated with the given index.
     */
    public Vec3d getVectorFromIndex(Entity par1Entity, int par2)
    {
    	if(this.entitySize==1){
    		double d0 = (double)this.points[par2].x + 0.5;
    		double d1 = (double)this.points[par2].y + this.points[par2].yOffset;
    		double d2 = (double)this.points[par2].z + 0.5;
    		return new Vec3d(d0, d1, d2);
    	}else{
    		double d0 = (double)this.points[par2].x ;
    		double d1 = (double)this.points[par2].y + this.points[par2].yOffset;
    		double d2 = (double)this.points[par2].z ;
    		return new Vec3d(d0, d1, d2);
    	}
    }

    /**
     * returns the current PathEntity target node as Vec3dD
     */
    public Vec3d getPosition(Entity par1Entity)
    {
        return this.getVectorFromIndex(par1Entity, this.currentPathIndex);
    }

    /**
     * Returns true if the EntityPath are the same. Non instance related equals.
     */
    public boolean isSamePath(AStarPathEntity par1PathEntity)
    {
        if (par1PathEntity == null)
        {
            return false;
        }
        else if (par1PathEntity.points.length != this.points.length)
        {
            return false;
        }
        else
        {
            for (int i = 0; i < this.points.length; ++i)
            {
                if (this.points[i].x != par1PathEntity.points[i].x || this.points[i].y != par1PathEntity.points[i].y || this.points[i].z != par1PathEntity.points[i].z)
                {
                    return false;
                }
            }

            return true;
        }
    }

    public int getTotalCost(){
    	return this.points[pathLength-1].totalRealCost;
    }

    /**
     * Returns true if the final AStarPathPoint in the PathEntity is equal to Vec3dD coords.
     */
    public boolean isDestinationSame(Vec3d par1Vec3d)
    {
        AStarPathPoint AStarPathPoint = this.getFinalPathPoint();
        return AStarPathPoint == null ? false : AStarPathPoint.x == (int)par1Vec3d.x && AStarPathPoint.z == (int)par1Vec3d.z;
    }

    public void enablePath(RegularArmyLeader leader,Entity entity){
    	this.crowdCost=this.calcCrowdCost(this.ai.getCrowdCost(), this.ai.getCrowdCost());
    	leader.analyzerManager.setting.addCrowdCost(this.crowdCost);

    	for(int i=0;i<this.pathLength;i++){
    		this.points[i].requests=new ArrayList();
    		if(this.points[i].blocksToPut!=null){
    			for(int j=0;j<this.points[i].numberOfBlocksToPut;j++){
    				this.points[i].requests.add(new EngineerRequest(this.points[i].blocksToPut[j], RequestType.PUT_BLOCK));

    			}
    		}
    		if(this.points[i].laddersToPut!=null){
    			for(int j=0;j<this.points[i].numberOfLaddersToPut;j++){
    				EngineerRequest r=new EngineerRequest(this.points[i].laddersToPut[j], RequestType.PUT_LADDER);
    				r.dir=this.points[i].ladderDirection[j];
    				this.points[i].requests.add(r);
    			}
    		}

    		if(this.points[i].blocksToBreak!=null){
    			for(int j=0;j<this.points[i].numberOfBlocksToBreak;j++){
    				n:{
    				for(EngineerRequest er:this.points[i].requests){
    					if(er.coord.equals(this.points[i].blocksToBreak[j])){
    						break n;
    					}
    				}
    				this.points[i].requests.add(new EngineerRequest(this.points[i].blocksToBreak[j], RequestType.BREAK));
    			}
    			}


    		}


    	}
    	/*
    	if(ai.isEngineer()){
    		for(int i=0;i<this.pathLength;i++){
    			for(int j=0;j<this.points[i].numberOfBlocksToBreak;j++){
    				((EntityAIBreakBlock) ai).addMyTarget(new EngineerRequest(this.points[i].blocksToBreak[j], false));
    			}
    			for(int j=0;j<this.points[i].numberOfBlocksToPut;j++){
    				((EntityAIBreakBlock) ai).addMyTarget(new EngineerRequest(this.points[i].blocksToPut[j], true));
    			}
    		}
    	}
    	 */
    	/*
    	else{
    		for(int i=0;i<this.pathLength;i++){
    			for(int j=0;j<this.points[i].numberOfBlocksToBreak;j++){
    				leader.manager.request(this.points[i].blocksToBreak[j], false,this.points[i==0?0:i-1].toCoord());
    			}
    			for(int j=0;j<this.points[i].numberOfBlocksToPut;j++){
    				manager.request(this.points[i].blocksToPut[j], true,this.points[i==0?0:i-1].toCoord());
    			}
    		}
    	}
    	 */
    }

    public void disablePath(RegularArmyLeader leader){
    	leader.analyzerManager.setting.removeCrowdCost(this.crowdCost);
    	/*
    	for(int i=0;i<this.pathLength;i++){
    		for(int j=0;j<this.points[i].numberOfBlocksToBreak;j++){
    			manager.delete(this.points[i].blocksToBreak[j]);
    		}
    		for(int j=0;j<this.points[i].numberOfBlocksToPut;j++){
    			manager.delete(this.points[i].blocksToPut[j]);
    		}
    	}
    	*/
    }

    /**
     * @param costFirst cost when this entity first enter to the MiniChunk
     * @param costCont cost when this entity is in the MiniChunk */
    public Map<Integer,Float> calcCrowdCost(float costFirst,float costCont){
    	Map<Integer,Float> map=new HashMap();
    	int hash=this.points[0].makeMiniChunkHash();
    	float value=costFirst;
    	for(int i=1;i<this.pathLength;i++){
    		int hashNow=this.points[i].makeMiniChunkHash();
    		if(hash==hashNow){
    			value+=costCont;
    		}else{
    			map.put(hash, value);
    			hash=hashNow;
    			if(map.containsKey(hash)){
    				value=costCont;
    			}else{
    				value=costFirst;
    			}
    		}

    	}
    	map.put(hash, value);
    	return map;
    }
}
