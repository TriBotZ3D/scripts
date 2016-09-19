package scripts;

import org.tribot.api.General;
import org.tribot.api.input.Mouse;
import org.tribot.api2007.*;
import org.tribot.api2007.ext.Filters;
import org.tribot.api2007.types.RSItem;
import org.tribot.script.Script;
import org.tribot.script.ScriptManifest;
import scripts.Utils.BankingUtils;
import scripts.Utils.Dropping;
import scripts.Utils.Misc;
import sun.security.x509.GeneralName;

import javax.rmi.CORBA.Util;
import java.awt.*;

/**
 * Created by James on 14/09/2016.
 */

@ScriptManifest(authors = {"Z3D"}, name = "Test Script", category = "Test")
public class TEST_SCRIPT extends Script{


    @Override
    public void run() {

        while (true) {

            General.sleep(1000);
            System.out.println("Is in Bank: " + Banking.isInBank());
            System.out.println("Player position: " + Player.getPosition());
            System.out.println("");
        }
    }

}
