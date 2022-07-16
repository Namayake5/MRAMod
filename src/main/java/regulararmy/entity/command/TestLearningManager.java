package regulararmy.entity.command;

import java.util.Map;

import net.minecraft.entity.EntityList;
import net.minecraft.nbt.NBTTagCompound;
import regulararmy.analysis.DataAnalyzer;
import regulararmy.analysis.DataAnalyzerOneToOne;
import regulararmy.analysis.FinderSettings;
import regulararmy.core.MRACore;
import regulararmy.entity.EntityRegularArmy;

public class TestLearningManager extends LearningManagerBase {
	public DataAnalyzer analyzer;
	public FinderSettings setting;
	public DataAnalyzer attackerDanger;
	public DataAnalyzerOneToOne distanceAnalyzer;
	public TestLearningManager(RegularArmyLeader l){
		this.leader=l;
		this.analyzer=new DataAnalyzer();
		this.setting=new FinderSettings();
		this.attackerDanger=new DataAnalyzer();
		this.distanceAnalyzer=new DataAnalyzerOneToOne();
	}

	public TestLearningManager(RegularArmyLeader l,NBTTagCompound nbt){
		this.leader=l;
		this.analyzer=new DataAnalyzer();
		this.setting=new FinderSettings();
		this.readFromNBT(nbt);
	}

	@Override
	public void onStart() {
	}

	@Override
	public void onUpdate() {
		Map<Integer, Integer>[] arrayOfMap=this.analyzer.analyze_average();

	     if (arrayOfMap.length != 0) {

	         this.setting.setblockCostsFromMap(arrayOfMap[0]);
	         this.setting.chunkCost = arrayOfMap[1];
	         this.setting.dangerousSupporter = arrayOfMap[2];

	         if (MRACore.logEntity) {
	           for (Map.Entry<Integer, Integer> e : arrayOfMap[2].entrySet()) {
	             String theName = EntityList.getClassFromID(((Integer)e.getKey()).intValue()).getSimpleName();
	             if (theName == null && ((Integer)e.getKey()).intValue() < 0) {
	               try {
	                 theName = MRACore.entityIDList.get(-((Integer)e.getKey()).intValue() - 1);
	               } catch (Exception exc) {
	                 System.out.println("Unknown id:" + e.getKey() + ";" + e.getValue());
	                 continue;
	               }
	             }
	             if (theName != null) {
	               System.out.println(theName + ";" + e.getValue()); continue;
	             }
	             System.out.println("Unknown id:" + e.getKey() + ";" + e.getValue());
	           }
	         }


	         if (MRACore.logRegion) {
	           for (Map.Entry<Integer, Integer> e : arrayOfMap[1].entrySet()) {
	             int hash = ((Integer)e.getKey()).intValue();
	             int rawx = hash >> 20;
	             int rawy = hash >> 12 & 0xFF;
	             int rawz = hash & 0xFFF;
	             int x = ((rawx & 0x800) == 0) ? (16 * (rawx & 0x7FF) + 8) : (-16 * ((rawx ^ 0xFFFFFFFF) & 0x7FF) - 8);
	             int z = ((rawz & 0x800) == 0) ? (16 * (rawz & 0x7FF) + 8) : (-16 * ((rawz ^ 0xFFFFFFFF) & 0x7FF) - 8);
	             int y = rawy * 16 + 8;
	             System.out.println("Cost around(" + x + "," + y + "," + z + ");" + e.getValue());
	           }
	         }
	       }

		this.leader.endPhase();
	}

	@Override
	public void onEnd() {

	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
			String a="analyzer";
			String s="setting";
			String attacker="attacker";
			String dis="distanceAnalyzer";
			if(!nbt.hasKey(a)){
				this.analyzer=new DataAnalyzer();
				this.setting=new FinderSettings();
			}else{
				this.analyzer=new DataAnalyzer(nbt.getCompoundTag(a));

				this.setting=new FinderSettings(nbt.getCompoundTag(s));

				this.attackerDanger=new DataAnalyzer(nbt.getCompoundTag(attacker));

				this.distanceAnalyzer=new DataAnalyzerOneToOne(nbt.getCompoundTag(dis));
			}

	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
			nbt.setTag("analyzer", this.analyzer.writeToNBT(new NBTTagCompound()));
			nbt.setTag("setting", this.setting.writeToNBT(new NBTTagCompound()));
			nbt.setTag("attacker", this.attackerDanger.writeToNBT(new NBTTagCompound()));
			nbt.setTag("distanceAnalyzer", this.distanceAnalyzer.writeToNBT(new NBTTagCompound()));
		return nbt;
	}

	public DataAnalyzer getAnalyzer(EntityRegularArmy e) {
		return this.analyzer;
	}

	@Override
	public FinderSettings getSettings(EntityRegularArmy e) {
		return this.setting;
	}

	public DataAnalyzer getAttackerDanger(EntityRegularArmy e) {
		return this.attackerDanger;
	}

	public DataAnalyzerOneToOne getDistanceAnalyzer(EntityRegularArmy e){
		return this.distanceAnalyzer;
	}

}
