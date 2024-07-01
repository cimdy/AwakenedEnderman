package com.cimdy.awakenedenderman.event;

import com.cimdy.awakenedenderman.Attachment.AttachRegister;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.CaveSpider;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingAttackEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.List;

public class LivingEvent {
    @SubscribeEvent
    public static void EntityTickEvent(EntityTickEvent.Pre event){
        if(event.getEntity() instanceof EnderMan enderMan && !enderMan.level().isClientSide){
            ServerLevel serverLevel = (ServerLevel) enderMan.level();
            RandomSource randomSource = serverLevel.random;
            int EXAMINE_TIME = enderMan.getData(AttachRegister.EXAMINE_TIME);

            if(EXAMINE_TIME <= 600){
                EXAMINE_TIME ++;
            }

            if(EXAMINE_TIME == 600 && randomSource.nextInt(100) + 1 <= 90){
                EXAMINE_TIME = 0;
            }

            if(EXAMINE_TIME >= 600 && !enderMan.hasData(AttachRegister.FOLLOW_PLAYER_UUID)){
                Player player = serverLevel.getNearestPlayer(enderMan, 32);//判断32格内是否存在玩家
                if (player != null) {
                    ItemStack itemStack = player.getItemBySlot(EquipmentSlot.HEAD);
                    enderMan.getLookControl().setLookAt(player, (float)(enderMan.getMaxHeadYRot() + 20), (float)enderMan.getMaxHeadXRot());
                    if (itemStack != ItemStack.EMPTY && itemStack.getItem().getMaxStackSize(itemStack) == 1) {//玩家是否装备南瓜头等以外的有效头盔 且末影人不在观测模式
                        player.sendSystemMessage(Component.translatable("message.awakened_enderman.enderman_mixin2"));
                        enderMan.setData(AttachRegister.FOLLOW_PLAYER_UUID, player.getStringUUID());
                    }
                }
            }

            if(enderMan.hasData(AttachRegister.FOLLOW_PLAYER_UUID) && !enderMan.getData(AttachRegister.FOLLOW_PLAYER_UUID).equals("Null")){
                EXAMINE_TIME ++;
            }

            if (EXAMINE_TIME >= 1800) {//超过一定时间
                List<Player> list = serverLevel.getEntitiesOfClass(Player.class, enderMan.getBoundingBox().inflate(24));
                for(Player target : list){
                    if(enderMan.getData(AttachRegister.FOLLOW_PLAYER_UUID).equals(target.getStringUUID())){
                        target.sendSystemMessage(Component.translatable("message.awakened_enderman.enderman_mixin3"));
                        enderMan.setTarget(target);//攻击玩家
                    }
                }
            }

            if(EXAMINE_TIME >= 1800){
                enderMan.removeData(AttachRegister.FOLLOW_PLAYER_UUID);
                EXAMINE_TIME = 0;
            }

            enderMan.setData(AttachRegister.EXAMINE_TIME, EXAMINE_TIME);
        }
    }




