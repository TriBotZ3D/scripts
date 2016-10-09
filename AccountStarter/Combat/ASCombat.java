package scripts.AccountStarter.Combat;

import org.tribot.api.General;
import org.tribot.api.Timing;
import org.tribot.api.types.generic.Condition;
import org.tribot.api.types.generic.Filter;
import org.tribot.api2007.*;
import org.tribot.api2007.ext.Filters;
import org.tribot.api2007.types.RSItem;
import org.tribot.api2007.types.RSNPC;
import org.tribot.api2007.types.RSObject;
import org.tribot.api2007.types.RSTile;
import scripts.AccountStarter.Banking.ASBanking;
import scripts.AccountStarter.Variables.ASAreas;
import scripts.AccountStarter.Variables.ASVariables;
import scripts.AccountStarter.Variables.ASTiles;
import scripts.Utils.InventoryUtils;
import scripts.Utils.Misc;
import scripts.Utils.SleepUtils;

/**
 * Created by James on 23/09/2016.
 */
public class ASCombat {

    private enum STATE {
        STARTING, BANKING, FIGHTING, WALKING_TO_BANK, WALKING_TO_COMBAT_AREA, LOST
    }

    private static STATE currentState = STATE.STARTING;

    private static STATE getInitialState() {
        if (ASAreas.getCombatArea().contains(Player.getPosition()))
            return STATE.FIGHTING;
        else if (ASAreas.getBankArea().contains(Player.getPosition()))
            return STATE.BANKING;
        return STATE.LOST;
    }

    public static void main() {

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

    private static void handleCombat() {

        RSNPC[] targetNPC;

            if (allCombatGearIsEquipped()) {
                if (InventoryUtils.inventIsEmpty()) {
                    if (ASAreas.getCombatArea().contains(Player.getPosition())) {
                        if (!isInCombat()) {
                            targetNPC = getTargetNPCs();
                            if (targetNPC.length > 0) {
                                if (targetNPC[0].isOnScreen()) {
                                    if (targetNPC[0].click("Attack"))
                                        SleepUtils.waitToEnterCombat();
                                    General.sleep(500, 1500);
                                } else {
                                    Walking.blindWalkTo(targetNPC[0].getPosition());
                                    SleepUtils.waitToStopWalking();
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
                            SleepUtils.waitToLeaveCombat();
                        }
                    } else {
                        currentState = STATE.LOST;
                    }
                }else{
                    Inventory.dropAllExcept(new String[] {ASVariables.getSwordName(), ASVariables.getShieldName()});
                    SleepUtils.waitForEmptyInvent();
                }
            } else {
                equipGear();
            }

    }

    private static void handleLost() {
        if (justGotCombatGear()) {
            currentState = STATE.WALKING_TO_COMBAT_AREA;
        } else {
            currentState = STATE.WALKING_TO_BANK;
        }
    }


    private static RSNPC[] getTargetNPCs(){
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

    private static boolean isInCombat(){
        return Player.getRSPlayer().getInteractingCharacter() != null;
    }

    private static void openGoblinHouseDoor() {
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

    private static boolean goblinHouseDoorIsOpen() {
        RSObject[] openDoor = Objects.getAt(new RSTile(3246, 3243, 0), Filters.Objects.nameEquals("Door"));
        return openDoor.length > 0;
    }


    private static void equipGear() {
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
                SleepUtils.waitForBankToClose();
        }

    }

    private static boolean justGotCombatGear() {

        if (!gotAllCombatGear())
            return false;

        RSItem[] nonCmbInventItems = Inventory.find(new Filter<RSItem>() {
            @Override
            public boolean accept(RSItem rsItem) {
                return !rsItem.getDefinition().getName().equals(ASVariables.getSwordName()) && !rsItem.getDefinition().getName().equals(ASVariables.getShieldName());

            }
        });

        if (nonCmbInventItems.length > 0)
            return false;

        RSItem[] nonCmbEquippedItems = Inventory.find(new Filter<RSItem>() {
            @Override
            public boolean accept(RSItem rsItem) {
                return !rsItem.getDefinition().getName().equals(ASVariables.getSwordName()) && !rsItem.getDefinition().getName().equals(ASVariables.getShieldName());

            }
        });

        if (nonCmbEquippedItems.length > 0)
            return false;

        return true;

    }

    private static boolean gotAllCombatGear() {
        return InventoryUtils.itemEquippedOrInInvent(ASVariables.getSwordName()) && InventoryUtils.itemEquippedOrInInvent(ASVariables.getShieldName());
    }

    private static boolean allCombatGearIsEquipped() {
        return Equipment.isEquipped(ASVariables.getSwordName()) && Equipment.isEquipped(ASVariables.getShieldName());
    }

    private static boolean onlyCombatGearEquipped() {
        return allCombatGearIsEquipped() && Equipment.getItems().length == 2;
    }

    private static int nonCombatGearItemsEquippedCount() {
        return Equipment.find(new Filter<RSItem>() {
            @Override
            public boolean accept(RSItem rsItem) {
                return !rsItem.getDefinition().getName().equals(ASVariables.getSwordName()) && !rsItem.getDefinition().getName().equals(ASVariables.getShieldName());
            }
        }).length;
    }


    private static void walkToCombatArea() {
        if (!ASAreas.getCombatArea().contains(Player.getPosition())) {
            if (Player.getPosition().getPlane() == 0) {
                WebWalking.walkTo(ASTiles.getCentreCombatTile());
                SleepUtils.waitToStopWalking();
            } else {
                ASBanking.leaveBank();
            }
        } else {
            currentState = STATE.FIGHTING;
        }
    }

    private static void walkToBank() {
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

}
