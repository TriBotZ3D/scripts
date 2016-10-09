package scripts.AccountStarter.Woodcutting;

import org.tribot.api.General;
import org.tribot.api.Timing;
import org.tribot.api.input.Keyboard;
import org.tribot.api.input.Mouse;
import org.tribot.api.types.generic.Condition;
import org.tribot.api.types.generic.Filter;
import org.tribot.api2007.*;
import org.tribot.api2007.ext.Filters;
import org.tribot.api2007.types.RSArea;
import org.tribot.api2007.types.RSItem;
import org.tribot.api2007.types.RSObject;
import scripts.AccountStarter.Banking.ASBanking;
import scripts.AccountStarter.Firemaking.ASFiremaking;
import scripts.AccountStarter.Variables.ASAreas;
import scripts.AccountStarter.Variables.ASVariables;
import scripts.Utils.Dropping;
import scripts.Utils.InventoryUtils;
import scripts.Utils.Misc;
import scripts.Utils.SleepUtils;

/**
 * Created by James on 30/09/2016.
 */
public class ASWoodcutting{

    private enum STATE{
        STARTING, CHOP_TREES, FIREMAKING, BANKING, WALKING_TO_BANK, WALKING_TO_TREE_AREA, LOST
    }

    private static STATE currentState = STATE.STARTING;

    private static STATE getInitialState(){
        if (Skills.getActualLevel(Skills.SKILLS.WOODCUTTING) >= 15){
            bestTreeName = "Oak";
            bestTreeArea = ASAreas.getOakTreesArea();
        }else{
            bestTreeName = "Tree";
            bestTreeArea = ASAreas.getNormalTreesArea();
        }

        if (ASAreas.getNormalTreesArea().contains(Player.getPosition()) || ASAreas.getOakTreesArea().contains(Player.getPosition()))
            return STATE.CHOP_TREES;
        else if (ASAreas.getBankArea().contains(Player.getPosition()))
            return STATE.BANKING;
        else
            return STATE.LOST;
    }

    private static RSArea bestTreeArea;
    private static String bestTreeName;
    public static void main(){

        switch (currentState) {

            case STARTING:
                currentState = getInitialState();
                break;

            case CHOP_TREES:
                handleChopping();
                break;

            case FIREMAKING:
                handleFiremaking();
                break;

            case BANKING:
                handleBank();
                break;

            case WALKING_TO_BANK:
                walkToBank();
                break;

            case WALKING_TO_TREE_AREA:
                walkToTreeArea(bestTreeArea);
                break;

            case LOST:
                handleLost();
                break;
        }
    }

    private static void handleChopping(){
        if (Skills.getActualLevel(Skills.SKILLS.WOODCUTTING) >= 15){
            bestTreeName = "Oak";
            bestTreeArea = ASAreas.getOakTreesArea();
        }else{
            bestTreeName = "Tree";
            bestTreeArea = ASAreas.getNormalTreesArea();
        }
        if (!Inventory.isFull()){
            if (onlyGotAxeAndLogs()){
                if (axeIsEquipped()){
                    chopTrees(bestTreeName, bestTreeArea);
                }else{
                    equipAxe();
                }
            }else{
                currentState = STATE.WALKING_TO_BANK;
            }
        }else
            currentState = STATE.FIREMAKING;
    }

    private static void handleFiremaking(){
        RSItem[] allLogs = Inventory.find(Filters.Items.nameContains("logs"));
        if (allLogs.length > 0){
            if (allLogs[0].getDefinition().getName().equals("Oak logs")){
                if (Skills.getActualLevel(Skills.SKILLS.FIREMAKING) >= 15){
                    ASFiremaking.lightLogs("Oak logs");
                }else{
                    if (allLogs[0].click("Drop")){
                        SleepUtils.waitForInventChange();
                    }
                }
            }else if (allLogs[0].getDefinition().getName().equals("Logs")){
                ASFiremaking.lightLogs("Logs");
            }else{
                if (allLogs[0].click("Drop")){
                    SleepUtils.waitForInventChange();
                }
            }
        }else{
            currentState = STATE.CHOP_TREES;
        }
    }

    private static void handleLost() {
        if (onlyGotAxeAndLogs())
            currentState = STATE.WALKING_TO_TREE_AREA;
        else
            currentState = STATE.WALKING_TO_BANK;
    }

