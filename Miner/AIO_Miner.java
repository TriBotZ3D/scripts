package scripts.Miner;

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
import org.tribot.api2007.types.RSTile;
import org.tribot.script.Script;
import org.tribot.script.ScriptManifest;
import scripts.Utils.*;

import java.util.ArrayList;

/**
 * Created by James on 14/09/2016.
 */

@ScriptManifest(authors = {"Z3D"}, name = "AIO Miner", category = "Mining")
public class AIO_Miner extends Script {

    public static int[] rockIDs;
    public static RSArea rockArea;

    public static RSArea bankArea = new RSArea(new RSTile(0,0,0), new RSTile(0,0,0));
    public static RSTile bankTile = new RSTile(0,0,0);

    public static boolean bankOre;
    public static boolean autoUpgradePickaxe;
    public static boolean hoverOverLastMined = true;

    private RSPickaxe[] allPickaxesOwned;
    private boolean upgradingPickaxe = false;

    @Override
    public void run() {

        System.out.println("Starting Z3D's AIO Miner");

        GUI _GUI = new GUI();

        _GUI.setLocationRelativeTo(null);
        _GUI.setVisible(true);

        while (_GUI.isVisible())
            General.sleep(200);

        if (_GUI.guiCompleted) {

            General.useAntiBanCompliance(true);
            Camera.setRotationMethod(Camera.ROTATION_METHOD.ONLY_MOUSE);

            Mouse.setSpeed(81);

            if (Login.getLoginState() == Login.STATE.INGAME){
                if (!Game.isRunOn())
                    Options.setRunOn(true);
            }

            while (true) {

                switch (updateState()) {

                    case MINE_ROCKS:
                        mineRocks(rockIDs, rockArea);
                        break;

                    case BANKING:
                        handleBank(rockArea, bankArea);
                        break;

                    case UPGRADING_PICK:
                        handlePickaxeUpgrade(bankArea);
                        break;

                    case DROP_ORE:
                        dropOre();
                        break;

                    case LOST:
                        handleLost(rockArea);
                        break;
                }
            }
        }else{
            System.out.println("GUI Closed!");
        }
    }


    private void handleBank(final RSArea inRockArea, final RSArea inBankArea){

        if (inBankArea.contains(Player.getPosition())){
            if (needToBank()){
                if (Banking.isBankScreenOpen()){

                    //Will load all pickaxes the player has in there bank only once
                    if (allPickaxesOwned == null && autoUpgradePickaxe) {
                        findAllAxes();
                        if (allPickaxesOwned.length > 0)
                            startSecondThread();
                    }

                    //If we don't have an pickaxe in out invent deposit all
                    if (!RSPickaxe.inventContainsPickaxe()){
                        Banking.depositAll();
                        Timing.waitCondition(new Condition() {
                            @Override
                            public boolean active() {
                                General.sleep(50, 300);
                                return Inventory.getAll().length == 0;
                            }
                        }, General.random(3000, 5000));
                    }else{
                        //Otherwise deposit all but the pickaxe
                        final int itemsInInvent = Inventory.getAll().length;
                        Banking.depositAllExcept(Inventory.find(Filters.Items.nameContains(" pickaxe"))[0].getID());
                        Timing.waitCondition(new Condition() {
                            @Override
                            public boolean active() {
                                General.sleep(100, 400);
                                return Inventory.getAll().length < itemsInInvent;
                            }
                        }, General.random(4000, 7000));
                    }

                }else{
                    //Open bank screen if not already open, then wait for it to appear
                    if (Banking.openBank()){
                        SleepUtils.waitForBankToOpen();
                    }
                }
            }else{
                //Walk back to rocks
                if (!Game.isRunOn() && Game.getRunEnergy() > 44)
                    Options.setRunOn(true);
                PathFinding.aStarWalk(Misc.getCentreTile(inRockArea));
                Timing.waitCondition(new Condition() {
                    @Override
                    public boolean active() {
                        General.sleep(400, 700);
                        return !Player.isMoving();
                    }
                }, General.random(10000, 20000));
            }
        }else{
            //Walk to bank then wait until we stop or enter a bank
            if (!Game.isRunOn() && Game.getRunEnergy() > 44)
                Options.setRunOn(true);
            PathFinding.aStarWalk(bankTile);
            Timing.waitCondition(new Condition() {
                @Override
                public boolean active() {
                    General.sleep(200, 500);
                    return !Player.isMoving() || inBankArea.contains(Player.getPosition());
                }
            }, General.random(10000, 20000));
        }
    }

