package regulararmy.entity.ai;

import net.minecraft.entity.Entity;
import regulararmy.entity.EntityRegularArmy;
import regulararmy.pathfinding.AStarPathEntity;
import regulararmy.pathfinding.AStarPathPoint;
import regulararmy.pathfinding.IPathFindRequester;

public class EntityAIEscapeFromDrown extends EntityRegularAIBase implements IPathFindRequester{
	public EntityRegularArmy theEntity;
	public AStarPathEntity pathEntity;
	public short findingFailPenalty=0;

	public EntityAIEscapeFromDrown(EntityRegularArmy e){
		this.theEntity=e;
		this.setMutexBits(2);
	}
	@Override
	public boolean shouldExecute() {
		
		if(theEntity.getAir()>60)return false;
		if(this.findingFailPenalty-->0)return false;
		theEntity.getANavigator().canUseEngineer=0;
		this.pathEntity=theEntity.getANavigator().getPathToXYZ(theEntity.posX+theEntity.world.rand.nextInt(7)-3, theEntity.posY+theEntity.world.rand.nextInt(5)-2, theEntity.posZ+theEntity.world.rand.nextInt(7)-3, 1.4f, this);
		theEntity.getANavigator().canUseEngineer=2;
		if(this.pathEntity==null){
			this.findingFailPenalty=15;
			return false;
		}else{
			return true;
		}
		
	}
	
	@Override
	public void startExecuting() {
		theEntity.getANavigator().setPath(this.pathEntity, this.theEntity.getSpeed());
	}
	@Override
	public boolean shouldContinueExecuting(){
		return theEntity.getAir()<=100;
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
		return this.theEntity.data.jumpHeight;
	}

	@Override
	public float getCrowdCost() {
		return this.theEntity.data.crowdCostPerBlock;
	}

	@Override
	public float getFightRange() {
		return this.theEntity.data.fightRange;
	}
}
