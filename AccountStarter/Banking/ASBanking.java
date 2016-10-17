package scripts.AccountStarter.Banking;

import org.tribot.api.General;
import org.tribot.api.Timing;
import org.tribot.api.types.generic.Condition;
import org.tribot.api2007.*;
import org.tribot.api2007.ext.Filters;
import org.tribot.api2007.types.RSObject;
import org.tribot.api2007.types.RSTile;
import scripts.AccountStarter.Variables.ASAreas;
import scripts.AccountStarter.Variables.ASTiles;
import scripts.Utils.BankingUtils;
import scripts.Utils.WalkingUtils;

/**
 * Created by James on 23/09/2016.
 */
public class ASBanking {

    //region BANK WALKING

    public static void walkToBank() {

        switch (Player.getPosition().getPlane()) {

            case 0:
                if (Player.getPosition().distanceTo(ASTiles.getLumbyStairsTile()) < 15) {
                    RSObject[] Staircase = Objects.findNearest(20, Filters.Objects.nameEquals("Staircase"));
                    if (Staircase.length > 0) {
                        if (Staircase[0].isOnScreen()) {
                            if (Staircase[0].click("Climb-up")) {
                                Timing.waitCondition(new Condition() {
                                    @Override
                                    public boolean active() {
                                        General.sleep(100, 400);
                                        return Player.getPosition().getPlane() != 0;
                                    }
                                }, General.random(5000, 8000));
                            }
                        } else {
                            Walking.blindWalkTo(ASTiles.getLumbyStairsTile());
                            WalkingUtils.waitToStopWalking();
                        }
                    } else {
                        System.out.println("Could not find a Staircase on the ground floor, going up.");
                        General.sleep(1000);
                    }
                } else {
                    WebWalking.walkTo(ASTiles.getLumbyStairsTile());
                    WalkingUtils.waitToStopWalking();
                }
                break;


            case 1:
                RSObject[] Staircase = Objects.findNearest(20, Filters.Objects.nameEquals("Staircase"));
                if (Staircase.length > 0) {
                    if (Staircase[0].isOnScreen() && Staircase[0].isClickable()) {
                        if (Staircase[0].click("Climb-up")) {
                            Timing.waitCondition(new Condition() {
                                @Override
                                public boolean active() {
                                    General.sleep(100, 400);
                                    return Player.getPosition().getPlane() != 1;
                                }
                            }, General.random(4000, 6000));
                        }
                    } else {
                        Walking.blindWalkTo(Staircase[0].getPosition());
                        WalkingUtils.waitToStopWalking();
                    }
                } else {
                    System.out.println("Could not find a Staircase on the 1st floor, going up.");
                    General.sleep(1000);
                }
                break;


            case 2:
                if (!ASAreas.getBankArea().contains(Player.getPosition())) {
                    Walking.blindWalkTo(ASTiles.getBankTile());
                    Timing.waitCondition(new Condition() {
                        @Override
                        public boolean active() {
                            General.sleep(100, 500);
                            return ASAreas.getBankArea().contains(Player.getPosition());
                        }
                    }, General.random(10000, 15000));
                }
                //else at bank
                break;

            default:
                General.sleep(1000);
                System.out.println("Unknown floor when trying to walk to bank");
                break;

        }
    }

