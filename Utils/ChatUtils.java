package scripts.Utils;

import org.tribot.api.General;
import org.tribot.api.Timing;
import org.tribot.api.types.generic.Condition;
import org.tribot.api2007.Screen;

import java.awt.*;

/**
 * Created by James on 16/10/2016.
 */
public class ChatUtils {

    public static boolean interfaceCoveringChat(){
        return !Screen.getColorAt(new Point(503, 445)).equals(new Color(0, 0, 1));
    }

    //region SLEEP METHODS

    public static void waitForChatInterface(){
        Timing.waitCondition(new Condition() {
            @Override
            public boolean active() {
                General.sleep(200, 700);
                return interfaceCoveringChat();
            }
        }, General.random(4000, 7000));
    }

    //endregion
}
