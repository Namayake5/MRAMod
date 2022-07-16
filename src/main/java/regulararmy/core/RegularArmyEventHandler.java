package regulararmy.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.storage.SaveHandler;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import regulararmy.entity.command.RegularArmyLeader;

public class RegularArmyEventHandler {
	@SubscribeEvent
	public void onEntityJoinWorld(EntityJoinWorldEvent e){
	}

	@SubscribeEvent
	public void onWorldLoaded(WorldEvent.Load e){
		/*
		int binary=e.getWorld().getWorldInfo().getNBTTagCompound().getInteger("leadersListBinary");
		System.out.println("binary="+binary);
		for(int i=0;i<32;i++){
			if((binary&(1<<i))!=0){
				MonsterRegularArmyCore.leaders[i]=new RegularArmyLeader(e.getWorld(),e.getWorld().getWorldInfo().getNBTTagCompound().getCompoundTag("leader"+i),(byte)i);
			}
		}
		*/
		if(e.getWorld().isRemote)return;
		File file2=new File(((SaveHandler) (e.getWorld().getSaveHandler())).getWorldDirectory(),"MRAdata");
			if(!file2.exists()){
				file2.mkdir();
				return;
			}
			File file1=new File(file2,"leaderDIM"+e.getWorld().provider.getDimension()+".dat");
			if(!file1.exists()){
				try {
					file1.createNewFile();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				return;
			}
            FileInputStream fileinputstream;
            NBTTagCompound nbttagcompound=null;
			try {
				fileinputstream = new FileInputStream(file1);
				nbttagcompound = CompressedStreamTools.readCompressed(fileinputstream);
	            fileinputstream.close();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			if(nbttagcompound==null)return;
			if(!nbttagcompound.hasKey("data"))System.out.println("Tag\"data\"does not exist");
            NBTTagCompound nbt=nbttagcompound.getCompoundTag("data");
            if(!nbt.hasKey("leadersListBinary"))System.out.println("Tag\"leadersListBinary\"does not exist");
            int binary=nbt.getInteger("leadersListBinary");
    		//System.out.println("binary="+binary);
    		for(int i=0;i<32;i++){
    			if((binary&(1<<i))!=0){
    				MRACore.leaders[i]=new RegularArmyLeader(e.getWorld(),nbt.getCompoundTag("leader"+i),(byte)i);
    				MRACore.leadersNum=i;
    			}
    		}




	}

	@SubscribeEvent
	public void onWorldSaved(WorldEvent.Save e){
		/*
		int binary=0;
		for(int i=0;i<MonsterRegularArmyCore.leadersNum+1;i++){
			if(MonsterRegularArmyCore.leaders[i]!=null&&MonsterRegularArmyCore.leaders[i].theWorld==e.getWorld()){
				RegularArmyLeader leader=MonsterRegularArmyCore.leaders[i];

				e.getWorld().getWorldInfo().getNBTTagCompound().setCompoundTag("leader"+i, leader.writeToNBT(new NBTTagCompound()));
				binary+=(1<<i);
			}
		}

		e.getWorld().getWorldInfo().getNBTTagCompound().setInteger("leadersListBinary", binary);
		*/
		if(e.getWorld().isRemote)return;
		int binary=0;
		NBTTagCompound nbt1=new NBTTagCompound();
		for(int i=0;i<MRACore.leadersNum+1;i++){
			if(MRACore.leaders[i]!=null&&MRACore.leaders[i].theWorld==e.getWorld()){
				nbt1.setTag("leader"+i, MRACore.leaders[i].writeToNBT(new NBTTagCompound()));
				//MonsterRegularArmyCore.leaders[i]=null;
				binary+=(1<<i);
			}
		}
		//System.out.println("binary="+binary);
		nbt1.setInteger("leadersListBinary", binary);
		NBTTagCompound nbt=new NBTTagCompound();
		nbt.setTag("data", nbt1);

		File file1=new File(((SaveHandler) (e.getWorld().getSaveHandler())).getWorldDirectory(),"MRAdata");

		if(file1!=null){
			try {
				if(!file1.exists()){
					file1.mkdir();
				}
				File file2=new File(file1,"leaderDIM"+e.getWorld().provider.getDimension()+".dat");
				if(file2!=null){
					if(!file2.exists())file2.createNewFile();
					FileOutputStream fis=new FileOutputStream(file2);
					CompressedStreamTools.writeCompressed(nbt, fis);
					fis.close();
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}

	@SubscribeEvent
	public void onWorldUnloaded(WorldEvent.Unload e){
		if(e.getWorld().isRemote)return;
		for(int i=0;i<MRACore.leadersNum+1;i++){
			if(MRACore.leaders[i]!=null&&MRACore.leaders[i].theWorld==e.getWorld()){
				MRACore.leaders[i]=null;
			}
		}
	}

	@SubscribeEvent
	public void tickStart(TickEvent.WorldTickEvent e) {
		if(e.phase!=Phase.START)return;
		for(int i=0;i<MRACore.leadersNum+1;i++){
			if(MRACore.leaders[i]!=null&&MRACore.leaders[i].theWorld==e.world){
				MRACore.leaders[i].onUpdate();
			}
		}
		if(MRACore.leadersNum>-1&&MRACore.leaders[MRACore.leadersNum]==null){
			MRACore.leadersNum--;
		}
	}

}
