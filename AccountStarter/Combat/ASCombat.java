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
import scripts.AccountStarter.Variables.ASNames;
import scripts.Utils.InventoryUtils;
import scripts.Utils.Misc;
import scripts.Utils.SleepUtils;

/**
 * Created by James on 23/09/2016.
 */
public class ASCombat {

    private enum STATE {
        STARTING, BANKING, FIGHTING, WALKING_TO_BANK, WALKING_TO_COMBAT_AREA, LOST, END
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
                getInitialState();
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

            case END:
                break;
        }
    }


    private static void handleBank() {
        if (onlyCombatGearEquipped() && InventoryUtils.inventIsEmpty()) {
            currentState = STATE.WALKING_TO_COMBAT_AREA;
        } else {
            if (gotAllCombatGear()) {
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
            } else {
                //Withdraw items
                if (InventoryUtils.haveGotItem(ASNames.getSwordName()))
                    ASBanking.withdraw(ASNames.getSwordName(), 1);
                if (InventoryUtils.haveGotItem(ASNames.getShieldName()))
                    ASBanking.withdraw(ASNames.getShieldName(), 1);
            }
        }
    }


    private static void handleCombat() {

        RSNPC[] targetNPC;
        if (ASAreas.getGoblinHouseArea().contains(Player.getPosition()) && !goblinHouseDoorIsOpen()) {
            //In goblin house and door is not open so only search for goblins in that area
            targetNPC = NPCs.findNearest(new Filter<RSNPC>() {
                @Override
                public boolean accept(RSNPC rsnpc) {
                    if (rsnpc.getDefinition().getName().equals(ASNames.getTargetNPCName()) && !rsnpc.isInCombat()) {
                        if (ASAreas.getCombatArea().contains(rsnpc.getPosition()) && ASAreas.getGoblinHouseArea().contains(Player.getPosition())) {
                            return true;
                        }
                    }
                    return false;
                }
            });
        }else{
            //Either we're not in the house or the door is open, so search for goblins outside of the house
            //Save dealing with the fucking akward door.
            targetNPC = NPCs.findNearest(new Filter<RSNPC>() {
                @Override
                public boolean accept(RSNPC rsnpc) {
                    if (rsnpc.getDefinition().getName().equals(ASNames.getTargetNPCName()) && !rsnpc.isInCombat()) {
                        if (ASAreas.getCombatArea().contains(rsnpc.getPosition()) && !ASAreas.getGoblinHouseArea().contains(rsnpc.getPosition())) {
                            return true;
                        }
                    }
                    return false;
                }
            });
        }

        if (!Player.getRSPlayer().isInCombat()){
            if (targetNPC.length > 0){
                if (targetNPC[0].isOnScreen()){
                    if (targetNPC[0].click("Attack")){
                        //This may look weird but it's for those with high ping
                        //When you click the npc it will run to them after a bit
                        //The two sleeps prevent spam clicking when waiting for the player to move
                        Timing.waitCondition(new Condition() {
                            @Override
                            public boolean active() {
                                General.sleep(100, 300);
                                return Player.getRSPlayer().isInCombat() || Player.isMoving();
                            }
                        }, General.random(7000, 12000));
                        Timing.waitCondition(new Condition() {
                            @Override
                            public boolean active() {
                                General.sleep(100, 300);
                                return Player.getRSPlayer().isInCombat() || !Player.isMoving();
                            }
                        }, General.random(8000, 12000));
                    }
                }else{
                    Walking.blindWalkTo(targetNPC[0].getPosition());
                    SleepUtils.waitToStopWalking();
                }
            }else{
                if (!goblinHouseDoorIsOpen()) {
                    openGoblinHouseDoor();
                }else {
                    //Should rarely happen, so many fucking goblins!!
                    System.out.println("No " + ASNames.getTargetNPCName() + "'s Found! Waiting for them to spawn.");
                    General.sleep(3000);
                }
            }
        }else{
            SleepUtils.waitToLeaveCombat();
        }
    }

    private static void handleLost() {
        if (justGotCombatGear()) {
            walkToCombatArea();
        } else {
            walkToBank();
        }
    }


    private static void equipGear() {
        RSItem[] Sword = Inventory.find(ASNames.getSwordName());
        RSItem[] Shield = Inventory.find(ASNames.getShieldName());

        if (!Banking.isBankScreenOpen()) {
            if (Sword.length > 0) {
                if (Sword[0].click("Wield")) {
                    Timing.waitCondition(new Condition() {
                        @Override
                        public boolean active() {
                            General.sleep(100, 300);
                            return Equipment.isEquipped(ASNames.getSwordName());
                        }
                    }, General.random(3000, 5000));
                }
            }
            if (Shield.length > 0) {
                if (Shield[0].click("Wear")) {
                    Timing.waitCondition(new Condition() {
                        @Override
                        public boolean active() {
                            General.sleep(100, 300);
                            return Equipment.isEquipped(ASNames.getShieldName());
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
        RSItem[] nonCmbInventItems = Inventory.find(new Filter<RSItem>() {
            @Override
            public boolean accept(RSItem rsItem) {
                if (rsItem != null && rsItem.getDefinition() != null)
                    return !rsItem.getDefinition().getName().equals(ASNames.getSwordName()) && !rsItem.getDefinition().getName().equals(ASNames.getShieldName());

                return false;
            }
        });

        if (nonCmbInventItems.length > 0)
            return false;

        RSItem[] nonCmbEquippedItems = Inventory.find(new Filter<RSItem>() {
            @Override
            public boolean accept(RSItem rsItem) {
                if (rsItem != null && rsItem.getDefinition() != null)
                    return !rsItem.getDefinition().getName().equals(ASNames.getSwordName()) && !rsItem.getDefinition().getName().equals(ASNames.getShieldName());

                return false;
            }
        });

        return nonCmbEquippedItems.length == 0;

    }

    private static boolean gotAllCombatGear() {
        return InventoryUtils.haveGotItem(ASNames.getSwordName()) && InventoryUtils.haveGotItem(ASNames.getShieldName());
    }

    private static boolean allCombatGearIsEquipped() {
        return Equipment.isEquipped(ASNames.getSwordName()) && Equipment.isEquipped(ASNames.getShieldName());
    }

    private static boolean onlyCombatGearEquipped() {
        return allCombatGearIsEquipped() && Equipment.getItems().length == 2;
    }

    private static int nonCombatGearItemsEquippedCount() {
        return Equipment.find(new Filter<RSItem>() {
            @Override
            public boolean accept(RSItem rsItem) {
                return !rsItem.getDefinition().getName().equals(ASNames.getSwordName()) && !rsItem.getDefinition().getName().equals(ASNames.getShieldName());
            }
        }).length;
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


    private static void walkToCombatArea() {
        if (!ASAreas.getCombatArea().contains(Player.getPosition())) {
            if (Player.getPosition().getPlane() == 0) {
                WebWalking.walkTo(Misc.getCentreTile(ASAreas.getCombatArea()));
                Timing.waitCondition(new Condition() {
                    @Override
                    public boolean active() {
                        General.sleep(200, 800);
                        return ASAreas.getCombatArea().contains(Player.getPosition());
                    }
                }, General.random(5000, 8000));
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
