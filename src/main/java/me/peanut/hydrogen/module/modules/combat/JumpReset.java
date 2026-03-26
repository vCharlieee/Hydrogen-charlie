package me.peanut.hydrogen.module.modules.combat;

import com.darkmagician6.eventapi.EventTarget;
import me.peanut.hydrogen.events.EventUpdate;
import me.peanut.hydrogen.module.Category;
import me.peanut.hydrogen.module.Info;
import me.peanut.hydrogen.module.Module;
import me.peanut.hydrogen.settings.Setting;

import java.util.Random;

/**
 * Created by peanut style
 */

@Info(name = "JumpReset", description = "Automatically jumps when hit", category = Category.Combat)
public class JumpReset extends Module {

    private final Random random = new Random();
    private long lastHitTime;

    public JumpReset() {
        addSetting(new Setting("Chance", this, 100, 0, 100, true));
    }

    @EventTarget
    public void onUpdate(EventUpdate e) {
        if (mc.thePlayer == null || mc.theWorld == null) return;

        int chance = (int) h2.settingsManager.getSettingByName(this, "Chance").getValue();

        // Detect if player was recently hurt
        if (mc.thePlayer.hurtTime > 0) {
            // Prevent multiple jumps during same hit
            if (System.currentTimeMillis() - lastHitTime > 200) {

                int roll = random.nextInt(100);

                if (roll <= chance) {
                    mc.thePlayer.jump();
                }

                lastHitTime = System.currentTimeMillis();
            }
        }
    }
}
