package regulararmy.analysis;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;

public class FinderSettings {
	public int[] blocksCost=new int[4096];
	   public Map<Integer, Integer> dangerousSupporter = new HashMap<Integer, Integer>();
	public Map<Integer,Integer> chunkCost=new HashMap();
	public Map<Integer,Float> crowdCost=new HashMap();

	public FinderSettings(){

	}

	public FinderSettings(NBTTagCompound nbt){
		this.readFromNBT(nbt);
	}

	public void setblockCostsFromMap(Map<Integer,Integer> map){
		Map.Entry<Integer,Integer>[] entries=map.entrySet().toArray(new Map.Entry[0]);
		int value=0;
		for(int i=0;i<entries.length;i++){
			value+=entries[i].getValue();
		}
		value/=entries.length;
		for(int i=0;i<entries.length;i++){
			this.blocksCost[entries[i].getKey()]=entries[i].getValue()>value?entries[i].getValue()-value:0;
			System.out.println(Block.getBlockById(entries[i].getKey()).getUnlocalizedName()+"'s cost: "+this.blocksCost[entries[i].getKey()]);
		}
	}

	public int getTotalBlocksCost(int[] ids){
		int cost=0;
	    int[] idAppeared = new int[ids.length];
		for(int i=0;i<ids.length;i++){
			if(ids[i]!=0){
				for (int j = 0; j < idAppeared.length &&
						idAppeared[j] != ids[i]; j++) {


					if (idAppeared[j] == 0) {
						cost += (this.blocksCost[ids[i]] < 0) ? 0 : this.blocksCost[ids[i]];
						idAppeared[j] = ids[i];
					}
				}
			}
		}
		return cost;
	}

	public void addCrowdCost(Map<Integer,Float> map){
		for(Entry<Integer,Float> entry:map.entrySet()){
			if(this.crowdCost.containsKey(entry.getKey())){
				this.crowdCost.put(entry.getKey(), this.crowdCost.get(entry.getKey())+entry.getValue());
				//System.out.println("chunk:"+entry.getKey()+"'s new cost is"+this.crowdCost.get(entry.getKey()));
			}else{
				this.crowdCost.put(entry.getKey(), entry.getValue());
				//System.out.println("chunk:"+entry.getKey()+"'s cost is"+this.crowdCost.get(entry.getKey()));
			}
		}
	}

	public void removeCrowdCost(Map<Integer,Float> map){
		for(Entry<Integer,Float> entry:map.entrySet()){
			if(this.crowdCost.containsKey(entry.getKey())){
				float value= this.crowdCost.get(entry.getKey())-entry.getValue();
				if(value<0.1f){
					this.crowdCost.remove(entry.getKey());
				}else{
					this.crowdCost.put(entry.getKey(),value);
				}
			}
		}
	}

	public NBTTagCompound writeToNBT(NBTTagCompound nbt){
		nbt.setIntArray("blocksCost", this.blocksCost);
	    Integer[] supporterNameArray = (Integer[])this.dangerousSupporter.keySet().toArray((Object[])new Integer[0]);
	     Integer[] supporterCostArray = (Integer[])this.dangerousSupporter.values().toArray((Object[])new Integer[0]);
	     for (int i = 0; i < supporterNameArray.length; i++) {

	         nbt.setInteger("supporterName" + i, supporterNameArray[i].intValue());
	         nbt.setInteger("supporterCost" + i, supporterCostArray[i].intValue());
	       }

		Integer[] chunkHashArray=this.chunkCost.keySet().toArray(new Integer[0]);
		Integer[] chunkCostArray=this.chunkCost.values().toArray(new Integer[0]);
		for(int i=0;i<chunkHashArray.length;i++){
			nbt.setInteger("chunkHash"+i, chunkHashArray[i]);
			nbt.setInteger("chunkCost"+i, chunkCostArray[i]);
		}

		return nbt;
	}

	public void readFromNBT(NBTTagCompound nbt){
		this.blocksCost=nbt.getIntArray("blocksCost");
	     int i;


	     for (i = 0; nbt.hasKey("supporterName" + i); i++) {
	       Integer id = Integer.valueOf(nbt.getInteger("supporterName" + i));

	       this.dangerousSupporter.put(id, Integer.valueOf(nbt.getInteger("supporterCost" + i)));
	     }
		for(i=0;nbt.hasKey("chunkHash"+i);i++){
			this.chunkCost.put(nbt.getInteger("chunkHash"+i), nbt.getInteger("chunkCost"+i));
		}
	}
}
