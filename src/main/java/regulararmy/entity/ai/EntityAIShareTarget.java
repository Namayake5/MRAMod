package regulararmy.entity.ai;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import regulararmy.entity.EntityRegularArmy;

public class EntityAIShareTarget extends EntityRegularAIBase {
	public EntityRegularArmy taskOwner;
	public EntityLivingBase entityToAttack;
	public int timer=-1;
	
	public EntityAIShareTarget(EntityRegularArmy entity){
		this.taskOwner=entity;
	}
	
	@Override
	public boolean shouldExecute() {
		this.timer++;
		
		if(this.timer%10==0){
			//System.out.println("target:"+this.taskOwner.getAttackTarget());
			if(taskOwner.getAttackTarget()==null){
				for(EntityRegularArmy e:taskOwner.unit.getEntityList()){
					if(e.getAttackTarget()!=null){
						
						this.entityToAttack=e.getAttackTarget();
						return true;
					}
				}
				return false;
			}else{
				if(taskOwner.getAttackTarget().isDead){
					this.entityToAttack=null;
					return true;
				}else{
					for(EntityRegularArmy e:taskOwner.unit.getEntityList()){
						if(e.getAttackTarget() == this.entityToAttack){
							return false;
						}
					}
					this.entityToAttack=null;
					return true;
				}
			}
		}
		return false;
	}
	
	@Override
	public void startExecuting(){
		this.taskOwner.setAttackTarget(entityToAttack);
		super.startExecuting();
	}

}
