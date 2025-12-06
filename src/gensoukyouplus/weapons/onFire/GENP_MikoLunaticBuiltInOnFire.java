package gensoukyouplus.weapons.onFire;

import com.fs.starfarer.api.combat.*;
import data.hullmods.FantasySpellMod;
import data.utils.FM_Colors;
import data.utils.FM_Misc;

public class GENP_MikoLunaticBuiltInOnFire implements OnFireEffectPlugin {

    @Override
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
        if (weapon == null) return;
        if (weapon.getShip() == null) return;

        ShipAPI ship = weapon.getShip();
        FantasySpellMod.SpellModState state = FM_Misc.getSpellModState(engine, ship);
        if (weapon.getAmmo() == 0){
            if(state.spellPower >= 0.8f){
                state.spellPower = state.spellPower - 0.8f;
                weapon.setAmmo(18);
            }

        }
        engine.addHitParticle(weapon.getFirePoint(0), FM_Misc.ZERO, 150f, 255f, 0.2f, FM_Colors.FM_TEXT_RED);

    }
}
