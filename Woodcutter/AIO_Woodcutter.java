package scripts.Woodcutter;

import org.tribot.api.General;
import org.tribot.api.Timing;
import org.tribot.api.input.Mouse;
import org.tribot.api.types.generic.Condition;
import org.tribot.api.types.generic.Filter;
import org.tribot.api.util.abc.ABCUtil;
import org.tribot.api2007.*;
import org.tribot.api2007.ext.Filters;
import org.tribot.api2007.types.RSArea;
import org.tribot.api2007.types.RSItem;
import org.tribot.api2007.types.RSObject;
import org.tribot.api2007.types.RSTile;
import org.tribot.script.Script;
import org.tribot.script.ScriptManifest;
import scripts.Utils.BankingUtils;
import scripts.Utils.Dropping;
import scripts.Utils.InventoryUtils;
import scripts.Utils.Misc;

import java.awt.*;
import java.util.ArrayList;

/**
 * Created by James on 14/09/2016.
 */

@ScriptManifest(authors = {"Z3D"}, name = "AIO Woodcutter", category = "Woodcutting")
public class AIO_Woodcutter extends Script {

    public static String TREE_NAME;
    public static RSArea TREE_AREA;

    public static RSArea BANK_AREA = new RSArea(new RSTile(0,0,0), new RSTile(0,0,0));
    public static RSTile BANK_TILE = new RSTile(0,0,0);

    public static boolean bankLogs;
    public static boolean autoUpgradeAxe;
    public static boolean lootNests = false;

    private RSAxe[] allAxesOwned;
    private boolean upgradingAxe = false;

