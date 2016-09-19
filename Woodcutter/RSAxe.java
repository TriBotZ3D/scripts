package scripts.Woodcutter;


import org.tribot.api.General;
import org.tribot.api.Timing;
import org.tribot.api.types.generic.Condition;
import org.tribot.api2007.Equipment;
import org.tribot.api2007.GameTab;
import org.tribot.api2007.Inventory;
import org.tribot.api2007.Skills;
import org.tribot.api2007.ext.Filters;
import org.tribot.api2007.types.RSItem;

public class RSAxe{

    private String _Name;
    private int _woodcuttingLvlReq;
    private int _attackLvlReq;

    public RSAxe(String inName, int wcReq, int attReq){
        _Name = inName;
        _woodcuttingLvlReq = wcReq;
        _attackLvlReq = attReq;
    }

    public String Name(){
        return _Name;
    }

    public int woodcuttingLvlReq(){
        return _woodcuttingLvlReq;
    }

    public int attackLvlReq(){
        return _attackLvlReq;
    }

    public boolean canWieldAxe(){
        return Skills.getActualLevel(Skills.SKILLS.ATTACK) >= _attackLvlReq;
    }

    public boolean canUseAxe(){
        return Skills.getActualLevel(Skills.SKILLS.WOODCUTTING) >= _woodcuttingLvlReq;
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






    public static final RSAxe[] allAxes = {
            new RSAxe("Bronze axe", 1, 1),
            new RSAxe("Iron axe", 1, 1),
            new RSAxe("Steel axe", 6, 5),
            new RSAxe("Black axe", 11, 10),
            new RSAxe("Mithril axe", 21, 20),
            new RSAxe("Adamant axe", 31, 30),
            new RSAxe("Rune axe", 41, 40),
            new RSAxe("Dragon axe", 61, 60),
            new RSAxe("Infernal axe", 61, 60)};


    public static boolean inventContainsAxe(){
        return Inventory.find(Filters.Items.nameContains(" axe")).length > 0;
    }

    public static boolean gotAxe(){
        return  Inventory.find(Filters.Items.nameContains(" axe")).length > 0 || Equipment.isEquipped(Filters.Items.nameContains(" axe"));
    }

    public static RSAxe getCurrentAxe(){
        RSItem[] inventAxe = Inventory.find(Filters.Items.nameContains(" axe"));
        RSItem[] equippedAxe = Equipment.find(Filters.Items.nameContains(" axe"));

        if (inventAxe.length > 0){
            return getAxeFromName(inventAxe[0].getDefinition().getName());
        }else if (equippedAxe.length > 0){
            return getAxeFromName(equippedAxe[0].getDefinition().getName());
        }
            return null;
    }

    public static RSAxe getAxeFromName(String inName){
        for (int i = 0; i < allAxes.length; i++){
            if (allAxes[i].Name().toLowerCase().equals(inName.toLowerCase())){
                return allAxes[i];
            }
        }
        return null;
    }

    public static RSAxe getBestAxe(RSAxe[] inAxes){

        if (inAxes.length > 0) {

            RSAxe bestAxe = inAxes[0];

            for (int i = 0; i < inAxes.length; i++) {
                if (inAxes[i].canUseAxe()){
                    if (inAxes[i].woodcuttingLvlReq() > bestAxe.woodcuttingLvlReq()){
                        bestAxe = inAxes[i];
                    }
                }
            }

            return bestAxe;
        }
        return null;
    }


}
