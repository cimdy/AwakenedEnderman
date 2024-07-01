package com.cimdy.awakenedenderman.Entity;

import com.cimdy.awakenedenderman.Attachment.AttachRegister;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.EnumSet;

public class EndermanFollowGoal extends Goal {
    private final TargetingConditions targetingConditions = TargetingConditions.forNonCombat().ignoreLineOfSight().selector(this::shouldFollow);
    protected final Mob mob;
    @Nullable
    protected Player player;
    private final double speedModifier;


    public EndermanFollowGoal(Mob mob, double pSpeedModifier) {
        this.mob = mob;
        this.speedModifier = pSpeedModifier;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    private boolean shouldFollow(LivingEntity livingEntity) {
        return this.mob.getData(AttachRegister.FOLLOW_PLAYER_UUID).equals(livingEntity.getStringUUID());
    }

    @Override
    public boolean canUse() {
        if(this.mob.hasData(AttachRegister.FOLLOW_PLAYER_UUID) && !this.mob.getData(AttachRegister.FOLLOW_PLAYER_UUID).equals("Null")){
            this.player = this.mob.level().getNearestPlayer(targetingConditions, this.mob);
            return this.player != null;
        }
        return false;
    }

    public static double getFollowRange(){
        double d = 6;
        return d * d;
    }

    @Override
    public boolean canContinueToUse() {
        if(this.mob.hasData(AttachRegister.FOLLOW_PLAYER_UUID) && !this.mob.getData(AttachRegister.FOLLOW_PLAYER_UUID).equals("Null")){
            this.player = this.mob.level().getNearestPlayer(targetingConditions, this.mob);
            return this.player != null;
        }
        return false;
    }

    @Override
    public void tick() {
        if (this.player != null) {
            this.mob.getLookControl().setLookAt(this.player, (float)(this.mob.getMaxHeadYRot() + 20), (float)this.mob.getMaxHeadXRot());
            this.mob.forceAddEffect(new MobEffectInstance(MobEffects.GLOWING,10,0), this.mob);
            double distanceToSqr = this.mob.distanceToSqr(this.player);
            double FollowRange = getFollowRange();
            if (distanceToSqr > FollowRange / 3)
                this.mob.getNavigation().moveTo(this.player, this.speedModifier);
        }
    }
}