    private static void walkToTreeArea(RSArea inArea) {
        if (!inArea.contains(Player.getPosition())){
            if (Player.getPosition().getPlane() == 0){
                WebWalking.walkTo(Misc.getCentreTile(inArea));
                SleepUtils.waitToStopWalking();
            }else{
                ASBanking.leaveBank();
            }
        }else{
            currentState = STATE.CHOP_TREES;
        }
    }

    private static void walkToBank() {
        if (ASAreas.getBankArea().contains(Player.getPosition()))
            currentState = STATE.BANKING;
        else
            ASBanking.walkToBank();
    }

    private static void handleBank() {
        if (axeIsEquipped() && onlyGotAxe()) {
            currentState = STATE.WALKING_TO_TREE_AREA;
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

    private static void chopTrees(final String inTreeName, final RSArea inTreeArea){


        //Search for all trees that have the desired name and are within the area and have a 'Chop down' action
        final RSObject[] allValidTrees = Objects.findNearest(20, Filters.Objects.nameEquals(inTreeName).combine(Filters.Objects.inArea(inTreeArea).combine(Filters.Objects.actionsContains("Chop down"), true), true));

        //If we're idle at the moment
        if (Player.getAnimation() == -1) {
            //If some trees exist that fit the criteria
            if (allValidTrees.length > 0) {
                if (allValidTrees[0].isOnScreen()) {
                    if (allValidTrees[0].click("Chop down")) {
                        //If we clicked the chop down option, wait until we start chopping
                        Timing.waitCondition(new Condition() {
                            @Override
                            public boolean active() {
                                General.sleep(100, 300);
                                return Player.getAnimation() != -1 || Interfaces.get(233) != null;
                            }
                        }, General.random(5000, 8000));
                        if (Interfaces.get(233) != null) {
                            General.sleep(600, 1500);
                            Keyboard.pressKeys(Keyboard.getKeyCode(' '));
                            General.sleep(400, 1200);
                            Timing.waitCondition(new Condition() {
                                @Override
                                public boolean active() {
                                    General.sleep(100, 300);
                                    return Interfaces.get(233) == null;
                                }
                            }, General.random(2000, 3000));
                        }
                    }

                } else {
                    if (Camera.getCameraAngle() < 100) {
                        Camera.setRotationMethod(Camera.ROTATION_METHOD.ONLY_KEYS);
                        Camera.setCameraAngle(100);
                        Camera.setRotationMethod(Camera.ROTATION_METHOD.ONLY_MOUSE);
                    }
                    //Blind walk to the tree if not clickable
                    if (!Game.isRunOn() && Game.getRunEnergy() > 44)
                        Options.setRunOn(true);
                    Walking.blindWalkTo(allValidTrees[0].getPosition());

                    //Wait until the tree is clickable or we stop moving
                    Timing.waitCondition(new Condition() {
                        @Override
                        public boolean active() {
                            General.sleep(100, 400);
                            return allValidTrees[0].isOnScreen() || !Player.isMoving();
                        }
                    }, General.random(20000, 40000));
                }
            } else {
                Timing.waitCondition(new Condition() {
                    @Override
                    public boolean active() {
                        General.sleep(800, 1200);
                        return Objects.findNearest(50, Filters.Objects.nameEquals(inTreeName).combine(Filters.Objects.inArea(inTreeArea).combine(Filters.Objects.actionsContains("Chop down"), true), true)).length > 0;
                    }
                }, General.random(60000, 150000));
            }
        } else {
            if (InventoryUtils.freeInventSlots() == 1) {
                //If the next log will fill the invent hover over the inventory
                Mouse.moveBox(InventoryUtils.allInventSlots[0]);
            } else {
                if (allValidTrees.length > 1 && allValidTrees[1].isOnScreen()) {
                    //If there is another tree and it is on the screen, hover the mouse over it
                    allValidTrees[1].hover();
                }
            }

            //Wait for the player to stop chopping the tree or wait for them to have a full invent
            Timing.waitCondition(new Condition() {
                @Override
                public boolean active() {
                    General.sleep(100, 600);
                    //Change player animation from '-1' to specific chopping animations
                    //Maybe add extra antiban here?
                    return Player.getAnimation() == -1 || Inventory.isFull();
                }
            }, General.random(60000, 200000)); //1 - ~3mins
        }
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