    @Override
    public void run() {

        System.out.println("Starting Z3D's AIO Woodcutter");

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

                    case CHOP_TREES:
                        chopTrees(TREE_NAME, TREE_AREA);
                        break;
                    case BANKING:
                        handleBank(TREE_AREA, BANK_AREA);
                        break;

                    case UPGRADING_AXE:
                        handleAxeUpgrade(BANK_AREA);
                        break;

                    case DROP_LOGS:
                        dropLogs();
                        break;

                    case LOST:
                        handleLost(TREE_AREA);
                        break;
                }
            }
        }else{
            System.out.println("GUI Closed!");
        }
    }


    private void handleBank(final RSArea inTreeArea, final RSArea inBankArea){

        if (inBankArea.contains(Player.getPosition())){
            if (needToBank()){
                if (Banking.isBankScreenOpen()){

                    //Will load all axes the player has in there bank only once
                    if (allAxesOwned == null && autoUpgradeAxe) {
                        findAllAxes();
                        if (allAxesOwned.length > 0)
                            startSecondThread();
                    }

                    //If we don't have an axe in out invent deposit all
                    if (!RSAxe.inventContainsAxe()){
                        Banking.depositAll();
                        Timing.waitCondition(new Condition() {
                            @Override
                            public boolean active() {
                                General.sleep(50, 300);
                                return Inventory.getAll().length == 0;
                            }
                        }, General.random(3000, 5000));
                    }else{
                        //Otherwise deposit all but the axe
                        final int itemsInInvent = Inventory.getAll().length;
                        Banking.depositAllExcept(Inventory.find(Filters.Items.nameContains(" axe"))[0].getID());
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
                        BankingUtils.waitForBankToOpen();
                    }
                }
            }else{
                //Walk back to trees
                if (!Game.isRunOn() && Game.getRunEnergy() > 44)
                    Options.setRunOn(true);
                PathFinding.aStarWalk(Misc.getCentreTile(inTreeArea));
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
            PathFinding.aStarWalk(BANK_TILE);
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
                return !rsItem.getDefinition().getName().contains(" axe");
            }
        });

        //Either we don't have an axe or we have items that aren't an axe
        return nonAxeItems.length > 0 || !RSAxe.gotAxe();
    }

    private void handleLost(RSArea inTreeArea){
        System.out.println("Oh no, we seem to be a little lost. Lets try and find out way back.");
        if (!Game.isRunOn() && Game.getRunEnergy() > 44)
            Options.setRunOn(true);
        PathFinding.aStarWalk(Misc.getCentreTile(inTreeArea));
        Timing.waitCondition(new Condition() {
            @Override
            public boolean active() {
                General.sleep(200, 500);
                return !Player.isMoving();
            }
        }, General.random(10000, 20000));
    }

    private void dropLogs(){

        RSItem[] axeInInvent = Inventory.find(Filters.Items.nameContains(" axe"));

        //If we have an axe in your inventory and its not in the bottom right, move it
        if (axeInInvent.length > 0 && axeInInvent[0].getIndex() != 27){
            System.out.println("Oi you fucking moron! Why is your axe not in the bottom right!?!");
            System.out.println("Do I have to do everything around here...");
            InventoryUtils.moveItemToIndex(axeInInvent[0], 27);
        }else {
            Dropping.fastDrop(27);
        }
    }

    private void chopTrees(final String inTreeName, final RSArea inTreeArea){

        //Search for all trees that have the desired name and are within the area and have a 'Chop down' action
        final RSObject[] allValidTrees = Objects.findNearest(20, Filters.Objects.nameEquals(inTreeName).combine(Filters.Objects.inArea(inTreeArea).combine(Filters.Objects.actionsContains("Chop down"), true), true));

            //If we're idle at the moment
            if (Player.getAnimation() == -1) {

                //If some trees exist that fit the criteria
                if (allValidTrees.length > 0) {
                    if (allValidTrees[0].isOnScreen() && allValidTrees[0].isClickable()) {
                        if (allValidTrees[0].click("Chop down")) {
                            //If we clicked the chop down option, wait until we start chopping
                            Timing.waitCondition(new Condition() {
                                @Override
                                public boolean active() {
                                    General.sleep(50, 200);
                                    return Player.getAnimation() != -1;
                                }
                            }, General.random(5000, 8000));
                        }

                    } else {
                        if (Camera.getCameraAngle() < 100){
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
                                return allValidTrees[0].isClickable() || !Player.isMoving();
                            }
                        }, General.random(20000, 40000));
                    }
                } else {
                    System.out.println("No " + inTreeName + " Trees found!");
                    Timing.waitCondition(new Condition() {
                        @Override
                        public boolean active() {
                            General.sleep(800, 1200);
                            return Objects.findNearest(100, Filters.Objects.nameEquals(inTreeName).combine(Filters.Objects.inArea(inTreeArea).combine(Filters.Objects.actionsContains("Chop down"), true), true)).length > 0;
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

    private void handleAxeUpgrade(final RSArea inBankArea){

        final RSAxe currentAxe = RSAxe.getCurrentAxe();
        final RSAxe bestAxe = RSAxe.getBestAxe(allAxesOwned);

        if (inBankArea.contains(Player.getPosition())){
            if (RSAxe.gotAxe()){
                if (currentAxe.equals(bestAxe)){
                    if (bestAxe.isEquipped()){
                        //Best axe is equipped, sorted!
                        upgradingAxe = false;
                    }else{
                        if (bestAxe.canWieldAxe()){
                            if (Banking.isBankScreenOpen()){
                                //Close bank in order to equip the axe
                                if (Banking.close())
                                    BankingUtils.waitForBankToClose();
                            }else {
                                //Bank is closed equip axe
                                bestAxe.Equip();
                            }
                        }else{
                            //Can't wield the axe but it's in our invent. Done
                            upgradingAxe = false;
                        }
                    }
                }else{
                    if (currentAxe.isEquipped()){
                        if (Banking.isBankScreenOpen()){
                            //Close bank in order to unequip the axe
                            if (Banking.close())
                                BankingUtils.waitForBankToClose();
                        }else {
                            //Bank is closed, unequip axe
                            currentAxe.Unequip();
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
                                BankingUtils.waitForBankToOpen();
                        }
                    }
                }
            }else{
                if (Banking.isBankScreenOpen()){
                    Banking.withdraw(1, bestAxe.Name());
                    Timing.waitCondition(new Condition() {
                        @Override
                        public boolean active() {
                            General.sleep(100, 300);
                            return RSAxe.gotAxe();
                        }
                    }, General.random(3000, 6000));
                }else{
                    if (Banking.openBank())
                        BankingUtils.waitForBankToOpen();
                }
            }
        }else{
            //Walk to bank then wait until we stop or enter a bank
            if (!Game.isRunOn() && Game.getRunEnergy() > 44)
                Options.setRunOn(true);
            PathFinding.aStarWalk(BANK_TILE);
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

        ArrayList<RSAxe> axeList = new ArrayList<RSAxe>();

        //Scans bank for axes and adds the valid ones to the list
        RSItem[] allBankedAxes = Banking.find(Filters.Items.nameContains(" axe"));
        for (int i = 0; i < allBankedAxes.length; i++){
            RSAxe temp = RSAxe.getAxeFromName(allBankedAxes[i].getDefinition().getName());
            if (temp != null)
                axeList.add(temp);
        }

        //Scans invent for axes and adds the valid ones to the list
        RSItem[] axesInInvent = Inventory.find(Filters.Items.nameContains(" axe"));
        for (int i = 0; i < axesInInvent.length; i++){
            RSAxe temp = RSAxe.getAxeFromName(axesInInvent[i].getDefinition().getName());
            if (temp != null)
                axeList.add(temp);
        }

        //Scans equipment for axes and adds the valid ones to the list
        RSItem[] axesEquipped = Equipment.find(Filters.Items.nameContains(" axe"));
        for (int i = 0; i < axesEquipped.length; i++){
            RSAxe temp = RSAxe.getAxeFromName(axesEquipped[i].getDefinition().getName());
            if (temp != null)
                axeList.add(temp);
        }

        //Convert that list into and array and save
        allAxesOwned = new RSAxe[axeList.size()];
        for (int i = 0; i < axeList.size(); i++)
            allAxesOwned[i] = axeList.get(i);
    }

    private boolean canUpgradeAxe(){

        RSAxe bestAxe = RSAxe.getBestAxe(allAxesOwned);

        return !RSAxe.gotAxe() || !RSAxe.getCurrentAxe().equals(bestAxe);
    }



    private void startSecondThread(){

        //Create a thread that will check if we can upgrade our axe
        Thread loop = new Thread(
                new Runnable() {

                    @Override
                    public void run() {
                        int currentWcLvl = Skills.getActualLevel(Skills.SKILLS.WOODCUTTING);

                        if (canUpgradeAxe()) {
                            upgradingAxe = true;
                            System.out.println("We can upgrade you axe! Lets hope we don't fuck this up.");
                        }

                        while (true){
                            General.sleep(10000, 20000);

                            if (Skills.getActualLevel(Skills.SKILLS.WOODCUTTING) > currentWcLvl) {
                                currentWcLvl = Skills.getActualLevel(Skills.SKILLS.WOODCUTTING);
                                System.out.println("Gratz! You just leveled up to level " + currentWcLvl + "!");
                                System.out.println("Just going to check if we can use a better axe.");
                                if (canUpgradeAxe()) {
                                    upgradingAxe = true;
                                    System.out.println("We can upgrade you axe! Lets hope we don't fuck this up.");
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
        if (Inventory.isFull() || !RSAxe.gotAxe() || (BANK_AREA.contains(Player.getPosition()) && bankLogs)) {
            if (bankLogs || !RSAxe.gotAxe()) {
                if (upgradingAxe)
                    return STATE.UPGRADING_AXE;
                else
                    return STATE.BANKING;
            }else
                return STATE.DROP_LOGS;
        }else {
            if (TREE_AREA.contains(Player.getPosition()))
                return STATE.CHOP_TREES;
            else
                return STATE.LOST;

        }
    }

    private enum STATE{
        CHOP_TREES, BANKING, DROP_LOGS, UPGRADING_AXE, LOST
    }
}
