package regulararmy.entity;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.BlockDynamicLiquid;
import net.minecraft.block.BlockStaticLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ReportedException;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EntityStone extends Entity
{
	public double prevMotionX,prevMotionY,prevMotionZ;
	public double destruction=5;
	public double reflection=0.7;
	public double damage=10;
	public EntityLivingBase setter=null;
	public float eyeHeight;
	public ForgeChunkManager.Ticket ticket;
	public List<ChunkPos> forcedChunkList=new ArrayList();

	public int timeToDisappear=0;
	public int timeToDisappearMax=1000;

	public EntityStone(World par1World)
	{
		super(par1World);

		this.setSize(1f, 1f);
		//System.out.println("position:"+posX+","+posY+","+posZ);
		//System.out.println("generated");
	}


	/**
	 * Called to update the entity's position/logic.
	 */
	@Override
	public void onUpdate()
	{
		super.onUpdate();
		//System.out.println("update");
		if(world.isRemote){

			return;
		}
		this.prevMotionX=this.motionX;
		this.prevMotionY=this.motionY;
		this.prevMotionZ=this.motionZ;

		if (MathHelper.abs((float) this.motionX) < 0.0001f) {
			this.motionX = 0;
		}
		if (MathHelper.abs((float) this.motionY) < 0.0001f) {
			this.motionY = 0;
		}
		if (MathHelper.abs((float) this.motionZ) < 0.0001f) {
			this.motionZ = 0;
		}


		int j=1+
				(int)((Math.max(Math.max(Math.abs(motionX),Math.abs(motionY)),Math.max(Math.abs(motionY),Math.abs(motionZ)))
						-Math.max(Math.max(Math.abs(motionX),Math.abs(motionY)),Math.max(Math.abs(motionY),Math.abs(motionZ)))%0.9)/0.9);
		//System.out.println("j="+j);

		//System.out.println("position:"+posX+","+posY+","+posZ);
		for(int i=1;i<=j;i++){

			this.onSplitUpdate(i, j);
			int minX = MathHelper.floor(this.getEntityBoundingBox().minX - 0.0D);
			int minY = MathHelper.floor(this.getEntityBoundingBox().minY - 0.0D);
			int minZ = MathHelper.floor(this.getEntityBoundingBox().minZ - 0.0D);
			int maxX = MathHelper.floor(this.getEntityBoundingBox().maxX + 0.0D);
			int maxY = MathHelper.floor(this.getEntityBoundingBox().maxY + 0.0D);
			int maxZ = MathHelper.floor(this.getEntityBoundingBox().maxZ + 0.0D);


			//System.out.println(maxX+","+maxY+","+maxZ+","+minX+","+minY+","+minZ+",");
			boolean xflag=false;
			boolean yflag=false;
			boolean zflag=false;


			if (this.world.isChunkGeneratedAt(((int)posX)>>4,((int)posZ)>>4))
			{
				for (int x = minX; x <= maxX; ++x)
				{
					for (int y = minY; y <= maxY; ++y)
					{
						for (int z = minZ; z <= maxZ; ++z)
						{
							BlockPos pos=new BlockPos(x,y,z);
							if(!(this.world.isAirBlock(pos))){

								IBlockState target=this.world.getBlockState(pos);
								AxisAlignedBB tAABB=target.getCollisionBoundingBox(world,pos);

								if(tAABB!=null){
									tAABB=tAABB.offset(-pos.getX(),-pos.getY(),-pos.getZ());
									if(this.getEntityBoundingBox().intersects(new AxisAlignedBB(tAABB.minX+x,tAABB.minY+y,tAABB.minZ+z,
											tAABB.maxX+x,tAABB.maxY+y,tAABB.maxZ+z))){
										double relativeX,relativeY,relativeZ;
										relativeX=this.posX-x-(tAABB.maxX+tAABB.minX)/2;
										relativeY=this.getCentreYOffset()-y-(tAABB.maxY+tAABB.minY)/2;
										relativeZ=this.posZ-z-(tAABB.maxZ+tAABB.minZ)/2;
										//System.out.println(relativeX+","+relativeY+","+relativeZ);

										relativeX*=(tAABB.maxY-tAABB.minY)*(tAABB.maxZ-tAABB.minZ);
										relativeY*=(tAABB.maxX-tAABB.minX)*(tAABB.maxZ-tAABB.minZ);
										relativeZ*=(tAABB.maxY-tAABB.minY)*(tAABB.maxX-tAABB.minX);
										double hardness;
										if(target.getBlockHardness(world,pos)==-1){
											hardness=Float.MAX_VALUE;
										}else{
											hardness=1.5*(target.getBlockHardness(world, pos)+(2.0-target.getBlockHardness(world,pos))/2)/this.destruction;
										}

										try{
											if(Math.abs(relativeX)>=Math.abs(relativeY)&&Math.abs(relativeX)>=Math.abs(relativeZ)){
												//System.out.println("CollidedX("+k1+","+l1+","+i2+")");
												if(!((target instanceof BlockDynamicLiquid)||(target instanceof BlockStaticLiquid))&&!xflag){
													if(motionX>0&&relativeX<0){
														this.onCollidedWithBlock(x, y, z,hardness, EnumFacing.WEST,tAABB);
														if(this.motionX>hardness){
															this.onBreakBlock(pos,hardness, EnumFacing.WEST,tAABB);
														}else{
															xflag=true;
															this.onFailedToBreakBlock(x, y, z,hardness, EnumFacing.WEST,tAABB);
														}
													}else if(motionX<=0&&relativeX>=0){
														this.onCollidedWithBlock(x, y, z,hardness, EnumFacing.EAST,tAABB);
														if(-this.motionX>hardness){

															this.onBreakBlock(pos,hardness, EnumFacing.EAST,tAABB);
														}else{
															xflag=true;

															this.onFailedToBreakBlock(x, y, z,hardness, EnumFacing.EAST,tAABB);
														}
													}
												}
											}else if(Math.abs(relativeY)>=Math.abs(relativeX)&&Math.abs(relativeY)>=Math.abs(relativeZ)){
												//System.out.println("CollidedY("+k1+","+l1+","+i2+")");
												if(!((target instanceof BlockDynamicLiquid)||(target instanceof BlockStaticLiquid))&&!yflag){
													if(motionY>0&&relativeY<0){
														this.onCollidedWithBlock(x, y, z,hardness, EnumFacing.DOWN,tAABB);
														if(this.motionY>hardness){

															this.onBreakBlock(pos,hardness, EnumFacing.DOWN,tAABB);
														}else{
															yflag=true;

															this.onFailedToBreakBlock(x, y, z,hardness, EnumFacing.DOWN,tAABB);
														}
													}else if(motionY<=0&&relativeY>=0){
														this.onCollidedWithBlock(x, y, z,hardness, EnumFacing.UP,tAABB);
														if(-this.motionY>hardness){

															this.onBreakBlock(pos,hardness, EnumFacing.UP,tAABB);
														}else{
															yflag=true;

															this.onFailedToBreakBlock(x, y, z,hardness, EnumFacing.UP,tAABB);
														}
													}
												}
											}else if(Math.abs(relativeZ)>=Math.abs(relativeY)&&Math.abs(relativeZ)>=Math.abs(relativeX)){

												//System.out.println("CollidedZ("+k1+","+l1+","+i2+")");
												if(!((target instanceof BlockDynamicLiquid)||(target instanceof BlockStaticLiquid))&&!zflag){
													if(motionZ>0&&relativeZ<0){
														this.onCollidedWithBlock(x, y, z,hardness, EnumFacing.NORTH,tAABB);
														if(this.motionZ>hardness){

															this.onBreakBlock(pos,hardness, EnumFacing.NORTH,tAABB);
														}else{
															zflag=true;

															this.onFailedToBreakBlock(x, y, z,hardness, EnumFacing.NORTH,tAABB);
														}
													}else if(motionZ<=0&&relativeZ>=0){
														this.onCollidedWithBlock(x, y, z,hardness, EnumFacing.SOUTH,tAABB);
														if(-this.motionZ>hardness){

															this.onBreakBlock(pos,hardness, EnumFacing.SOUTH,tAABB);
														}else{
															zflag=true;

															this.onFailedToBreakBlock(x, y, z,hardness, EnumFacing.SOUTH,tAABB);
														}
													}
												}
											}

										}catch(NullPointerException e){
										}
									}
								}
							}
						}
					}
				}
			}


			this.setEntityBoundingBox(this.getEntityBoundingBox().offset(motionX/j, motionY/j, motionZ/j));
			this.posX = (this.getEntityBoundingBox().minX + this.getEntityBoundingBox().maxX) / 2.0D;
			this.posY = (this.getEntityBoundingBox().minY + this.getEntityBoundingBox().maxY)/2.0D-this.eyeHeight;
			this.posZ = (this.getEntityBoundingBox().minZ + this.getEntityBoundingBox().maxZ) / 2.0D;

			//this.setPosition(this.posX+this.motionX/j,this.posY+this.motionY/j, this.posZ+this.motionZ/j);

			List entityList=world.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox());
			if(!entityList.isEmpty()){

				for(int i1=0;i1<entityList.size();i1++){
					if(entityList.get(i1) instanceof Entity){

						Entity entity=(Entity)entityList.get(i1);
						//System.out.println("collided with"+entity.getClass().getSimpleName());
						this.onCollidedWithEntity(entity);
					}
				}

			}
			if(!yflag){
				this.onGround=false;
			}
			try
			{
				this.doBlockCollisions();
			}
			catch (Throwable throwable)
			{
				CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Checking entity tile collision");
				CrashReportCategory crashreportcategory = crashreport.makeCategory("Entity being checked for collision");
				this.addEntityCrashInfo(crashreportcategory);
				throw new ReportedException(crashreport);
			}

		}

		if(!this.onGround){
			this.motionY -= 0.06;
		}else{
			this.motionX*=0.9;
			this.motionZ*=0.9;
		}
		if(this.posY<192){
			this.motionX *= 0.98D;
			this.motionY *= 0.98D;
			this.motionZ *= 0.98D;
		}

		//System.out.println("motion:"+motionX+","+motionY+","+motionZ);
		//System.out.println(this.onGround);


		//System.out.println("position:"+posX+","+posY+","+posZ);

		if(this.timeToDisappear<this.timeToDisappearMax){
			this.timeToDisappear++;
		}else{
			this.setDead();
		}

	}
	@Override
	public void setSize(float width,float height){
		super.setSize(width, height);
		this.eyeHeight=height/2;
	}

	@Override
	public float getEyeHeight(){
		return this.eyeHeight;
	}

	@Override
	public boolean canBePushed(){
		return true;
	}

	public AxisAlignedBB getCollisionBox(Entity par1Entity)
	{
		return par1Entity.getEntityBoundingBox();
	}


	public double getCentreYOffset(){
		return this.posY+this.height/2;
	}

	/**
	 * Return the motion factor for this projectile. The factor is multiplied by the original motion.
	 */
	protected float getMotionFactor()
	{
		return 0.98F;
	}


	/**
	 * (abstract) Protected helper method to write subclass entity data to NBT.
	 */
	@Override
	public void writeEntityToNBT(NBTTagCompound nbt)
	{
		nbt.setDouble("reflection", this.reflection);
		nbt.setDouble("damage", this.damage);
		nbt.setDouble("destruction", this.destruction);
		nbt.setInteger("timeToDisappear", this.timeToDisappear);
		nbt.setInteger("timeToDisappearMax", this.timeToDisappearMax);
	}

	/**
	 * (abstract) Protected helper method to read subclass entity data from NBT.
	 */
	@Override
	public void readEntityFromNBT(NBTTagCompound nbt)
	{
		this.reflection=nbt.getDouble("reflection");
		this.damage=nbt.getDouble("damage");
		this.destruction=nbt.getDouble("destruction");
		this.timeToDisappear=nbt.getInteger("timeToDisappear");
		this.timeToDisappearMax=nbt.getInteger("timeToDisappearMax");
		System.out.println(this.timeToDisappear+" , "+this.timeToDisappearMax);
	}


	public boolean isEnteringUnloadedChunk(){
		AxisAlignedBB aabb=this.getEntityBoundingBox();
		return !this.world.isChunkGeneratedAt(((int)posX)>>4,((int)posZ)>>4);
	}

	/**
	 * Returns true if other Entities should be prevented from moving through this Entity.
	 */
	public boolean canBeCollidedWith()
	{
		return true;
	}


	/**
	 * Called when the entity is attacked.
	 */


	@SideOnly(Side.CLIENT)
	public float getShadowSize()
	{
		return 0.0F;
	}



	@SideOnly(Side.CLIENT)
	public int getBrightnessForRender(float par1)
	{
		return 15728880;
	}

	/**
	 * called when this broke a block
	 * @param x block xcoord
	 * @param y block ycoord
	 * @param z block zcoord
	 * @param hardness hardness of the block(treated)
	 * @param fd side which this collided
	 * @param tAABB AxisAlignedBoundingBox of the block
	 */
	public void onBreakBlock(BlockPos pos,double hardness,EnumFacing fd,AxisAlignedBB tAABB){
		//System.out.println("break");
		switch(fd){
		case WEST:
			this.motionX-=hardness;
			this.world.destroyBlock(pos, true);
			break;
		case EAST:
			this.motionX+=hardness;
			this.world.destroyBlock(pos, true);
			break;
		case DOWN:
			this.motionY-=hardness;
			this.world.destroyBlock(pos, true);
			break;
		case UP:
			this.motionY+=hardness;
			this.world.destroyBlock(pos, true);
			break;
		case NORTH:
			this.motionZ-=hardness;
			this.world.destroyBlock(pos, true);
			break;
		case SOUTH:
			this.motionZ+=hardness;
			this.world.destroyBlock(pos, true);
			break;
		default:
			break;

		}
	}

	/**
	 * called when this failed to break a block
	 * @param x block xcoord
	 * @param y block ycoord
	 * @param z block zcoord
	 * @param hardness hardness of the block(treated)
	 * @param fd side which this collided
	 * @param tAABB AxisAlignedBoundingBox of the block
	 */
	public void onFailedToBreakBlock(int x,int y,int z,double hardness,EnumFacing fd,AxisAlignedBB tAABB){
		//System.out.println("fail");
		switch(fd){

		case WEST:
			this.getEntityBoundingBox().offset(x+tAABB.minX-this.getEntityBoundingBox().maxX, 0, 0);
			this.motionX*=-this.reflection;
			break;
		case EAST:
			this.getEntityBoundingBox().offset(x+tAABB.maxX-this.getEntityBoundingBox().minX,0,0);
			this.motionX*=-this.reflection;
			break;
		case DOWN:
			this.getEntityBoundingBox().offset(0,y+tAABB.minY-this.getEntityBoundingBox().maxY,0);
			this.motionY*=-this.reflection;
			break;
		case UP:
			this.getEntityBoundingBox().offset(0,y+tAABB.maxY-this.getEntityBoundingBox().minY,0);
			this.motionY*=-this.reflection;
			break;
		case NORTH:
			this.getEntityBoundingBox().offset(0,0,z+tAABB.minZ-this.getEntityBoundingBox().maxZ);
			this.motionZ*=-this.reflection;
			break;
		case SOUTH:
			this.getEntityBoundingBox().offset(0,0,z+tAABB.maxZ-this.getEntityBoundingBox().minZ);
			this.motionZ*=-this.reflection;
			break;
		default:
			break;

		}
		if(fd.equals(EnumFacing.UP)){
			if(!this.onGround&&this.motionY<MathHelper.sqrt(this.reflection)/5){
				this.onGround=true;
				this.motionY=0;
			}
		}
	}

	/**
	 * called when this collided with a block
	 * @param x block xcoord
	 * @param y block ycoord
	 * @param z block zcoord
	 * @param hardness hardness of the block(treated)
	 * @param fd side which this collided
	 * @param tAABB AxisAlignedBoundingBox of the block
	 */
	public void onCollidedWithBlock(int x,int y,int z,double hardness,EnumFacing fd,AxisAlignedBB tAABB){
	}

	/**
	 * called when this collided with an entity
	 * @param entity entity collided with
	 */
	public void onCollidedWithEntity(Entity entity){
		//System.out.println("collide entity");
		double relativeMotionX=this.motionX-entity.motionX;
		double relativeMotionY=this.motionY-entity.motionY;
		double relativeMotionZ=this.motionZ-entity.motionZ;
		double relativeSpeedSquare=relativeMotionX*relativeMotionX+relativeMotionY*relativeMotionY+relativeMotionZ*relativeMotionZ;
		if(relativeSpeedSquare>0.25){

			double damage=this.damage*(MathHelper.sqrt(relativeSpeedSquare)-0.5);
			entity.attackEntityFrom(DamageSource.causeThrownDamage(this, this.setter), (float)damage);

		}
		if (!this.isPassenger(entity) && entity.canBePushed())
		{
			if(entity instanceof EntityStone){
				double motionX_=this.motionX;
				double motionY_=this.motionY;
				double motionZ_=this.motionZ;
				this.motionX=entity.motionX;
				this.motionY=entity.motionY;
				this.motionZ=entity.motionZ;
				entity.motionX=motionX_;
				entity.motionY=motionY_;
				entity.motionZ=motionZ_;
			}else if(!((entity instanceof EntityLivingBase) && ((EntityLivingBase)entity).hurtResistantTime>0)){

				double motionX_=this.motionX;
				double motionY_=this.motionY;
				double motionZ_=this.motionZ;
				this.motionX=this.motionX/5*4+entity.motionX/5;
				this.motionY=this.motionY/5*4+entity.motionY/5;
				this.motionZ=this.motionZ/5*4+entity.motionZ/5;
				entity.motionX=motionX_;
				entity.motionY=motionY_;
				entity.motionZ=motionZ_;

			}

			entity.applyEntityCollision(this);
		}
	}

	/**
	 * called on update split to update the destroy of blocks precisely*/
	public void onSplitUpdate(int times,int maxSplit){
	}


	@Override
	protected void entityInit() {

	}
}