    public static void leaveBank(){
        RSObject[] Staircase = Objects.findNearest(20, Filters.Objects.nameEquals("Staircase"));

        switch (Player.getPosition().getPlane()){

            case 2:
                if (Staircase.length > 0) {
                    if (ASAreas.getBankArea().contains(Player.getPosition())){
                        Walking.blindWalkTo(new RSTile(3205, 3209));
                        WalkingUtils.waitToStopWalking();
                    }else {
                        if (Staircase[0].isOnScreen()) {
                            if (Staircase[0].click("Climb-down")) {
                                Timing.waitCondition(new Condition() {
                                    @Override
                                    public boolean active() {
                                        General.sleep(100, 400);
                                        return Player.getPosition().getPlane() != 2;
                                    }
                                }, General.random(4000, 6000));
                            }
                        } else {
                            Walking.blindWalkTo(new RSTile(3205, 3209));
                            WalkingUtils.waitToStopWalking();
                        }
                    }
                }else{
                    System.out.println("Could not find a Staircase on the 2nd floor, going down.");
                    General.sleep(1000);
                }
            break;

            case 1 :
                if (Staircase.length > 0) {
                    if (Staircase[0].isOnScreen()) {
                        if (Staircase[0].click("Climb-down")) {
                            Timing.waitCondition(new Condition() {
                                @Override
                                public boolean active() {
                                    General.sleep(100, 400);
                                    return Player.getPosition().getPlane() != 1;
                                }
                            }, General.random(4000, 6000));
                        }
                    } else {
                        Walking.blindWalkTo(Staircase[0].getPosition());
                        WalkingUtils.waitToStopWalking();
                    }
                }else{
                    System.out.println("Could not find a Staircase on the 1st floor, going down");
                    General.sleep(1000);
                }
            break;

            case 0:
                System.out.println("We've left the bank but we're still trying to leave?");
                break;
        }
    }

    //endregion

    //region DEPOSIT METHODS

    public static void depositEverything(){
        if (ASAreas.getBankArea().contains(Player.getPosition())){
            if (Banking.isBankScreenOpen()){
                if (Inventory.getAll().length > 0)
                    Banking.depositAll();

                if (Equipment.getItems().length > 0)
                    Banking.depositEquipment();

                Timing.waitCondition(new Condition() {
                    @Override
                    public boolean active() {
                        General.sleep(100, 300);
                        return Inventory.getAll().length == 0 && Equipment.getItems().length == 0;
                    }
                }, General.random(4000, 6000));
            }else{
                if (Banking.openBank())
                    BankingUtils.waitForBankToOpen();
            }
        }else{
            walkToBank();
        }
    }

    public static void depositInventory(){
        if (ASAreas.getBankArea().contains(Player.getPosition())){
            if (Banking.isBankScreenOpen()){
                if (Inventory.getAll().length > 0)
                    Banking.depositAll();

                Timing.waitCondition(new Condition() {
                    @Override
                    public boolean active() {
                        General.sleep(100, 300);
                        return Inventory.getAll().length == 0;
                    }
                }, General.random(4000, 6000));
            }else{
                if (Banking.openBank())
                    BankingUtils.waitForBankToOpen();
            }
        }else{
            walkToBank();
        }
    }

    public static void depositEquipment(){
        if (ASAreas.getBankArea().contains(Player.getPosition())){
            if (Banking.isBankScreenOpen()){
                if (Equipment.getItems().length > 0)
                    Banking.depositEquipment();

                Timing.waitCondition(new Condition() {
                    @Override
                    public boolean active() {
                        General.sleep(100, 300);
                        return Equipment.getItems().length == 0;
                    }
                }, General.random(4000, 6000));
            }else{
                if (Banking.openBank())
                    BankingUtils.waitForBankToOpen();
            }
        }else{
            walkToBank();
        }
    }

    //endregion

    //region WITHDRAW METHODS

    public static void withdraw(final String itemName, final int itemCount){
        if (ASAreas.getBankArea().contains(Player.getPosition())){
            if (Banking.isBankScreenOpen()){
                if (Banking.find(itemName).length > 0) {
                    if (Banking.withdraw(itemCount, itemName)) {
                        Timing.waitCondition(new Condition() {
                            @Override
                            public boolean active() {
                                General.sleep(100, 300);
                                return Inventory.getCount(itemName) >= itemCount;
                            }
                        }, General.random(3000, 5000));
                    }
                }else{
                    System.out.println("Could not find " + itemName + " in your bank!");
                    General.sleep(1000);
                }
            }else{
                if (Banking.openBank())
                    BankingUtils.waitForBankToOpen();
            }
        }else{
            walkToBank();
        }
    }

    //endregion

}
