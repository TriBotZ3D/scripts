package scripts.AccountStarter;

import org.tribot.api.input.Mouse;
import org.tribot.api2007.Camera;
import org.tribot.api2007.util.ThreadSettings;
import org.tribot.script.Script;
import org.tribot.script.ScriptManifest;
import org.tribot.script.interfaces.MessageListening07;
import scripts.AccountStarter.Combat.ASCombat;
import scripts.AccountStarter.Woodcutting.ASWoodcutting;

/**
 * Created by James on 22/09/2016.
 */
@ScriptManifest(authors = {"Z3D"}, name = "Account Starter", category = "Account Starter")
public class AccountStarter extends Script{


    @Override
    public void run() {

        Camera.setRotationMethod(Camera.ROTATION_METHOD.ONLY_MOUSE);
        Mouse.setSpeed(81);
        ThreadSettings.get().setClickingAPIUseDynamic(true);



    }

}
