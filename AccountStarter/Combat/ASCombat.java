package scripts.AccountStarter.Combat;

import org.tribot.api.General;
import org.tribot.api.Timing;
import org.tribot.api.types.generic.Condition;
import org.tribot.api.types.generic.Filter;
import org.tribot.api2007.*;
import org.tribot.api2007.ext.Filters;
import org.tribot.api2007.types.RSItem;
import org.tribot.api2007.types.RSObject;
import org.tribot.api2007.types.RSTile;
import scripts.AccountStarter.Banking.ASBanking;
import scripts.AccountStarter.Variables.ASAreas;
import scripts.Utils.Misc;
import scripts.Utils.SleepUtils;

/**
 * Created by James on 23/09/2016.
 */
public class ASCombat {

    private static String swordName = "Bronze sword";
    private static String shieldName = "Wooden shield";
    private static String targetNPCName = "Goblin";

    private enum STATE{
        BANKING, FIGHTING, LOST, END
    }

    private static STATE getCurrentState(){
        if (ASAreas.getCombatArea().contains(Player.getPosition())){
            if (justGotCombatGear()){
                return STATE.FIGHTING;
            }else{
                return STATE.BANKING;
            }
        }else{
            return STATE.LOST;
        }
    }


    public static void main(){

        switch (getCurrentState()){

            case BANKING:
                handleBank();
                break;

            case FIGHTING:
                handleCombat();
                break;

            case LOST:
                handleLost();
                break;

            case END:
                break;
        }
    }

   public static void handleBank(){
       if (Banking.isBankScreenOpen()){
            if (!justGotCombatGear()){
                ASBanking.depositEverything();
                ASBanking.withdraw(swordName, 1);
                ASBanking.withdraw(shieldName, 1);
            }else{
                if (Banking.close()){
                    SleepUtils.waitForBankToClose();
                }
            }
       }else{
           if (justGotCombatGear()){
               if (allGearEquipped()){
                   walkToComabatArea();
               }else{
                   equipGear();
               }
           }else{
               if(Banking.openBank()){
                   SleepUtils.waitForBankToOpen();
               }
           }
       }
   }

    private static void handleBank2(){
        if (!justGotCombatGear()) {
            if (ASAreas.getBankArea().contains(Player.getPosition())) {
                if (Banking.isBankScreenOpen()) {
                    if (Inventory.getAll().length > 0 && Equipment.getItems().length > 0){
                        ASBanking.withdraw(swordName, 1);
                        ASBanking.withdraw(shieldName, 1);
                    }else{
                        ASBanking.depositEverything();
                    }
                } else {
                    if (Banking.openBank())
                        SleepUtils.waitForBankToOpen();
                }
            } else {
                walkToBank();
            }
        }else{
            walkToComabatArea();
        }
    }

    private static void handleCombat(){

    }

    private static void handleLost(){
        if (justGotCombatGear()){
            walkToComabatArea();
        }else{
            walkToBank();
        }
    }


    private static void equipGear(){
        RSItem[] Sword = Inventory.find(swordName);
        RSItem[] Shield = Inventory.find(shieldName);

        if (Sword.length > 0){
            if (Sword[0].click("Wield")){
                Timing.waitCondition(new Condition() {
                    @Override
                    public boolean active() {
                        General.sleep(100, 300);
                        return Equipment.isEquipped(swordName);
                    }
                }, General.random(2000, 5000));
            }
        }
        if (Shield.length > 0){
            if (Shield[0].click("Wear")){
                Timing.waitCondition(new Condition() {
                    @Override
                    public boolean active() {
                        General.sleep(100, 300);
                        return Equipment.isEquipped(shieldName);
                    }
                }, General.random(2000, 5000));
            }
        }

    }

    private static boolean justGotCombatGear(){
        RSItem[] nonCmbInventItems = Inventory.find(new Filter<RSItem>() {
            @Override
            public boolean accept(RSItem rsItem) {
                if (rsItem!= null && rsItem.getDefinition() != null)
                    return !rsItem.getDefinition().getName().equals(swordName) && !rsItem.getDefinition().getName().equals(shieldName);

                return false;
            }
        });

        if (nonCmbInventItems.length > 0)
            return false;

        RSItem[] nonCmbEquippedItems = Inventory.find(new Filter<RSItem>() {
            @Override
            public boolean accept(RSItem rsItem) {
                if (rsItem!= null && rsItem.getDefinition() != null)
                    return !rsItem.getDefinition().getName().equals(swordName) && !rsItem.getDefinition().getName().equals(shieldName);

                return false;
            }
        });

        return nonCmbEquippedItems.length == 0;

    }

    private static boolean gotCombatGear(){
        if (Inventory.find(swordName).length > 0 || Equipment.isEquipped(swordName)){
            if (Inventory.find(shieldName).length > 0 || Equipment.isEquipped(shieldName)){
                return true;
            }
        }
        return false;
    }

    private static boolean allGearEquipped(){
        return Equipment.isEquipped(swordName) && Equipment.isEquipped(shieldName);
    }


    private static void openGoblinHouseDoor(){
        RSObject[] Door = Objects.findNearest(10, new Filter<RSObject>() {
            @Override
            public boolean accept(RSObject rsObject) {
                if (rsObject.getDefinition().getName().equals("Door")) {
                    if (Misc.arrayContainsString(rsObject.getDefinition().getActions(), "Open")) {
                        return true;
                    }
                }
                return false;
            }
        });
        if (Door.length > 0){
                if (Door[0].click("Open")){
                    Timing.waitCondition(new Condition() {
                        @Override
                        public boolean active() {
                            General.sleep(400, 800);
                            return isGoblinHouseDoorOpen();
                        }
                    }, General.random(6000, 9000));
                }
        }else{
            System.out.println("Failed trying to open Goblin House Door, no door found.");
            General.sleep(1000);
        }
    }

    public static boolean isGoblinHouseDoorOpen(){
        RSObject[] openDoor = Objects.getAt(new RSTile(3246, 3243, 0), Filters.Objects.nameEquals("Door"));
        return openDoor.length > 0;
    }

    private static void walkToComabatArea(){
        if (Player.getPosition().getPlane() == 0){
            WebWalking.walkTo(Misc.getCentreTile(ASAreas.getCombatArea()));
            Timing.waitCondition(new Condition() {
                @Override
                public boolean active() {
                    General.sleep(200, 800);
                    return ASAreas.getCombatArea().contains(Player.getPosition());
                }
            },General.random(5000, 8000));
        }else{
            ASBanking.leaveBank();
        }
    }

    private static void walkToBank(){
        if (ASAreas.getGoblinHouseArea().contains(Player.getPosition())){
            if (isGoblinHouseDoorOpen()){
                ASBanking.walkToBank();
            }else{
                openGoblinHouseDoor();
            }
        }else{
            ASBanking.walkToBank();
        }
    }
}
