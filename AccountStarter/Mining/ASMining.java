package scripts.AccountStarter.Mining;

import org.tribot.api.General;
import org.tribot.api.Timing;
import org.tribot.api.types.generic.Condition;
import org.tribot.api.types.generic.Filter;
import org.tribot.api2007.*;
import org.tribot.api2007.ext.Filters;
import org.tribot.api2007.types.RSItem;
import scripts.AccountStarter.Banking.ASBanking;
import scripts.AccountStarter.Variables.ASAreas;
import scripts.AccountStarter.Variables.ASTiles;
import scripts.AccountStarter.Variables.ASVariables;
import scripts.Utils.InventoryUtils;
import scripts.Utils.Misc;
import scripts.Utils.SleepUtils;

/**
 * Created by James on 01/10/2016.
 */
public class ASMining {

    private enum STATE{
        STARTING, MINE_ROCKS, DROPPING, BANKING, WALKING_TO_BANK, WALKING_TO_MINING_AREA, LOST
    }

    private static STATE currentState = STATE.STARTING;

    private static STATE getInitialState(){
        return null;
    }

    public static void main(){

        switch (currentState){

            case STARTING:
                currentState = getInitialState();
                break;

            case MINE_ROCKS:
                handleMining();
                break;

            case DROPPING:
                handleDropping();
                break;

            case BANKING:
                handleBanking();
                break;

            case WALKING_TO_BANK:
                walkToBank();
                break;

            case WALKING_TO_MINING_AREA:
                walkToMiningArea();
                break;

            case LOST:
                handleLost();
                break;
        }
    }

    private static void handleLost() {

    }

    private static void walkToMiningArea() {
        if (!ASAreas.getCombatArea().contains(Player.getPosition())) {
            if (Player.getPosition().getPlane() == 0) {
                WebWalking.walkTo(Misc.getCentreTile(ASAreas.getMiningArea()));
                SleepUtils.waitToStopWalking();
            } else {
                ASBanking.leaveBank();
            }
        } else {
            currentState = STATE.MINE_ROCKS;
        }
    }

    private static void walkToBank() {
        if (ASAreas.getBankArea().contains(Player.getPosition())) {
            currentState = STATE.BANKING;
        } else {
            ASBanking.walkToBank();
        }
    }

    private static void handleBanking() {
        if (axeIsEquipped() && onlyGotAxe()) {
            currentState = STATE.WALKING_TO_MINING_AREA;
        } else {
            if (onlyGotAxe()){
                if (!axeIsEquipped())
                    equipAxe();
            }else{
                if (InventoryUtils.inventIsEmpty()){
                    if (Equipment.getItems().length == 0){
                        if (!InventoryUtils.itemEquippedOrInInvent(ASVariables.getAxeName()))
                            ASBanking.withdraw(ASVariables.getAxeName(), 1);
                    }else{
                        ASBanking.depositEquipment();
                    }
                }else{
                    ASBanking.depositInventory();
                }
            }
        }
    }

    private static void handleDropping() {

    }

    private static void handleMining() {

    }

    private static boolean onlyGotAxeAndLogs(){

        if (!InventoryUtils.itemEquippedOrInInvent(ASVariables.getAxeName()))
            return false;

        RSItem[] nonValidInventItems = Inventory.find(new Filter<RSItem>() {
            @Override
            public boolean accept(RSItem rsItem) {
                return !rsItem.getDefinition().getName().toUpperCase().contains("LOGS") && !rsItem.getDefinition().getName().equals(ASVariables.getAxeName());
            }
        });
        return nonValidInventItems.length == 0;
    }

    private static boolean onlyGotAxe(){

        if (!InventoryUtils.itemEquippedOrInInvent(ASVariables.getAxeName()))
            return false;

        RSItem[] nonAxeInventItems = Inventory.find(Filters.Items.nameNotEquals(ASVariables.getAxeName()));
        if (nonAxeInventItems.length > 0)
            return false;

        RSItem[] nonAxeEquipmentItems = Equipment.find(Filters.Items.nameNotEquals(ASVariables.getAxeName()));
        if (nonAxeEquipmentItems.length > 0)
            return false;

        return true;
    }

    private static void equipAxe(){
        RSItem[] Axe = Inventory.find(ASVariables.getAxeName());

        if (!Banking.isBankScreenOpen()){
            if (Axe.length > 0) {
                if (Axe[0].click("Wield")){
                    Timing.waitCondition(new Condition() {
                        @Override
                        public boolean active() {
                            General.sleep(100, 300);
                            return Equipment.isEquipped(ASVariables.getAxeName());
                        }
                    }, General.random(3000, 5000));
                }
            }
        }else{
            if (Banking.close())
                SleepUtils.waitForBankToClose();
        }
    }

    private static boolean axeIsEquipped(){
        return Equipment.find(ASVariables.getAxeName()).length > 0;
    }

}
