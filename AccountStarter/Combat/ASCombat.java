package scripts.AccountStarter.Combat;

import org.tribot.api.General;
import org.tribot.api.Timing;
import org.tribot.api.input.Keyboard;
import org.tribot.api.types.generic.Condition;
import org.tribot.api.types.generic.Filter;
import org.tribot.api2007.*;
import org.tribot.api2007.ext.Filters;
import org.tribot.api2007.types.*;
import org.tribot.script.interfaces.MessageListening07;
import scripts.AccountStarter.Banking.ASBanking;
import scripts.AccountStarter.Variables.ASAreas;
import scripts.AccountStarter.Variables.ASVariables;
import scripts.AccountStarter.Variables.ASTiles;
import scripts.Utils.*;

/**
 * Created by James on 23/09/2016.
 *
 * Things to do:
 * -FINISHED
 */
public class ASCombat implements MessageListening07 {

    //region VARIABLES

    private boolean runScript = true;

    private COMBAT_STYLE bestCombatStyle;
    private boolean needToUpdateCombatStyle = true;

    private int targetAttLevel;
    private int currentAttLevel;

    private int targetStrLevel;
    private int currentStrLevel;

    private int targetDefLevel;
    private int currentDefLevel;

    //endregion

    //region MAIN RUN METHOD & CONSTRUCTORS

    public ASCombat(int inTargetAttLevel, int inTargetStrLevel, int inTargetDefLevel){
        targetAttLevel = inTargetAttLevel;
        currentAttLevel = Skills.getCurrentLevel(Skills.SKILLS.ATTACK);

        targetStrLevel = inTargetStrLevel;
        currentStrLevel = Skills.getCurrentLevel(Skills.SKILLS.STRENGTH);

        targetDefLevel = inTargetDefLevel;
        currentDefLevel = Skills.getCurrentLevel(Skills.SKILLS.DEFENCE);
    }

    public void run() {

        while (runScript) {
            switch (currentState) {

                case STARTING:
                    currentState = getInitialState();
                    break;

                case BANKING:
                    handleBank();
                    break;

                case FIGHTING:
                    handleCombat();
                    break;

                case WALKING_TO_BANK:
                    walkToBank();
                    break;

                case WALKING_TO_COMBAT_AREA:
                    walkToCombatArea();
                    break;

                case LOST:
                    handleLost();
                    break;
            }
        }
    }

    //endregion



    //region HANDLE BANKING

    /*
    private static void handleBank() {
        if (onlyCombatGearEquipped() && InventoryUtils.inventIsEmpty()) {
            currentState = STATE.WALKING_TO_COMBAT_AREA;
        } else {
            if (InventoryUtils.itemEquippedOrInInvent(ASVariables.getSwordName())){
                if (InventoryUtils.itemEquippedOrInInvent(ASVariables.getShieldName())){
                    if (nonCombatGearItemsEquippedCount() == 0) {
                        //If we are wearing anything, it is just combat gear
                        if (allCombatGearIsEquipped()) {
                            ASBanking.depositInventory();
                        }else{
                            equipGear();
                        }
                    } else {
                        //Wearing stuff that's not needed for combat
                        ASBanking.depositEquipment();
                    }
                }else{
                    ASBanking.withdraw(ASVariables.getShieldName(), 1);
                }
            }else{
                ASBanking.withdraw(ASVariables.getSwordName(), 1);
            }
        }
    }
    */

    private void handleBank(){
        if (allCombatGearIsEquipped() && gotAllCombatGear() && InventoryUtils.inventIsEmpty()) {
            currentState = STATE.WALKING_TO_COMBAT_AREA;
        }else {
            if (onlyCombatGearEquipped()) {
                if (onlyGotCombatGear()) {
                    if (gotAllCombatGear()) {
                        equipGear();
                    } else {
                        if (!InventoryUtils.itemEquippedOrInInvent(ASVariables.getSwordName()))
                            ASBanking.withdraw(ASVariables.getSwordName(), 1);
                        if (!InventoryUtils.itemEquippedOrInInvent(ASVariables.getShieldName()))
                            ASBanking.withdraw(ASVariables.getShieldName(), 1);
                    }
                } else {
                    ASBanking.depositInventory();
                }
            } else {
                ASBanking.depositEquipment();
            }
        }
    }

    //endregion

    //region HANDLE COMBAT

