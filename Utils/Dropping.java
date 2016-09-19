package scripts.Utils;

import org.tribot.api.General;
import org.tribot.api.Timing;
import org.tribot.api.input.Mouse;
import org.tribot.api.types.generic.Condition;
import org.tribot.api2007.ChooseOption;
import org.tribot.api2007.GameTab;
import org.tribot.api2007.Inventory;
import org.tribot.api2007.types.RSItem;

import java.awt.*;

/**
 * Created by James on 14/09/2016.
 */

public class Dropping {

    //Code written by 'Encoded', modified by 'TheBat' and finally adapted by 'Z3D'
    public static void fastDrop(int numberOfItems) {
        if (GameTab.getOpen() != GameTab.TABS.INVENTORY)
            GameTab.open(GameTab.TABS.INVENTORY);

        if (GameTab.getOpen() == GameTab.TABS.INVENTORY) {
            RSItem[] items = Inventory.getAll();
            for (int i = 0; i < 4; i++) {
                for (int j = 0; j < 7; j++) {

                    if (items[4 * j + i] == null) continue;
                    if ((4 * j + i) >= numberOfItems) break;

                    final Rectangle r = new RSItem(4 * j + i, 0, 0, RSItem.TYPE.INVENTORY).getArea();
                    if (!r.contains(Mouse.getPos())) {
                        Mouse.move(new Point((int) r.getCenterX() + General.random(-3, 3),
                                (int) r.getCenterY() + General.random(-3, 3)));
                    }

                    if (r.contains(Mouse.getPos())) {
                        Mouse.click(3);
                        final int yy = getY();
                        if (yy == -1) {
                            Mouse.click(1);
                        } else {
                            Mouse.hop(new Point((int) Mouse.getPos().getX(), yy));
                            Mouse.click(1);
                        }
                    }

                }
            }

        }
    }

    private static int getY() {

        try {

            final String[] actions = ChooseOption.getOptions();

            for (int i = 0; i < actions.length; i++) {
                if (actions[i].toLowerCase().contains("drop")) {
                    return (int) (ChooseOption.getPosition().getY() + 21 + 16 * i);
                }
            }
        }catch (NullPointerException e){
            System.out.println("Null Point Exception caught. This was Encoded's code");
        }

        return -1;
    }

}
