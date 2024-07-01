package com.cimdy.awakenedenderman;

import com.cimdy.awakenedenderman.Attachment.AttachRegister;
import com.cimdy.awakenedenderman.event.LivingEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

@Mod(AwakenedEnderman.MODID)
public class AwakenedEnderman
{
    public static final String MODID = "awakened_enderman";

    public AwakenedEnderman(IEventBus modEventBus)
    {
        AttachRegister.ATTACHMENT_TYPES.register(modEventBus);

        NeoForge.EVENT_BUS.register(this);
        NeoForge.EVENT_BUS.addListener(LivingEvent::LivingAttackEvent);
        NeoForge.EVENT_BUS.addListener(LivingEvent::EntityTickEvent);

    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {

    }

    @EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {

        }
    }
}