    private void handleCombat() {

        RSNPC[] targetNPC;

        if (allCombatGearIsEquipped()) {
            if (InventoryUtils.inventIsEmpty()) {
                if (ASAreas.getCombatArea().contains(Player.getPosition())) {
                    if (!CombatUtils.isInCombat()) {
                        targetNPC = getTargetNPCs();
                        if (targetNPC.length > 0) {
                            if (targetNPC[0].isOnScreen()) {
                                if (targetNPC[0].click("Attack"))
                                    CombatUtils.waitToEnterCombat();
                                General.sleep(500, 1500);
                            } else {
                                Walking.blindWalkTo(targetNPC[0].getPosition());
                                WalkingUtils.waitToStopWalking();
                            }
                        } else {
                            if (!goblinHouseDoorIsOpen()) {
                                openGoblinHouseDoor();
                            } else {
                                //Should rarely happen, so many fucking goblins!!
                                System.out.println("No " + ASVariables.getTargetNPCName() + "'s Found! Waiting for them to spawn.");
                                General.sleep(3000);
                            }
                        }
                    } else {
                        if (needToUpdateCombatStyle) {
                            setCombatStyle(bestCombatStyle);
                            needToUpdateCombatStyle = false;
                        }
                        CombatUtils.waitToLeaveCombat();
                    }
                } else {
                    currentState = STATE.LOST;
                }
            }else{
                Inventory.dropAllExcept(new String[] {ASVariables.getSwordName(), ASVariables.getShieldName()});
                InventoryUtils.waitForEmptyInvent();
            }
        } else {
            equipGear();
        }

    }

    private RSNPC[] getTargetNPCs(){
        if (ASAreas.getGoblinHouseArea().contains(Player.getPosition()) && !goblinHouseDoorIsOpen()) {
            //In goblin house and door is not open so only search for goblins in that area
            return NPCs.findNearest(new Filter<RSNPC>() {
                @Override
                public boolean accept(RSNPC rsnpc) {
                    if (rsnpc.getDefinition().getName().equals(ASVariables.getTargetNPCName()) && !rsnpc.isInCombat()) {
                        if (ASAreas.getCombatArea().contains(rsnpc.getPosition()) && ASAreas.getGoblinHouseArea().contains(Player.getPosition())) {
                            return true;
                        }
                    }
                    return false;
                }
            });
        }else{
            //Either we're not in the house or the door is open, so search for goblins outside of the house
            //Save dealing with the fucking awkward door.
            return NPCs.findNearest(new Filter<RSNPC>() {
                @Override
                public boolean accept(RSNPC rsnpc) {
                    if (rsnpc.getDefinition().getName().equals(ASVariables.getTargetNPCName()) && !rsnpc.isInCombat()) {
                        if (ASAreas.getCombatArea().contains(rsnpc.getPosition()) && !ASAreas.getGoblinHouseArea().contains(rsnpc.getPosition())) {
                            return true;
                        }
                    }
                    return false;
                }
            });
        }
    }

    private enum COMBAT_STYLE{
        ATTACK(0),
        STRENGTH(1),
        DEFENCE(3);

        private final int value;

        COMBAT_STYLE(final int newValue) {
            value = newValue;
        }

        public int getValue() { return value; }
    }

    private void setCombatStyle(COMBAT_STYLE inCombatStyle){
        Combat.selectIndex(inCombatStyle.getValue());
    }

    private COMBAT_STYLE getWorstCombatStyle(){
        COMBAT_STYLE worstCombatStyle = COMBAT_STYLE.ATTACK;
        int lowestCombatLevel = currentAttLevel;

        if (currentDefLevel < lowestCombatLevel && currentDefLevel < targetDefLevel){
            worstCombatStyle = COMBAT_STYLE.DEFENCE;
            lowestCombatLevel = currentDefLevel;
        }
        if (currentStrLevel < lowestCombatLevel && currentStrLevel < targetStrLevel)
            worstCombatStyle = COMBAT_STYLE.STRENGTH;

        return worstCombatStyle;
    }

    //endregion

    //region HANDLE WALKING

    private void handleLost() {
        if (onlyGotCombatGear()) {
            currentState = STATE.WALKING_TO_COMBAT_AREA;
        } else {
            currentState = STATE.WALKING_TO_BANK;
        }
    }

    private void walkToCombatArea() {
        if (!ASAreas.getCombatArea().contains(Player.getPosition())) {
            if (Player.getPosition().getPlane() == 0) {
                WebWalking.walkTo(ASTiles.getCentreCombatTile());
                WalkingUtils.waitToStopWalking();
            } else {
                ASBanking.leaveBank();
            }
        } else {
            currentState = STATE.FIGHTING;
        }
    }