    public static void LivingAttackEvent(LivingAttackEvent event){
          LivingEntity entity = event.getEntity();
          Entity entity1 = event.getSource().getEntity();
          if(!entity.level().isClientSide){
               ServerLevel serverLevel = (ServerLevel) entity.level();
              RandomSource randomSource = serverLevel.random;
               if(entity1 instanceof LivingEntity target){
                   if(entity.getType() == EntityType.ENDERMAN){//当末影人被攻击时
                       int random = randomSource.nextInt(100) + 1;
                       if(random <= 10) {//攻击末影人有10%几率使手持物品掉落在末影人脚下
                           ItemStack itemStack = target.getMainHandItem().copyAndClear();
                           if (itemStack != ItemStack.EMPTY) {
                               ItemEntity itemEntity = new ItemEntity(EntityType.ITEM, entity.level());
                               itemEntity.setItem(itemStack);
                               itemEntity.moveTo(entity.getX(), entity.getY(), entity.getZ());
                               entity.onItemPickup(itemEntity);
                               entity.level().addFreshEntity(itemEntity);
                               random = 0;
                           }else {
                               random += 10;
                           }
                       }
                       if (random <= 20) {//有10%几率生成以攻击者为攻击目标的小僵尸
                           Zombie zombie = EntityType.ZOMBIE.create(entity.level());
                           if (zombie != null) {
                               zombie.moveTo(target.getX(), target.getY(), target.getZ(), 0, 0);
                               zombie.setBaby(true);
                               zombie.finalizeSpawn(serverLevel, entity.level().getCurrentDifficultyAt(target.getOnPos()), MobSpawnType.SPAWN_EGG, null);
                               target.level().addFreshEntity(zombie);
                           }
                       }else if (random <= 30) {//有10%几率生成以攻击者为攻击目标的蜘蛛
                           Spider spider = EntityType.SPIDER.create(target.level());
                           if (spider != null) {
                               spider.moveTo(target.getX(), target.getY(), target.getZ(), 0, 0);
                               spider.finalizeSpawn(serverLevel, entity.level().getCurrentDifficultyAt(target.getOnPos()), MobSpawnType.SPAWN_EGG, null);
                               spider.setTarget(target);
                               target.level().addFreshEntity(spider);
                           }
                       }else if (random <= 40) {//有10%几率生成2-3只以攻击者为攻击目标的洞穴蜘蛛
                           int num = randomSource.nextInt(3) + 1;
                           for (int a = 0; a < num; a++){
                               CaveSpider caveSpider = EntityType.CAVE_SPIDER.create(entity.level());
                               if (caveSpider != null) {
                                   caveSpider.moveTo(target.getX(), target.getY(), target.getZ(), 0, 0);
                                   caveSpider.finalizeSpawn(serverLevel, entity.level().getCurrentDifficultyAt(target.getOnPos()), MobSpawnType.SPAWN_EGG, null);
                                   caveSpider.setTarget(target);
                                   target.level().addFreshEntity(caveSpider);
                               }
                           }
                       }else if (random <= 50) {//有10%几率将攻击者位移到末影人位置
                           target.teleportTo(entity.getX(),entity.getY(),entity.getZ());
                       }
                   }
                   if(target.getType() == EntityType.ENDERMAN){//当末影人攻击时
                       int random = randomSource.nextInt(100) + 1;
                       if(random <= 50){//总随机概率
                           ItemStack itemStack = entity.getItemBySlot(EquipmentSlot.HEAD);
                           if(itemStack != ItemStack.EMPTY){//如果目标生物佩戴头盔将被夺走掉落在末影人脚下
                               itemStack.copyAndClear();
                               ItemEntity itemEntity = new ItemEntity(EntityType.ITEM,entity.level());
                               itemEntity.setItem(itemStack);
                               itemEntity.moveTo(entity.getX(),entity.getY(),entity.getZ());
                               entity.onItemPickup(itemEntity);
                               entity.level().addFreshEntity(itemEntity);
                               random = 0;//目标穿戴头盔 则随机项目必定是抢夺头盔
                           }
                           if(random <= 10){//有10%的概率抢夺其他位置盔甲
                               ItemStack itemStack1 = entity.getItemBySlot(EquipmentSlot.CHEST);
                               ItemStack itemStack2 = entity.getItemBySlot(EquipmentSlot.LEGS);
                               ItemStack itemStack3 = entity.getItemBySlot(EquipmentSlot.FEET);
                               if(itemStack1 != ItemStack.EMPTY){//如果目标生物穿戴胸甲将被夺走掉落在末影人脚下
                                   itemStack1.copyAndClear();
                                   ItemEntity itemEntity = new ItemEntity(EntityType.ITEM,entity.level());
                                   itemEntity.setItem(itemStack1);
                                   itemEntity.moveTo(entity.getX(),entity.getY(),entity.getZ());
                                   entity.onItemPickup(itemEntity);
                                   entity.level().addFreshEntity(itemEntity);
                                   random = 0;
                               }else if(itemStack2 != ItemStack.EMPTY){//如果目标生物穿戴护腿将被夺走掉落在末影人脚下
                                   itemStack2.copyAndClear();
                                   ItemEntity itemEntity = new ItemEntity(EntityType.ITEM,entity.level());
                                   itemEntity.setItem(itemStack2);
                                   itemEntity.moveTo(entity.getX(),entity.getY(),entity.getZ());
                                   entity.onItemPickup(itemEntity);
                                   entity.level().addFreshEntity(itemEntity);
                                   random = 0;
                               }else if(itemStack3 != ItemStack.EMPTY){//如果目标生物穿戴鞋子将被夺走掉落在末影人脚下
                                   itemStack3.copyAndClear();
                                   ItemEntity itemEntity = new ItemEntity(EntityType.ITEM,entity.level());
                                   itemEntity.setItem(itemStack3);
                                   itemEntity.moveTo(entity.getX(),entity.getY(),entity.getZ());
                                   entity.onItemPickup(itemEntity);
                                   entity.level().addFreshEntity(itemEntity);
                                   random = 0;
                               }else {
                                   random += 10;
                               }
                           }
                           if (random <= 20) {//有10%几率使目标 1级 失明10s
                               entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS,200,0));
                           }else if (random <= 30){//有10%几率将目标脚下方块挪放至两格高
                               BlockPos blockPos = new BlockPos (entity.getOnPos().getX(),entity.getOnPos().getY(),entity.getOnPos().getZ());
                               BlockPos blockPos2 = new BlockPos (entity.getOnPos().getX(),entity.getOnPos().getY() + 2,entity.getOnPos().getZ());
                               BlockState blockState = serverLevel.getBlockState(blockPos);
                               if(blockState != Blocks.BEDROCK.defaultBlockState() &&
                                       blockState != Blocks.END_PORTAL_FRAME.defaultBlockState()){//如果不是基岩或者末地传送门框架
                                   serverLevel.setBlock(blockPos2, blockState,3);
                                   serverLevel.setBlock(blockPos, Blocks.AIR.defaultBlockState(),3);
                               }
                           }
                       }
                   }
               }
          }
    }
}
