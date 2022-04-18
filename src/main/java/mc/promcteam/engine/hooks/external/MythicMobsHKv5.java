package mc.promcteam.engine.hooks.external;

import io.lumine.mythic.api.MythicPlugin;
import io.lumine.mythic.api.MythicProvider;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.adapters.AbstractLocation;
import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.utils.serialize.Position;
import io.lumine.mythic.core.mobs.ActiveMob;
import io.lumine.mythic.core.skills.SkillMetadataImpl;
import io.lumine.mythic.core.skills.SkillTriggers;
import mc.promcteam.engine.NexEngine;
import mc.promcteam.engine.hooks.HookState;
import mc.promcteam.engine.hooks.NHook;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MythicMobsHKv5 extends NHook<NexEngine> implements IMythicHook {

    private MythicPlugin mm;

    public MythicMobsHKv5(NexEngine plugin) {
        super(plugin);
    }

    @Override
    @NotNull
    protected HookState setup() {
        this.mm = MythicProvider.get();
        return HookState.SUCCESS;
    }

    @Override
    public void shutdown() {

    }

    @Override
    public boolean isMythicMob(@NotNull Entity e) {
        return mm.getMobManager().getActiveMobs().stream()
                .filter(a -> a.getUniqueId().equals(e.getUniqueId())).findFirst().isPresent();
    }

    @Override
    public String getMythicNameByEntity(@NotNull Entity e) {
        MythicMob mob = getMythicInstance(e);
        return mob == null ? null : mob.getInternalName();
    }

    @Override
    public MythicMob getMythicInstance(@NotNull Entity e) {
        Optional<ActiveMob> mob = mm.getMobManager().getActiveMobs().stream()
                .filter(a -> a.getUniqueId().equals(e.getUniqueId())).findFirst();

        return mob.isPresent() ? mob.get().getType() : null;
    }

    @Override
    public boolean isDropTable(@NotNull String table) {
        return mm.getDropManager().getDropTable(table) != null && mm.getDropManager().getDropTable(table).isPresent();
    }

    @Override
    public double getLevel(@NotNull Entity e) {
        ActiveMob mob = getActiveMythicInstance(e);

        return mob != null ? mob.getLevel() : 1;
    }

    @NotNull
    @Override
    public List<String> getMythicIds() {
        return new ArrayList<>(mm.getMobManager().getMobNames());
    }

    @Override
    public void setSkillDamage(@NotNull Entity e, double d) {
        if (!isMythicMob(e)) return;
        ActiveMob am1 = getActiveMythicInstance(e);
        am1.setLastDamageSkillAmount(d);
    }

    @Override
    public void castSkill(@NotNull Entity e, @NotNull String skill) {
        ActiveMob mob = getActiveMythicInstance(e);
        if (mob == null) return;

        mm.getSkillManager().getSkill(skill).ifPresent(sk -> {
            sk.execute(new SkillMetadataImpl(SkillTriggers.API, mob, mob.getEntity()));
        });
    }

    @Override
    public void killMythic(@NotNull Entity e) {
        ActiveMob mob = getActiveMythicInstance(e);
        if (mob == null || mob.isDead()) return;

        mob.setDead();
        e.remove();
    }

    @Override
    public boolean isValid(@NotNull String name) {
        Optional<MythicMob> koke = mm.getMobManager().getMythicMob(name);
        return koke.isPresent();
    }

    @NotNull
    @Override
    public String getName(@NotNull String mobId) {
        Optional<MythicMob> koke = mm.getMobManager().getMythicMob(mobId);
        return koke.isPresent() ? koke.get().getDisplayName().get() : mobId;
    }

    @Nullable
    @Override
    public Entity spawnMythicMob(@NotNull String name, @NotNull Location loc, int level) {
        Optional<MythicMob> koke = mm.getMobManager().getMythicMob(name);
        if (koke.isPresent()) {
            MythicMob mob = koke.get();
            ActiveMob e   = mob.spawn(new AbstractLocation(Position.of(loc)), level);

            return e.getEntity().getBukkitEntity();
        }
        return null;
    }

    @Override
    public void taunt(LivingEntity target, LivingEntity source, double amount) {
        AbstractEntity abs = BukkitAdapter.adapt(source);
        if (amount > 0) {
            getActiveMythicInstance(target).getThreatTable().threatGain(abs, amount);
        } else if (amount < 0) {
            getActiveMythicInstance(target).getThreatTable().threatLoss(abs, -amount);
        }
    }

    public ActiveMob getActiveMythicInstance(@NotNull Entity e) {
        Optional<ActiveMob> mob = mm.getMobManager().getActiveMobs().stream()
                .filter(a -> a.getUniqueId().equals(e.getUniqueId())).findFirst();

        return mob.isPresent() ? mob.get() : null;
    }
}