    private boolean needToBank(){

        RSItem[] nonAxeItems = Inventory.find(new Filter<RSItem>() {
            @Override
            public boolean accept(RSItem rsItem) {
                return !rsItem.getDefinition().getName().contains(" pickaxe");
            }
        });

        //Either we don't have an pickaxe or we have items that aren't a pickaxe
        return nonAxeItems.length > 0 || !RSPickaxe.gotPickaxe();
    }

    private void handleLost(RSArea inRocksArea){
        System.out.println("Oh no, we seem to be a little lost. Lets try and find out way back.");
        if (!Game.isRunOn() && Game.getRunEnergy() > 44)
            Options.setRunOn(true);
        PathFinding.aStarWalk(Misc.getCentreTile(inRocksArea));
        Timing.waitCondition(new Condition() {
            @Override
            public boolean active() {
                General.sleep(200, 500);
                return !Player.isMoving();
            }
        }, General.random(10000, 20000));
    }

    private void dropOre(){

        RSItem[] pickaxeInInvent = Inventory.find(Filters.Items.nameContains(" pickaxe"));

        //If we have an pickaxe in your inventory and its not in the bottom right, move it
        if (pickaxeInInvent.length > 0 && pickaxeInInvent[0].getIndex() != 27){
            System.out.println("Oi you fucking moron! Why is your pickaxe not in the bottom right!?!");
            System.out.println("Do I have to do everything around here...");
            InventoryUtils.moveItemToIndex(pickaxeInInvent[0], 27);
        }else {
            Dropping.fastDrop(27);
        }
    }

