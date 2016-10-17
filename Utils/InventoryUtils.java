package scripts.Utils;

import org.tribot.api.General;
import org.tribot.api.Timing;
import org.tribot.api.input.Mouse;
import org.tribot.api.types.generic.Condition;
import org.tribot.api.types.generic.Filter;
import org.tribot.api2007.Equipment;
import org.tribot.api2007.Game;
import org.tribot.api2007.GameTab;
import org.tribot.api2007.Inventory;
import org.tribot.api2007.types.RSItem;

import java.awt.*;

/**
 * Created by James on 15/09/2016.
 */
public class InventoryUtils {

    public static Rectangle[] allInventSlots = {
            new Rectangle(563, 213, 32, 32), new Rectangle(605, 213, 32, 32), new Rectangle(647, 213, 32, 32), new Rectangle(689, 213, 32, 32),
            new Rectangle(563, 249, 32, 32), new Rectangle(605, 249, 32, 32), new Rectangle(647, 249, 32, 32), new Rectangle(689, 249, 32, 32),
            new Rectangle(563, 285, 32, 32), new Rectangle(605, 285, 32, 32), new Rectangle(647, 285, 32, 32), new Rectangle(689, 285, 32, 32),
            new Rectangle(563, 321, 32, 32), new Rectangle(605, 321, 32, 32), new Rectangle(647, 321, 32, 32), new Rectangle(689, 321, 32, 32),
            new Rectangle(563, 357, 32, 32), new Rectangle(605, 357, 32, 32), new Rectangle(647, 357, 32, 32), new Rectangle(689, 357, 32, 32),
            new Rectangle(563, 393, 32, 32), new Rectangle(605, 393, 32, 32), new Rectangle(647, 393, 32, 32), new Rectangle(689, 393, 32, 32),
            new Rectangle(563, 429, 32, 32), new Rectangle(605, 429, 32, 32), new Rectangle(647, 429, 32, 32), new Rectangle(689, 429, 32, 32)};


    //Index as specified by TriBot. Left to right, top to bottom
    public static void moveItemToIndex(final RSItem inItem, final int inIndex) {

        //If the item is already at the right spot, just skip it
        if (inItem.getIndex() != inIndex) {

            //The inventory MUST be open!!
            if (GameTab.getOpen() != GameTab.TABS.INVENTORY)
                GameTab.open(GameTab.TABS.INVENTORY);

            if (GameTab.getOpen() == GameTab.TABS.INVENTORY) {

                //Hover over the item you want moved
                Mouse.moveBox(inItem.getArea());

                //Wait until we're sure we're hovering over it
                Timing.waitCondition(new Condition() {
                    @Override
                    public boolean active() {
                        General.sleep(100, 300);
                        return inItem.getArea().contains(Mouse.getPos());
                    }
                }, General.random(2000, 3000));

                //Just confirm we are hovering over it
                if (inItem.getArea().contains(Mouse.getPos())) {

                    //Hold down the mouse button
                    Mouse.sendPress(Mouse.getPos(), 1);

                    //Drag the item to the new spot
                    Mouse.moveBox(allInventSlots[inIndex]);

                    //Would like a conditional wait here, or a check that we're hovering over the right thing but
                    //When you click down the mouse, it's co-ords stay at that position >.>
                    General.sleep(100, 400);

                    //And release it
                    Mouse.sendRelease(MiscUtils.getRandPointInRectangle(allInventSlots[inIndex]), 1);
                    General.sleep(150, 500);
                }
            }
        }
    }

    public static boolean itemEquippedOrInInvent(String inName){
        return Inventory.find(inName).length > 0 || Equipment.find(inName).length > 0;
    }

    public static boolean onlyGotItems(final String[] inItemNames){
        int invalidInventItemsCount = Inventory.find(new Filter<RSItem>() {
            @Override
            public boolean accept(RSItem rsItem) {
                return !MiscUtils.arrayContainsString(inItemNames, rsItem.getDefinition().getName());
            }
        }).length;

        if (invalidInventItemsCount > 0)
            return false;

        int invalidEquipItemsCount = Equipment.find(new Filter<RSItem>() {
            @Override
            public boolean accept(RSItem rsItem) {
                return !MiscUtils.arrayContainsString(inItemNames, rsItem.getDefinition().getName());
            }
        }).length;

        if (invalidEquipItemsCount > 0)
            return false;

        return true;
    }

    public static int freeInventSlots(){
       return 28 - Inventory.getAll().length;
    }

    public static boolean inventIsEmpty(){
        return Inventory.getAll().length == 0;
    }

    //region CONDITIONAL SLEEPING

    public static void waitForEmptyInvent(){
        Timing.waitCondition(new Condition() {
            @Override
            public boolean active() {
                General.sleep(100, 300);
                return InventoryUtils.inventIsEmpty();
            }
        }, General.random(3000, 5000));
    }

    public static void waitForItemSelect(){
        Timing.waitCondition(new Condition() {
            @Override
            public boolean active() {
                General.sleep(100, 300);
                return Game.isUptext("->");
            }
        }, General.random(2000, 4000));
    }

    public static void waitForInventChange(){
        final int inventCount = Inventory.getAll().length;
        Timing.waitCondition(new Condition() {
            @Override
            public boolean active() {
                General.sleep(100, 300);
                return inventCount != Inventory.getAll().length;
            }
        }, General.random(2000, 3000));
    }

    //endregion

}
