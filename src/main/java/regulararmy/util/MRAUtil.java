package regulararmy.util;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockStaticLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.fluids.BlockFluidClassic;

public class MRAUtil {
	public static boolean isAir(IBlockState b) {
		return b.getBlock()==Blocks.AIR;
	}

	public static boolean isLiquid(IBlockState b) {
		return b.getMaterial().isLiquid();
	}
	public static boolean isLiquidSource(IBlockState b) {
		Block block=b.getBlock();
		return b instanceof BlockStaticLiquid || ((b instanceof BlockFluidClassic) && ((BlockFluidClassic)b).getMetaFromState(b)==0);
	}

	public static boolean isAABBNull(Entity e,BlockPos pos) {
			double x=pos.getX();double y=pos.getY();double z=pos.getZ();
			IBlockState id=e.world.getBlockState(pos);
			if(MRAUtil.isAir(id))return true;
			if(id.isOpaqueCube()){
				return false;
			}
			List<AxisAlignedBB> list=MRAUtil.getCollisionListAt(e, pos);
			if(!list.isEmpty())return false;
			AxisAlignedBB aabb=id.getCollisionBoundingBox(e.getEntityWorld(), pos);
			if(aabb!=null)return false;
			return true;
	}

	public static List<AxisAlignedBB> getCollisionListAt(Entity entity,BlockPos pos){
		IBlockState b=entity.world.getBlockState(pos);
		AxisAlignedBB aabb=new AxisAlignedBB(pos.getX(), pos.getY(),pos.getZ(),pos.getX()+1, pos.getY()+1,pos.getZ()+1);
		List<AxisAlignedBB> aabbList=new ArrayList();
		b.addCollisionBoxToList(entity.world, pos, aabb, aabbList, entity,false);
		return aabbList;
	}

	public static  AxisAlignedBB getAABB(Entity e,BlockPos pos){
		double x=pos.getX();double y=pos.getY();double z=pos.getZ();
		IBlockState id=e.world.getBlockState(pos);
		if(MRAUtil.isAir(id))return null;
		if(id.isOpaqueCube()){
			return new AxisAlignedBB(x, y, z, x+1, y+1, z+1);
		}
		List<AxisAlignedBB> list=MRAUtil.getCollisionListAt(e, pos);
		AxisAlignedBB aabb=id.getCollisionBoundingBox(e.getEntityWorld(), pos);
		if(list.isEmpty()) {
			return aabb;
		}else {
			if(aabb==null) {
				aabb=list.get(0);
				double minX=aabb.minX;double minY=aabb.minY;double minZ=aabb.minZ;
				double maxX=aabb.maxX;double maxY=aabb.maxY;double maxZ=aabb.maxZ;
				for(int i=1;i<list.size();i++){
					AxisAlignedBB aabb1=list.get(i);
					if(aabb1.minX<minX)minX=aabb1.minX;
					if(aabb1.minY<minY)minY=aabb1.minY;
					if(aabb1.minZ<minZ)minZ=aabb1.minZ;
					if(aabb1.maxX>maxX)maxX=aabb1.maxX;
					if(aabb1.maxY>maxY)maxY=aabb1.maxY;
					if(aabb1.maxZ>maxZ)maxZ=aabb1.maxZ;

				}
				AxisAlignedBB aabb2=new AxisAlignedBB(minX,minY,minZ,maxX,maxY,maxZ);
				return aabb2;
			}else {
				double minX=aabb.minX;double minY=aabb.minY;double minZ=aabb.minZ;
				double maxX=aabb.maxX;double maxY=aabb.maxY;double maxZ=aabb.maxZ;
				for(int i=0;i<list.size();i++){
					AxisAlignedBB aabb1=list.get(i);
					if(aabb1.minX<minX)minX=aabb1.minX;
					if(aabb1.minY<minY)minY=aabb1.minY;
					if(aabb1.minZ<minZ)minZ=aabb1.minZ;
					if(aabb1.maxX>maxX)maxX=aabb1.maxX;
					if(aabb1.maxY>maxY)maxY=aabb1.maxY;
					if(aabb1.maxZ>maxZ)maxZ=aabb1.maxZ;

				}
				AxisAlignedBB aabb2=new AxisAlignedBB(minX,minY,minZ,maxX,maxY,maxZ);
				return aabb2;
			}
		}
	}

	public static boolean isBlockRidable(Entity par1Entity,BlockPos pos,boolean canSwim,float width){
		World w=par1Entity.world;
		IBlockState id=w.getBlockState(pos);
		BlockPos posabove=pos.add(0, 1, 0);
		IBlockState b=w.getBlockState(posabove);
		if(par1Entity instanceof EntityLivingBase){
			if(b.getBlock().isLadder(b,w,posabove, (EntityLivingBase)par1Entity))return true;
		}
		if(MRAUtil.isAir(id))return false;
		else{
			if(id.isOpaqueCube())return true;

			if(MRAUtil.isLiquid(b))return true;
			if(isBlockPassable(par1Entity, pos))return false;

			AxisAlignedBB aabb=id.getCollisionBoundingBox(par1Entity.world,pos);
			//if(aabb==null)return false;

			if(aabb.maxX+width/2<pos.getX()+1||aabb.minX-width/2>pos.getX()||aabb.maxZ+width/2<pos.getZ()+1||aabb.minZ-width/2>pos.getZ())return false;
			return true;
		}
	}

	public static boolean isBlockPassable(Entity par1Entity,BlockPos pos){
		World w=par1Entity.world;
		IBlockState id=w.getBlockState(pos);
		if(MRAUtil.isAABBNull(par1Entity,pos))return true;
		else if(MRAUtil.isLiquid(id))return true;
		else if(id.getCollisionBoundingBox(par1Entity.world, pos)==null)return true;
		return false;
	}

	public static void sendMessageAll(World w,String s) {
		List<EntityPlayer> players=w.playerEntities;
		for(int i=0;i<players.size();i++){
			players.get(i).sendMessage(new TextComponentString(s));
		}
	}
}
