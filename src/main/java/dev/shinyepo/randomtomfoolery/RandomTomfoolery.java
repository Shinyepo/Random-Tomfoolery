package dev.shinyepo.randomtomfoolery;

import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.Util;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.component.Fireworks;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.*;
import net.neoforged.neoforge.event.level.BlockEvent;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

import java.util.List;
import java.util.Random;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(RandomTomfoolery.MODID)
public class RandomTomfoolery
{
    // Define mod id in a common place for everything to reference
    public static final String MODID = "randomtomfoolery";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    private static final Random RANDOM = new Random();

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public RandomTomfoolery(IEventBus modEventBus, ModContainer modContainer)
    {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (ExampleMod) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void logJackpot(Player player, String type) {
        LOGGER.info("[{}] has hit a jackpot with {}!!!", player.getName().getString(), type);
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP");

        LOGGER.info(Config.magicNumberIntroduction + Config.magicNumber);
    }

    private int generateRandom() {
        return RANDOM.nextInt(101);
    }
    private int generateMediumRandom() {
        return RANDOM.nextInt(1001);
    }
    private int generateBiggerRandom() {
        return RANDOM.nextInt(10001);
    }

    @SubscribeEvent
    public void onLivingChestOpen(PlayerContainerEvent.Open event) {
        var chance = generateMediumRandom();
        if (chance < 20 && event.getContainer() instanceof ChestMenu) {
            var level = event.getEntity().level();
            var player = event.getEntity();
            level.playSound(null, player.blockPosition(), SoundEvents.GHAST_SCREAM, SoundSource.AMBIENT, 1.0f, 1.0f);
            logJackpot(player,"opening a chest");
        }
    }

    @SubscribeEvent
    public void onLivingEatFood(LivingEntityUseItemEvent.Finish event) {
        var chance = generateRandom();
        if (chance < 5 && event.getEntity() instanceof ServerPlayer player) {
            var item = event.getItem();
            if (item.is(Items.MILK_BUCKET)) {
                player.sendSystemMessage(Component.literal("To mleko było skiśnięte"));
                player.hurtServer((ServerLevel) player.level(), player.damageSources().generic(),1F);
                player.getFoodData().setFoodLevel(0);
                player.addEffect(new MobEffectInstance(MobEffects.NAUSEA,200));
                logJackpot(player,"drinking milk");
            }
            if (item.is(Items.APPLE) || item.is(Items.CARROT) || item.is(Items.BREAD) || item.is(Items.COOKED_BEEF) || item.is(Items.COOKED_CHICKEN) || item.is(Items.COOKED_PORKCHOP) || item.is(Items.COOKED_MUTTON)) {
                player.sendSystemMessage(Component.literal("Ugryzłeś coś twardego i połamałeś jedynki"));
                player.hurtServer((ServerLevel) player.level(), player.damageSources().generic(),4F);
                player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS,100));
                System.out.println(player.getName());
                logJackpot(player,"eating " + item.getDisplayName());
            }
        }
    }

    @SubscribeEvent
    public void onBlockDestroy(BlockEvent.BreakEvent event) {
        var chance = generateBiggerRandom();
        if (chance < 1) {
            var level = (ServerLevel) event.getPlayer().level();
            var pos = event.getPos().getCenter();
            level.explode(null,pos.x, pos.y, pos.z,0F,false, Level.ExplosionInteraction.NONE);
            logJackpot(event.getPlayer(),"destroying a block");
        }
    }



    @SubscribeEvent
    public void onZombieDeath(LivingDeathEvent event) {
        var entity = event.getEntity();
        var chance = generateRandom();
        if (chance < 5 && entity instanceof Zombie parent && !parent.isBaby() && event.getSource().getEntity() instanceof Player player) {
            var smallZombie1 = new Zombie(entity.level());
            var smallZombie2 = new Zombie(entity.level());
            smallZombie1.setBaby(true);
            smallZombie2.setBaby(true);
            smallZombie1.setPos(entity.position());
            smallZombie2.setPos(entity.position());

            entity.level().addFreshEntity(smallZombie1);
            entity.level().addFreshEntity(smallZombie2);
            logJackpot(player, "killing a zombie");
        }
    }

    @SubscribeEvent
    public void onFarmlandTrample(BlockEvent.FarmlandTrampleEvent event) {
        var chance = generateRandom();
        if (chance < 25) {
            var player = event.getEntity();
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.sendSystemMessage(Component.literal("NIE PO SIONYM KURRRWA!!!"));
                serverPlayer.hurtServer((ServerLevel) serverPlayer.level(), serverPlayer.damageSources().generic(),3F);
                logJackpot(serverPlayer,"destroying farmland");
            }
        }
    }

    @SubscribeEvent
    public void onWakeUp(PlayerWakeUpEvent event) {
        var chance = generateRandom();
        if (chance < 25 && event.getEntity() instanceof ServerPlayer serverPlayer) {
            var level = serverPlayer.level();
            var position = serverPlayer.position();
            var zombie1 = new Zombie(EntityType.ZOMBIE,level);
            var zombie2 = new Zombie(EntityType.ZOMBIE,level);
            var zombie3 = new Zombie(EntityType.ZOMBIE,level);

            zombie1.removeFreeWill();
            zombie2.removeFreeWill();
            zombie3.removeFreeWill();

            zombie1.setPos(new Vec3(position.x+2,position.y, position.z));
            zombie2.setPos(new Vec3(position.x,position.y, position.z+2));
            zombie3.setPos(new Vec3(position.x+2,position.y, position.z+2));

            zombie1.lookAt(EntityAnchorArgument.Anchor.EYES,serverPlayer.position());
            zombie2.lookAt(EntityAnchorArgument.Anchor.EYES,serverPlayer.position());
            zombie3.lookAt(EntityAnchorArgument.Anchor.EYES,serverPlayer.position());

            level.addFreshEntity(zombie1);
            level.addFreshEntity(zombie2);
            level.addFreshEntity(zombie3);
            serverPlayer.sendSystemMessage(Component.literal("NAD TWOIM SPIACYM CIALEM MASTURBOWALY SIE ZOMBIAKI!!!!"));
            logJackpot(serverPlayer,"waking up");
            }

    }

    @SubscribeEvent
    public void onCriticalHit(CriticalHitEvent event) {
        var chance = generateRandom();
        if (chance < 2 && event.isCriticalHit() && event.getTarget() instanceof LivingEntity) {
            var target = event.getTarget();
            var level = event.getEntity().level();
            ItemStack fireworkItem = new ItemStack(Items.FIREWORK_ROCKET);
            DyeColor dyecolor = Util.getRandom(DyeColor.values(), target.getRandom());
            fireworkItem.set(DataComponents.FIREWORKS,new Fireworks(
                    1,
                    List.of(new FireworkExplosion(FireworkExplosion.Shape.STAR, IntList.of(dyecolor.getFireworkColor()), IntList.of(), false, false))));

            var firework = new FireworkRocketEntity(level,
                    target.getX(),
                    target.getY()+1,
                    target.getZ(),
                    fireworkItem);

            level.addFreshEntity(firework);
            logJackpot(event.getEntity(),"critical hitting "+event.getTarget().getName());
        }
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
        // Do something when the server starts
        LOGGER.info("Random Tomfoolery is UP AND READY!");
    }
}
