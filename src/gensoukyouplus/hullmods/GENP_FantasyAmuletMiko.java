package gensoukyouplus.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.listeners.DamageDealtModifier;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.combat.entities.DamagingExplosion;
import data.hullmods.FantasySpellMod;
import data.utils.FM_Colors;
import data.utils.FM_Misc;
import data.utils.I18nUtil;
import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicRender;

public class GENP_FantasyAmuletMiko extends BaseHullMod {
    public static final float TIME = 0.4F;
    public static final float MAX_BUFF_OF_SPELLMOD = 15.0F;
    public static final float SpellSupply = 0.04F;
    private static final String buffId = "FantasyAmuletMod_Buff";
    private final Object key = new Object();


    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        super.applyEffectsAfterShipCreation(ship, id);
        ship.addListener(new MikoHitListener(ship));
    }

    public void advanceInCombat(ShipAPI ship, float amount) {
        if (ship != null) {
            if (ship.isAlive()) {
                CombatEngineAPI engine = Global.getCombatEngine();
                if (engine != null) {
                    if (!engine.getCustomData().containsKey("FantasyAmuletMod")) {
                        engine.getCustomData().put("FantasyAmuletMod", new HashMap());
                    }

                    Map<ShipAPI, ModState> currState = (Map)engine.getCustomData().get("FantasyAmuletMod");
                    if (!currState.containsKey(ship)) {
                        currState.put(ship, new ModState());
                    }

                    if (ship.getVariant().hasHullMod("FantasySpellMod")) {
                        FantasySpellMod.SpellModState spellModState = FM_Misc.getSpellModState(engine, ship);
                        spellModState.spellPower += engine.getElapsedInLastFrame() * 0.049999997F;
                        float percent = spellModState.spellPower * 15.0F;
                        if (percent >= 0.0F) {
                            ship.getMutableStats().getMaxSpeed().modifyPercent("FantasyAmuletMod_Buff", percent);
                            ship.getMutableStats().getMaxTurnRate().modifyPercent("FantasyAmuletMod_Buff", percent);
                            ship.getMutableStats().getAcceleration().modifyPercent("FantasyAmuletMod_Buff", percent);
                            ship.getMutableStats().getDeceleration().modifyPercent("FantasyAmuletMod_Buff", percent);
                            ship.getMutableStats().getTurnAcceleration().modifyPercent("FantasyAmuletMod_Buff", percent);
                        } else {
                            ship.getMutableStats().getMaxSpeed().unmodifyPercent("FantasyAmuletMod_Buff");
                            ship.getMutableStats().getMaxTurnRate().unmodifyPercent("FantasyAmuletMod_Buff");
                            ship.getMutableStats().getAcceleration().unmodifyPercent("FantasyAmuletMod_Buff");
                            ship.getMutableStats().getDeceleration().unmodifyPercent("FantasyAmuletMod_Buff");
                            ship.getMutableStats().getTurnAcceleration().unmodifyPercent("FantasyAmuletMod_Buff");
                        }

                        if (ship == engine.getPlayerShip()) {
                            engine.maintainStatusForPlayerShip(this.key, ship.getSystem().getSpecAPI().getIconSpriteName(), I18nUtil.getHullModString("FantasyAmuletMod_PlayerTitle"), I18nUtil.getHullModString("FantasyAmuletMod_PlayerData") + (int)percent + "%", false);
                        }

                        SpriteAPI effect = Global.getSettings().getSprite("fx", "miko_lunatic_effects");
                        SpriteAPI effectorb = Global.getSettings().getSprite("fx", "miko_lunatic_effects_orbs");
                        Vector2f size = new Vector2f(effect.getWidth(), effect.getHeight());
                        Vector2f offsetVector = new Vector2f(-10,0);
                        ((ModState)currState.get(ship)).alphaForVisual += amount;
                        if (((ModState)currState.get(ship)).alphaForVisual >= 1.0F) {
                            ((ModState)currState.get(ship)).alphaForVisual = 0.0F;

                            for(int i = 0; i < 4; ++i) {
                                MagicRender.objectspace(effect, ship, offsetVector, FM_Misc.ZERO, size, ship.getRenderOffset(), -180.0F, 0.0F, true, Misc.scaleAlpha(FM_Colors.FM_RED_EMP_FRINGE, Math.min(spellModState.spellPower, 1.0F)), spellModState.spellPower * 3.5F, 0.0F, 1.0F, 1.0F, 0.0F, 0.3F, 0.3F, 0.4F, true, CombatEngineLayers.ABOVE_SHIPS_LAYER, 770, 1);
                                MagicRender.objectspace(effectorb, ship, offsetVector, FM_Misc.ZERO, size, ship.getRenderOffset(), -180.0F, 0.0F, true, Misc.scaleAlpha(FM_Colors.FM_RED_EMP_FRINGE, Math.min(spellModState.spellPower, 1.0F)), spellModState.spellPower * 3.5F, 0.0F, 1.0F, 1.0F, 0.0F, 0.3F, 0.3F, 0.4F, true, CombatEngineLayers.ABOVE_SHIPS_LAYER, 770, 1);

                            }
                        }

                        if (!((ModState)currState.get(ship)).isActive) {
                            ((ModState)currState.get(ship)).weapons = ship.getAllWeapons();
                            ((ModState)currState.get(ship)).isActive = true;
                        }

                        if (((ModState)currState.get(ship)).isActive) {
                            ((ModState)currState.get(ship)).timer += amount;
                            if (((ModState)currState.get(ship)).timer >= 0.4F) {
                                ((ModState)currState.get(ship)).timer = 0.4F;
                            }
                        }

                    }
                }
            }
        }
    }

    public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        tooltip.addSpacer(10.0F);
        tooltip.addSectionHeading(I18nUtil.getHullModString("FM_DescriptionAndEvaluation"), Alignment.TMID, 4.0F);
        if (Keyboard.isKeyDown(Keyboard.getKeyIndex("F1"))) {
            tooltip.addPara(I18nUtil.getHullModString("FantasyAmulet_DAE_0"), Misc.getTextColor(), 4.0F);
            tooltip.addSpacer(10.0F);
            tooltip.addPara(I18nUtil.getHullModString("FantasyAmulet_DAE_1"), Misc.getGrayColor(), 4.0F);
        }

        if (!Keyboard.isKeyDown(Keyboard.getKeyIndex("F1"))) {
            tooltip.addPara("Press and hold [%s] to view this information.", Float.valueOf(10.0F), Misc.getGrayColor(), Misc.getStoryBrightColor(), new String[]{"F1"}).setAlignment(Alignment.MID);
        }

    }

    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if (index == 0) {
            return "4%";
        } else if (index == 1) {
            return "15%";
        } else if (index == 2) {
            return "16";
        } else if (index == 3) {
            return 0.4F + I18nUtil.getHullModString("FantasyAmuletMod_HL_1");
        } else {
            return index == 4 ? 70 + I18nUtil.getHullModString("FantasyAmuletMod_HL_2") : null;
        }
    }

    public static class MikoHitListener implements DamageDealtModifier {
        private final ShipAPI ship;

        public MikoHitListener(ShipAPI ship) {
            this.ship = ship;
        }

        public String modifyDamageDealt(Object param, CombatEntityAPI target, DamageAPI damage, Vector2f point, boolean shieldHit) {
            if (Global.getCombatEngine() == null) {
                return null;
            } else {
                CombatEngineAPI engine = Global.getCombatEngine();
                if (!engine.getCustomData().containsKey("FantasyAmuletMod")) {
                    return null;
                } else {
                    Map<ShipAPI, ModState> currState = (Map)engine.getCustomData().get("FantasyAmuletMod");
                    if (!currState.containsKey(this.ship)) {
                        return null;
                    } else {
                        ModState shipState = (ModState)currState.get(this.ship);
                        if (param == null) {
                            return null;
                        } else if (!(param instanceof DamagingProjectileAPI)) {
                            return null;
                        } else if (!(target instanceof ShipAPI)) {
                            return null;
                        } else if (param instanceof MissileAPI) {
                            return null;
                        } else if (((DamagingProjectileAPI)param).isFromMissile()) {
                            return null;
                        } else if (param instanceof DamagingExplosion) {
                            return null;
                        } else {
                            if (shipState.timer >= 0.4F) {
                                for(WeaponAPI weapon : shipState.weapons) {
                                    if (weapon.getId().equals("FM_Amulet_B")) {
                                        engine.spawnProjectile(this.ship, weapon, "FM_Amulet_B", weapon.getLocation(), MathUtils.getRandomNumberInRange(weapon.getCurrAngle() - 7.5F, weapon.getCurrAngle() + 7.5F), new Vector2f());
                                        Global.getSoundPlayer().playSound("harpoon_fire", 10.0F, 0.5F, weapon.getLocation(), this.ship.getVelocity());
                                        MagicRender.battlespace(Global.getSettings().getSprite("fx", "FM_modeffect_4"), weapon.getLocation(), MathUtils.getRandomPointInCircle(weapon.getShip().getVelocity(), 20.0F), new Vector2f(30.0F, 30.0F), new Vector2f(10.0F, 10.0F), (float)MathUtils.getRandomNumberInRange(0, 360), 10.0F, new Color(236, 56, 56, 221), true, 0.1F, 0.6F, 0.3F);
                                    }
                                }

                                shipState.timer = 0.0F;
                            }

                            return this.ship.getId() + "_MikoHitListener";
                        }
                    }
                }
            }
        }
    }

    private static final class ModState {
        boolean isActive;
        List<WeaponAPI> weapons;
        float timer;
        float alphaForVisual;

        private ModState() {
            this.isActive = false;
            this.timer = 0.0F;
            this.alphaForVisual = 0.0F;
        }
    }
}
