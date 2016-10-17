package scripts.AccountStarter.Woodcutting;

import org.tribot.api.General;
import org.tribot.api.Timing;
import org.tribot.api.input.Keyboard;
import org.tribot.api.input.Mouse;
import org.tribot.api.types.generic.Condition;
import org.tribot.api2007.*;
import org.tribot.api2007.ext.Filters;
import org.tribot.api2007.types.RSArea;
import org.tribot.api2007.types.RSItem;
import org.tribot.api2007.types.RSObject;
import org.tribot.script.interfaces.MessageListening07;
import scripts.AccountStarter.Banking.ASBanking;
import scripts.AccountStarter.Firemaking.ASFiremaking;
import scripts.AccountStarter.Variables.ASAreas;
import scripts.AccountStarter.Variables.ASVariables;
import scripts.Utils.*;

/**
 * Created by James on 30/09/2016.
 *
 * Things to do:
 * -FINISHED
 */

public class ASWoodcutting implements MessageListening07{

    //region VARIABLES

    private RSArea bestTreeArea;
    private String bestTreeName;
    private boolean runScript = true;

    private int targetWCLevel;
    private int currentWCLevel;

    private int targetFMLevel;
    private int currentFMLevel;

    //endregion

    //region MAIN RUN METHOD & CONSTRUCTORS

    public ASWoodcutting(int inTargetWCLevel, int inTargetFMLevel){
        targetWCLevel = inTargetWCLevel;
        currentWCLevel = Skills.getCurrentLevel(Skills.SKILLS.WOODCUTTING);

        targetFMLevel = inTargetFMLevel;
        currentFMLevel = Skills.getCurrentLevel(Skills.SKILLS.FIREMAKING);
    }

    public void run(){

        while (runScript) {
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
    }

    //endregion



    //region HANDLE CHOPPING

    private void handleChopping(){
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

    private void chopTrees(final String inTreeName, final RSArea inTreeArea){


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

    //endregion

    //region HANDLE FIREMAKING

    private void handleFiremaking(){
        RSItem[] allLogs = Inventory.find(Filters.Items.nameContains("logs"));
        if (allLogs.length > 0){
            if (allLogs[0].getDefinition().getName().equals("Oak logs")){
                if (currentFMLevel >= 15){
                    ASFiremaking.lightLogs("Oak logs");
                }else{
                    if (allLogs[0].click("Drop")){
                        InventoryUtils.waitForInventChange();
                    }
                }
            }else if (allLogs[0].getDefinition().getName().equals("Logs")){
                ASFiremaking.lightLogs("Logs");
            }else{
                if (allLogs[0].click("Drop")){
                    InventoryUtils.waitForInventChange();
                }
            }
        }else{
            currentState = STATE.CHOP_TREES;
        }
    }

    //endregion

    //region HANDLE BANKING

    private void handleBank() {
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
                        if (Inventory.find("Tinderbox").length == 0)
                            ASBanking.withdraw("Tinderbox", 1);
                    }else{
                        ASBanking.depositEquipment();
                    }
                }else{
                    ASBanking.depositInventory();
                }
            }
        }

    }

    //endregion

    //region HANDLE WALKING

    private void handleLost() {
        if (onlyGotAxeAndLogs())
            currentState = STATE.WALKING_TO_TREE_AREA;
        else
            currentState = STATE.WALKING_TO_BANK;
    }

    private void walkToTreeArea(RSArea inArea) {
        if (!inArea.contains(Player.getPosition())){
            if (Player.getPosition().getPlane() == 0){
                WebWalking.walkTo(MiscUtils.getCentreTile(inArea));
                WalkingUtils.waitToStopWalking();
            }else{
                ASBanking.leaveBank();
            }
        }else{
            currentState = STATE.CHOP_TREES;
        }
    }

    private void walkToBank() {
        if (ASAreas.getBankArea().contains(Player.getPosition()))
            currentState = STATE.BANKING;
        else
            ASBanking.walkToBank();
    }

    //endregion


    //region MISC METHODS

    private boolean onlyGotAxeAndLogs(){
        return InventoryUtils.onlyGotItems(new String[]{"Logs", "Oak logs", ASVariables.getAxeName(), "Tinderbox"});
    }

    private boolean onlyGotAxe(){
       return InventoryUtils.onlyGotItems(new String[]{ASVariables.getAxeName()});
    }

    private void equipAxe(){
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
                BankingUtils.waitForBankToClose();
        }
    }

    private boolean axeIsEquipped(){
        return Equipment.find(ASVariables.getAxeName()).length > 0;
    }

    //endregion

    //region STATE HANDLING

    private enum STATE{
        STARTING, CHOP_TREES, FIREMAKING, BANKING, WALKING_TO_BANK, WALKING_TO_TREE_AREA, LOST
    }

    private STATE currentState = STATE.STARTING;
    private STATE getInitialState(){
        if (currentWCLevel >= 15 && currentFMLevel > 15){
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

    //endregion


    //region MESSAGE LISTENING

    @Override
    public void serverMessageReceived(String s) {
        if (s.contains("Congratulations, you just advanced a")){
            ChatUtils.waitForChatInterface();
            while(ChatUtils.interfaceCoveringChat()){
                Keyboard.pressKeys(Keyboard.getKeyCode(' '));
                General.sleep(800, 1500);
            }

            currentFMLevel = Skills.getCurrentLevel(Skills.SKILLS.FIREMAKING);
            currentWCLevel = Skills.getCurrentLevel(Skills.SKILLS.WOODCUTTING);

            if (currentFMLevel >= targetFMLevel && currentWCLevel >= targetWCLevel)
                runScript = false;

            if (currentWCLevel >= 15 && currentFMLevel > 15){
                bestTreeName = "Oak";
                bestTreeArea = ASAreas.getOakTreesArea();
            }else{
                bestTreeName = "Tree";
                bestTreeArea = ASAreas.getNormalTreesArea();
            }

        }
    }

    @Override
    public void playerMessageReceived(String s, String s1) {

    }

    @Override
    public void duelRequestReceived(String s, String s1) {

    }

    @Override
    public void clanMessageReceived(String s, String s1) {

    }

    @Override
    public void tradeRequestReceived(String s) {

    }

    @Override
    public void personalMessageReceived(String s, String s1) {

    }

    //endregion

}
