package regulararmy.entity.ai;

import net.minecraft.entity.Entity;
import regulararmy.entity.EntityEngineer;
import regulararmy.entity.EntityRegularArmy;
import regulararmy.pathfinding.AStarPathEntity;
import regulararmy.pathfinding.AStarPathPoint;
import regulararmy.pathfinding.IPathFindRequester;

public class EntityAIFollowEngineer extends EntityRegularAIBase implements IPathFindRequester{

	public EntityEngineer engineer;
	public EntityRegularArmy entity;
	public int penalty;

	public EntityAIFollowEngineer(EntityRegularArmy host,EntityEngineer target){
		this.engineer=target;
		this.entity=host;
		this.penalty=0;
		this.setMutexBits(3);
	}
	@Override
	public boolean shouldExecute() {
		return !this.engineer.isDead&&(this.entity.getAttackTarget()==null || this.engineer.getDistance(entity)>16);
	}

	@Override
	public void updateTask(){
		if(this.penalty>0){
			this.penalty--;
			return;
		}
		double relativeX=this.engineer.posX-this.entity.posX;
		double relativeZ=this.engineer.posZ-this.entity.posZ;
		if(Math.abs(relativeX)<Math.abs(relativeZ)){
			if(relativeZ<0){
				AStarPathEntity pathentity=this.entity.getANavigator().getPathToXYZ(this.engineer.posX, this.engineer.posY, this.engineer.posZ+1, 1.4f, this);
				if(pathentity!=null){
					this.entity.getANavigator().setPath(pathentity, 1.2);
				}
			}else{
				AStarPathEntity pathentity=this.entity.getANavigator().getPathToXYZ(this.engineer.posX, this.engineer.posY, this.engineer.posZ-1, 1.4f, this);
				if(pathentity!=null){
					this.entity.getANavigator().setPath(pathentity, 1.2);
				}
			}
		}else{
			if(relativeX<0){
				AStarPathEntity pathentity=this.entity.getANavigator().getPathToXYZ(this.engineer.posX+1, this.engineer.posY, this.engineer.posZ, 1.4f, this);
				if(pathentity!=null){
					this.entity.getANavigator().setPath(pathentity, 1.2);
				}
			}else{
				AStarPathEntity pathentity=this.entity.getANavigator().getPathToXYZ(this.engineer.posX-1, this.engineer.posY, this.engineer.posZ, 1.4f, this);
				if(pathentity!=null){
					this.entity.getANavigator().setPath(pathentity, 1.2);
				}
			}
		}
		this.penalty=15;
	}

	@Override
	public int getTacticsCost(Entity entity, AStarPathPoint start,
			AStarPathPoint current, AStarPathPoint end) {
		return 0;
	}

	@Override
	public boolean isEngineer() {
		return false;
	}

	@Override
	public float getJumpHeight() {
		return this.entity.data.jumpHeight;
	}

	@Override
	public float getCrowdCost() {
		return this.entity.data.crowdCostPerBlock;
	}

	@Override
	public float getFightRange() {
		return this.entity.data.fightRange;
	}
}