    private RSTile lastMinedTile = null;
    private void mineRocks(final int[] inRockIDs, final RSArea inRockArea){

        //Search for all trees that have the desired name and are within the area and have a 'Chop down' action
        final RSObject[] allValidRocks = Objects.findNearest(20, new Filter<RSObject>() {
            @Override
            public boolean accept(RSObject rsObject) {
                if (inRockArea.contains(rsObject.getPosition())) {
                    for (int i = 0; i < inRockIDs.length; i++) {
                        if (rsObject.getID() == inRockIDs[i])
                            return true;
                    }
                }
                return false;
            }
        });

            //If we're idle at the moment
            if (Player.getAnimation() == -1) {

                //If some trees exist that fit the criteria
                if (allValidRocks.length > 0) {
                    if (allValidRocks[0].isOnScreen() && allValidRocks[0].isClickable()) {
                        if (allValidRocks[0].click("Mine")) {
                            //If we clicked the Mine option, wait until we start mining
                            Timing.waitCondition(new Condition() {
                                @Override
                                public boolean active() {
                                    General.sleep(100, 300);
                                    return Player.getAnimation() != -1 || Interfaces.get(233) != null;
                                }
                            }, General.random(2000, 4000));
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
                        if (Camera.getCameraAngle() < 100){
                            Camera.setRotationMethod(Camera.ROTATION_METHOD.ONLY_KEYS);
                            Camera.setCameraAngle(100);
                            Camera.setRotationMethod(Camera.ROTATION_METHOD.ONLY_MOUSE);
                        }
                        //Blind walk to the rock if not clickable
                        if (!Game.isRunOn() && Game.getRunEnergy() > 44)
                            Options.setRunOn(true);
                        Walking.blindWalkTo(allValidRocks[0].getPosition());

                        //Wait until the rock is clickable or we stop moving
                        Timing.waitCondition(new Condition() {
                            @Override
                            public boolean active() {
                                General.sleep(100, 400);
                                return allValidRocks[0].isClickable() || !Player.isMoving();
                            }
                        }, General.random(20000, 40000));
                    }
                } else {
                    System.out.println("No Rocks found!");
                    Timing.waitCondition(new Condition() {
                        @Override
                        public boolean active() {
                            General.sleep(800, 1200);
                            return Objects.findNearest(50, new Filter<RSObject>() {
                                @Override
                                public boolean accept(RSObject rsObject) {
                                    if (inRockArea.contains(rsObject.getPosition())) {
                                        for (int i = 0; i < inRockIDs.length; i++) {
                                            if (rsObject.getID() == inRockIDs[i])
                                                return true;
                                        }
                                    }
                                    return false;
                                }
                            }).length > 0;
                        }
                    }, General.random(60000, 150000));
                }
            } else {
                if (InventoryUtils.freeInventSlots() == 1 && !bankOre) {
                    //If the next ore will fill the invent hover over the inventory
                    Mouse.moveBox(InventoryUtils.allInventSlots[0]);
                } else {
                    if (hoverOverLastMined && allValidRocks.length > 0){
                        if (lastMinedTile != null)
                                lastMinedTile.hover();
                        lastMinedTile = allValidRocks[0].getPosition();
                    }else {
                        if (allValidRocks.length > 1 && allValidRocks[1].isOnScreen()) {
                            //If there is another rock and it is on the screen, hover the mouse over it
                            allValidRocks[1].hover();
                        }
                    }
                }

                //Wait for the player to stop mining the rock or wait for them to have a full invent
                Timing.waitCondition(new Condition() {
                    @Override
                    public boolean active() {
                        General.sleep(300,500);
                        //Change player animation from '-1' to specific mining animations
                        //Maybe add extra antiban here?
                        return Player.getAnimation() == -1 || Inventory.isFull();
                    }
                }, General.random(60000, 200000)); //1 - ~3mins
            }
    }

    private void handlePickaxeUpgrade(final RSArea inBankArea){

        final RSPickaxe currentPick = RSPickaxe.getCurrentPickaxe();
        final RSPickaxe bestPickaxe = RSPickaxe.getBestPickaxe(allPickaxesOwned);

        if (inBankArea.contains(Player.getPosition())){
            if (RSPickaxe.gotPickaxe()){
                if (currentPick.equals(bestPickaxe)){
                    if (bestPickaxe.isEquipped()){
                        //Best pickaxe is equipped, sorted!
                        upgradingPickaxe = false;
                    }else{
                        if (bestPickaxe.canWieldPickaxe()){
                            if (Banking.isBankScreenOpen()){
                                //Close bank in order to equip the pickaxe
                                if (Banking.close())
                                    SleepUtils.waitForBankToClose();
                            }else {
                                //Bank is closed equip pickaxe
                                bestPickaxe.Equip();
                            }
                        }else{
                            //Can't wield the pickaxe but it's in our invent. Done
                            upgradingPickaxe = false;
                        }
                    }
                }else{
                    if (currentPick.isEquipped()){
                        if (Banking.isBankScreenOpen()){
                            //Close bank in order to unequip the pickaxe
                            if (Banking.close())
                                SleepUtils.waitForBankToClose();
                        }else {
                            //Bank is closed, unequip pickaxe
                            currentPick.Unequip();
                        }
                    }else{
                        if (Banking.isBankScreenOpen()){
                            Banking.depositAll();
                            Timing.waitCondition(new Condition() {
                                @Override
                                public boolean active() {
                                    General.sleep(100, 300);
                                    return Inventory.getAll().length == 0;
                                }
                            }, General.random(3000, 6000));
                        }else{
                            //Open bank screen if not already open, then wait for it to appear
                            if (Banking.openBank())
                                SleepUtils.waitForBankToOpen();
                        }
                    }
                }
            }else{
                if (Banking.isBankScreenOpen()){
                    Banking.withdraw(1, bestPickaxe.Name());
                    Timing.waitCondition(new Condition() {
                        @Override
                        public boolean active() {
                            General.sleep(100, 300);
                            return RSPickaxe.gotPickaxe();
                        }
                    }, General.random(3000, 6000));
                }else{
                    if (Banking.openBank())
                        SleepUtils.waitForBankToOpen();
                }
            }
        }else{
            //Walk to bank then wait until we stop or enter a bank
            if (!Game.isRunOn() && Game.getRunEnergy() > 44)
                Options.setRunOn(true);
            PathFinding.aStarWalk(bankTile);
            Timing.waitCondition(new Condition() {
                @Override
                public boolean active() {
                    General.sleep(200, 500);
                    return !Player.isMoving() || inBankArea.contains(Player.getPosition());
                }
            }, General.random(10000, 20000));
        }

    }

    private void findAllAxes(){

        ArrayList<RSPickaxe> pickaxeList = new ArrayList<RSPickaxe>();

        //Scans bank for pickaxes and adds the valid ones to the list
        RSItem[] allBankedPickaxes = Banking.find(Filters.Items.nameContains(" pickaxe"));
        for (int i = 0; i < allBankedPickaxes.length; i++){
            RSPickaxe temp = RSPickaxe.getPickaxeFromName(allBankedPickaxes[i].getDefinition().getName());
            if (temp != null)
                pickaxeList.add(temp);
        }

        //Scans invent for pickaxes and adds the valid ones to the list
        RSItem[] pickaxesInInvent = Inventory.find(Filters.Items.nameContains(" pickaxe"));
        for (int i = 0; i < pickaxesInInvent.length; i++){
            RSPickaxe temp = RSPickaxe.getPickaxeFromName(pickaxesInInvent[i].getDefinition().getName());
            if (temp != null)
                pickaxeList.add(temp);
        }

        //Scans equipment for pickaxes and adds the valid ones to the list
        RSItem[] pickaxesEquipped = Equipment.find(Filters.Items.nameContains(" pickaxe"));
        for (int i = 0; i < pickaxesEquipped.length; i++){
            RSPickaxe temp = RSPickaxe.getPickaxeFromName(pickaxesEquipped[i].getDefinition().getName());
            if (temp != null)
                pickaxeList.add(temp);
        }

        //Convert that list into and array and save
        allPickaxesOwned = new RSPickaxe[pickaxeList.size()];
        for (int i = 0; i < pickaxeList.size(); i++)
            allPickaxesOwned[i] = pickaxeList.get(i);
    }

    private boolean canUpgradePickaxe(){

        RSPickaxe bestAxe = RSPickaxe.getBestPickaxe(allPickaxesOwned);

        return !RSPickaxe.gotPickaxe() || !RSPickaxe.getCurrentPickaxe().equals(bestAxe);
    }



    private void startSecondThread(){

        //Create a thread that will check if we can upgrade our pickaxe
        Thread loop = new Thread(
                new Runnable() {

                    @Override
                    public void run() {
                        int currentMiningLvl = Skills.getActualLevel(Skills.SKILLS.MINING);

                        if (canUpgradePickaxe()) {
                            upgradingPickaxe = true;
                            System.out.println("We can upgrade your pickaxe! Lets hope we don't fuck this up.");
                        }

                        while (true){
                            General.sleep(10000, 20000);

                            if (Skills.getActualLevel(Skills.SKILLS.MINING) > currentMiningLvl) {
                                currentMiningLvl = Skills.getActualLevel(Skills.SKILLS.MINING);
                                System.out.println("Gratz! You just leveled up to level " + currentMiningLvl + "!");
                                System.out.println("Just going to check if we can use a better pickaxe.");
                                if (canUpgradePickaxe()) {
                                    upgradingPickaxe = true;
                                    System.out.println("We can upgrade your pickaxe! Lets hope we don't fuck this up.");
                                }else{
                                    System.out.println("Nop.");
                                }
                            }

                        }
                    }


                }
        );
        loop.start();
    }

    private STATE updateState(){
        if (Game.getUptext().contains("->")) Mouse.click(1);
        if (Inventory.isFull() || !RSPickaxe.gotPickaxe() || (bankArea.contains(Player.getPosition()) && bankOre)) {
            if (bankOre || !RSPickaxe.gotPickaxe()) {
                if (upgradingPickaxe)
                    return STATE.UPGRADING_PICK;
                else
                    return STATE.BANKING;
            }else
                return STATE.DROP_ORE;
        }else {
            if (rockArea.contains(Player.getPosition()))
                return STATE.MINE_ROCKS;
            else
                return STATE.LOST;

        }
    }

    private enum STATE{
        MINE_ROCKS, BANKING, DROP_ORE, UPGRADING_PICK, LOST
    }
}
