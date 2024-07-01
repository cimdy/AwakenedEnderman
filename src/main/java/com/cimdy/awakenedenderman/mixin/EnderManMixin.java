package com.cimdy.awakenedenderman.mixin;

import com.cimdy.awakenedenderman.Attachment.AttachRegister;
import com.cimdy.awakenedenderman.Entity.EndermanFollowGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnderMan.class)
public abstract class EnderManMixin extends Monster{

    @Shadow protected abstract boolean teleport();

    protected EnderManMixin(EntityType<? extends Monster> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Inject(method = "registerGoals", at = @At("TAIL"))
    private void registerGoals(CallbackInfo ci){
        this.goalSelector.addGoal(1, new EndermanFollowGoal(this, 1.0));
    }

    @Inject(method = "requiresCustomPersistence", at = @At("RETURN"), cancellable = true)//末影人不再具有持久性
    private void requiresCustomPersistence(CallbackInfoReturnable<Boolean> cir){
        cir.setReturnValue(false);
    }

    @Inject(method = "hurt", at = @At("HEAD"))
    private void hurt(DamageSource pSource, float pAmount, CallbackInfoReturnable<Boolean> cir){
        if(pSource.getEntity() instanceof LivingEntity){
            this.setData(AttachRegister.EXAMINE_TIME, 0);
        }
    }

    @Override
    protected InteractionResult mobInteract(Player pPlayer, InteractionHand pHand) {//右键末影人事件
        if (this.hasData(AttachRegister.FOLLOW_PLAYER_UUID) && !this.getData(AttachRegister.FOLLOW_PLAYER_UUID).equals("Null")) {//如果处于观测模式
            if(!pPlayer.level().isClientSide){
                ItemStack itemStack = pPlayer.getMainHandItem();
                if (getEquipmentSlotForItem(itemStack) == EquipmentSlot.HEAD) {//对末影人使用能装备在头上的物品
                    if (itemStack.getItem().getMaxStackSize(itemStack) != 1) {//如果使用雕刻南瓜或者各种没用的怪物头等等能堆叠的会激怒末影人
                        pPlayer.sendSystemMessage(Component.translatable("message.awakened_enderman.enderman_mixin4"));
                        this.setTarget(pPlayer);
                    } else {//正确使用头盔后触发事件
                        this.awakenedEnderman_NeoForge_1_21_0$EnderManInteractHead(pPlayer);
                        itemStack.shrink(1);//扣除物品

                        this.removeData(AttachRegister.FOLLOW_PLAYER_UUID);//重置观察
                        this.setData(AttachRegister.EXAMINE_TIME, 0);

                        pPlayer.sendSystemMessage(Component.translatable("message.awakened_enderman.enderman_mixin5"));
                        this.teleport();//一次随机传送
                        return InteractionResult.SUCCESS;
                    }
                }
            }
        }
        return super.mobInteract(pPlayer, pHand);
    }

    @Unique
    private void awakenedEnderman_NeoForge_1_21_0$EnderManInteractHead(Player player) {
        if (!player.level().isClientSide) {//生成凋零
            ServerLevel serverLevel = (ServerLevel) this.level();
            ItemStack itemStack = player.getMainHandItem();
            int damageValue = itemStack.getMaxDamage() - itemStack.getDamageValue();
            if (damageValue <= 54) {
                WitherBoss wither = EntityType.WITHER.create(player.level());
                if (wither != null) {
                    wither.moveTo(player.getX(), player.getY(), player.getZ(), 0, 0);
                    wither.finalizeSpawn(serverLevel, player.level().getCurrentDifficultyAt(player.getOnPos()), MobSpawnType.NATURAL, null);
                    wither.setTarget(player);
                    player.level().addFreshEntity(wither);
                }
            }else if (damageValue <= 76) {//生成一个一秒爆炸的TNT
                PrimedTnt primedTnt = EntityType.TNT.create(player.level());
                if (primedTnt != null) {
                    primedTnt.moveTo(player.getX(), player.getY(), player.getZ(), 0, 0);
                    primedTnt.setFuse(20);
                    player.level().addFreshEntity(primedTnt);
                }
            }else if (damageValue <= 164) {//生成苦力怕
                Creeper creeper = EntityType.CREEPER.create(player.level());
                if (creeper != null) {
                    creeper.moveTo(player.getX(), player.getY(), player.getZ(), 0, 0);
                    creeper.finalizeSpawn(serverLevel, player.level().getCurrentDifficultyAt(player.getOnPos()), MobSpawnType.NATURAL, null);
                    creeper.setTarget(player);
                    player.level().addFreshEntity(creeper);
                }
            }else if(damageValue <= 362) {//放置一个铁矿
                BlockPos blockPos = new BlockPos(this.getBlockX(), this.getBlockY(), this.getBlockZ());
                BlockState blockstate = Blocks.IRON_ORE.defaultBlockState();
                serverLevel.setBlock(blockPos, blockstate, 3);
            }else if(damageValue <= 406){//放置一个钻石矿
                BlockPos blockPos = new BlockPos(this.getBlockX(), this.getBlockY(), this.getBlockZ());
                BlockState blockstate = Blocks.DIAMOND_ORE.defaultBlockState();
                serverLevel.setBlock(blockPos, blockstate,3);
            }else{//放置一个下界残骸
                BlockPos blockPos = new BlockPos(this.getBlockX(), this.getBlockY(), this.getBlockZ());
                BlockState blockstate = Blocks.ANCIENT_DEBRIS.defaultBlockState();
                serverLevel.setBlock(blockPos, blockstate,3);
            }
        }
    }
}
