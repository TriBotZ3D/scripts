package scripts.Utils;

import org.tribot.api.General;
import org.tribot.api.Timing;
import org.tribot.api.types.generic.Condition;
import org.tribot.api.types.generic.Filter;
import org.tribot.api2007.*;
import org.tribot.api2007.ext.Filters;
import org.tribot.api2007.types.RSArea;
import org.tribot.api2007.types.RSItem;
import org.tribot.api2007.types.RSNPC;
import org.tribot.api2007.types.RSObject;

/**
 * Created by James on 15/09/2016.
 */
public class BankingUtils {


    public static void depositItems(RSItem[] itemsToDeposit, boolean closeBankScreen){

        if (Banking.isInBank()){
           // if ()
            if (Banking.isBankScreenOpen()) {
                if (canUseBankAll(itemsToDeposit)) {
                    Banking.depositAll();
                    Timing.waitCondition(new Condition() {
                        @Override
                        public boolean active() {
                            General.sleep(50, 300);
                            return !Inventory.isFull();
                        }
                    }, General.random(3000, 5000));
                } else {
                    Banking.depositAllExcept(Inventory.find(Filters.Items.nameContains("axe"))[0].getID());
                    Timing.waitCondition(new Condition() {
                        @Override
                        public boolean active() {
                            General.sleep(50, 300);
                            return !Inventory.isFull();
                        }
                    }, General.random(5000, 10000));
                }
            }
        }else{
            //Walk to bank then wait until we stop or enter a bank
            WebWalking.walkToBank();
            Timing.waitCondition(new Condition() {
                @Override
                public boolean active() {
                    General.sleep(200, 500);
                    return !Player.isMoving() || Banking.isInBank();
                }
            }, General.random(10000, 20000));
        }
    }

    private static boolean canUseBankAll(RSItem[] itemsToDeposit){
        //We are depositing all items in our inv
        return itemsToDeposit.length == Inventory.getAll().length;
    }


    //region CONDITIONAL SLEEPING

    public static void waitForBankToOpen(){
        Timing.waitCondition(new Condition() {
            @Override
            public boolean active() {
                General.sleep(100, 300);
                return Banking.isBankScreenOpen();
            }
        }, General.random(4000, 6000));
    }

    public static void waitForBankToClose(){
        Timing.waitCondition(new Condition() {
            @Override
            public boolean active() {
                General.sleep(100, 300);
                return !Banking.isBankScreenOpen();
            }
        }, General.random(4000, 6000));
    }

    //endregion

}