    private void walkToBank() {
        if (!ASAreas.getBankArea().contains(Player.getPosition())) {
            if (ASAreas.getGoblinHouseArea().contains(Player.getPosition())) {
                if (goblinHouseDoorIsOpen()) {
                    ASBanking.walkToBank();
                } else {
                    openGoblinHouseDoor();
                }
            } else {
                ASBanking.walkToBank();
            }
        } else {
            currentState = STATE.BANKING;
        }
    }

    //endregion


    //region MISC METHODS

    private void equipGear() {
        RSItem[] Sword = Inventory.find(ASVariables.getSwordName());
        RSItem[] Shield = Inventory.find(ASVariables.getShieldName());

        if (!Banking.isBankScreenOpen()) {
            if (Sword.length > 0) {
                if (Sword[0].click("Wield")) {
                    Timing.waitCondition(new Condition() {
                        @Override
                        public boolean active() {
                            General.sleep(100, 300);
                            return Equipment.isEquipped(ASVariables.getSwordName());
                        }
                    }, General.random(3000, 5000));
                }
            }
            if (Shield.length > 0) {
                if (Shield[0].click("Wield")) {
                    Timing.waitCondition(new Condition() {
                        @Override
                        public boolean active() {
                            General.sleep(100, 300);
                            return Equipment.isEquipped(ASVariables.getShieldName());
                        }
                    }, General.random(3000, 5000));
                }
            }
        } else {
            if (Banking.close())
                BankingUtils.waitForBankToClose();
        }

    }

    private boolean onlyGotCombatGear() {
        return InventoryUtils.onlyGotItems(new String[] {ASVariables.getSwordName(), ASVariables.getShieldName()});
    }

    private boolean gotAllCombatGear() {
        return InventoryUtils.itemEquippedOrInInvent(ASVariables.getSwordName()) && InventoryUtils.itemEquippedOrInInvent(ASVariables.getShieldName());
    }

    private boolean allCombatGearIsEquipped() {
        return Equipment.isEquipped(ASVariables.getSwordName()) && Equipment.isEquipped(ASVariables.getShieldName());
    }

    private boolean onlyCombatGearEquipped() {
        int combatGear = Equipment.find(ASVariables.getSwordName(), ASVariables.getShieldName()).length;
        int allGear = Equipment.getItems().length;
        return allGear == combatGear;
    }

    //endregion

    //region HANDLE GOBLIN HOUSE

    private void openGoblinHouseDoor() {
        RSObject[] Door = Objects.findNearest(10, new Filter<RSObject>() {
            @Override
            public boolean accept(RSObject rsObject) {
                if (rsObject.getDefinition().getName().equals("Door")) {
                    if (MiscUtils.arrayContainsString(rsObject.getDefinition().getActions(), "Open")) {
                        return true;
                    }
                }
                return false;
            }
        });
        if (Door.length > 0) {
            if (Door[0].click("Open")) {
                Timing.waitCondition(new Condition() {
                    @Override
                    public boolean active() {
                        General.sleep(400, 800);
                        return goblinHouseDoorIsOpen();
                    }
                }, General.random(6000, 9000));
            }
        } else {
            System.out.println("Failed trying to open Goblin House Door, no door found.");
            General.sleep(1000);
        }
    }

    private boolean goblinHouseDoorIsOpen() {
        RSObject[] openDoor = Objects.getAt(new RSTile(3246, 3243, 0), Filters.Objects.nameEquals("Door"));
        return openDoor.length > 0;
    }

    //endregion

    //region STATE HANDLING

    private enum STATE {
        STARTING, BANKING, FIGHTING, WALKING_TO_BANK, WALKING_TO_COMBAT_AREA, LOST
    }

    private STATE currentState = STATE.STARTING;

    private STATE getInitialState() {
        if (ASAreas.getCombatArea().contains(Player.getPosition()))
            return STATE.FIGHTING;
        else if (ASAreas.getBankArea().contains(Player.getPosition()))
            return STATE.BANKING;
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

            currentDefLevel = Skills.getCurrentLevel(Skills.SKILLS.DEFENCE);
            currentStrLevel = Skills.getCurrentLevel(Skills.SKILLS.STRENGTH);
            currentAttLevel = Skills.getCurrentLevel(Skills.SKILLS.ATTACK);

            if (currentAttLevel >= targetAttLevel && currentStrLevel >= targetStrLevel && currentDefLevel >= targetDefLevel)
                runScript = false;

            COMBAT_STYLE styleToTrain = getWorstCombatStyle();
            if (styleToTrain != bestCombatStyle){
                bestCombatStyle = styleToTrain;
                needToUpdateCombatStyle = true;
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
