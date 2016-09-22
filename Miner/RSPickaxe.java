package scripts.Miner;


import org.tribot.api.General;
import org.tribot.api.Timing;
import org.tribot.api.types.generic.Condition;
import org.tribot.api2007.Equipment;
import org.tribot.api2007.GameTab;
import org.tribot.api2007.Inventory;
import org.tribot.api2007.Skills;
import org.tribot.api2007.ext.Filters;
import org.tribot.api2007.types.RSItem;

public class RSPickaxe {

    private String _Name;
    private int _miningLvlReq;
    private int _attackLvlReq;

    public RSPickaxe(String inName, int miningReq, int attReq){
        _Name = inName;
        _miningLvlReq = miningReq;
        _attackLvlReq = attReq;
    }

    public String Name(){
        return _Name;
    }

    public int miningLvlReq(){
        return _miningLvlReq;
    }

    public int attackLvlReq(){
        return _attackLvlReq;
    }

    public boolean canWieldPickaxe(){
        return Skills.getActualLevel(Skills.SKILLS.ATTACK) >= _attackLvlReq;
    }

    public boolean canUsePickaxe(){
        return Skills.getActualLevel(Skills.SKILLS.MINING) >= _miningLvlReq;
    }

    public boolean isEquipped(){
        return Equipment.isEquipped(_Name);
    }

    public void Equip(){
        if (GameTab.getOpen() == GameTab.TABS.INVENTORY) {
            Inventory.find(_Name)[0].click("Wield");
            Timing.waitCondition(new Condition() {
                @Override
                public boolean active() {
                    General.sleep(100, 300);
                    return Inventory.find(_Name).length == 0;
                }
            }, General.random(3000, 4000));
        }else{
            //Invent not open, open invent and wait for it to open
            GameTab.open(GameTab.TABS.INVENTORY);
            Timing.waitCondition(new Condition() {
                @Override
                public boolean active() {
                    General.sleep(100, 400);
                    return GameTab.getOpen() == GameTab.TABS.INVENTORY;
                }
            }, General.random(3000, 5000));
        }
    }

    public void Unequip(){
        if (GameTab.getOpen() == GameTab.TABS.EQUIPMENT){
            Equipment.find(_Name)[0].click("Remove");
            Timing.waitCondition(new Condition() {
                @Override
                public boolean active() {
                    General.sleep(100, 300);
                    return Equipment.find(_Name).length == 0;
                }
            }, General.random(3000, 4000));
        }else{
            GameTab.open(GameTab.TABS.EQUIPMENT);
            Timing.waitCondition(new Condition() {
                @Override
                public boolean active() {
                    General.sleep(100, 400);
                    return GameTab.getOpen() == GameTab.TABS.EQUIPMENT;
                }
            }, General.random(3000, 5000));
        }
    }






    public static final RSPickaxe[] allPickaxes = {
            new RSPickaxe("Bronze pickaxe", 1, 1),
            new RSPickaxe("Iron pickaxe", 1, 1),
            new RSPickaxe("Steel pickaxe", 6, 5),
            new RSPickaxe("Black pickaxe", 11, 10),
            new RSPickaxe("Mithril pickaxe", 21, 20),
            new RSPickaxe("Adamant pickaxe", 31, 30),
            new RSPickaxe("Rune pickaxe", 41, 40),
            new RSPickaxe("Dragon pickaxe", 61, 60),
            new RSPickaxe("Infernal pickaxe", 61, 60)};


    public static boolean inventContainsPickaxe(){
        return Inventory.find(Filters.Items.nameContains(" pickaxe")).length > 0;
    }

    public static boolean gotPickaxe(){
        return  Inventory.find(Filters.Items.nameContains(" pickaxe")).length > 0 || Equipment.isEquipped(Filters.Items.nameContains(" pickaxe"));
    }

    public static RSPickaxe getCurrentPickaxe(){
        RSItem[] inventPickaxe = Inventory.find(Filters.Items.nameContains(" pickaxe"));
        RSItem[] equippedPickaxe = Equipment.find(Filters.Items.nameContains(" pickaxe"));

        if (inventPickaxe.length > 0){
            return getPickaxeFromName(inventPickaxe[0].getDefinition().getName());
        }else if (equippedPickaxe.length > 0){
            return getPickaxeFromName(equippedPickaxe[0].getDefinition().getName());
        }
            return null;
    }

    public static RSPickaxe getPickaxeFromName(String inName){
        for (int i = 0; i < allPickaxes.length; i++){
            if (allPickaxes[i].Name().toLowerCase().equals(inName.toLowerCase())){
                return allPickaxes[i];
            }
        }
        return null;
    }

    public static RSPickaxe getBestPickaxe(RSPickaxe[] inPickaxes){

        if (inPickaxes.length > 0) {

            RSPickaxe bestPickaxe = inPickaxes[0];

            for (int i = 0; i < inPickaxes.length; i++) {
                if (inPickaxes[i].canUsePickaxe()){
                    if (inPickaxes[i].miningLvlReq() > bestPickaxe.miningLvlReq()){
                        bestPickaxe = inPickaxes[i];
                    }
                }
            }

            return bestPickaxe;
        }
        return null;
    }


}
