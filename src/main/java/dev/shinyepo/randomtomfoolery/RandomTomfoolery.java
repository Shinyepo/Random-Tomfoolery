package dev.shinyepo.randomtomfoolery;

import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.food.Foods;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.component.Fireworks;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.entity.EntityEvent;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.CriticalHitEvent;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
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
        if (chance < 2 && event.getContainer() instanceof ChestMenu) {
            var level = event.getEntity().level();
            var player = event.getEntity();
            level.playSound(null, player.blockPosition(), SoundEvents.CREEPER_PRIMED, SoundSource.AMBIENT, 1.0f, 1.0f);
            logJackpot(player,"opening a chest");
        }
    }

    @SubscribeEvent
    public void onLivingEatFood(LivingEntityUseItemEvent.Finish event) {
        var chance = generateRandom();
        if (chance < 5) {
            var item = event.getItem();
            ServerPlayer player = (ServerPlayer) event.getEntity();
            if (item.is(Items.MILK_BUCKET)) {
                player.sendSystemMessage(Component.literal("To mleko było skiśnięte"));
                player.hurtServer((ServerLevel) player.level(), player.damageSources().generic(),1F);
                player.getFoodData().setFoodLevel(0);
                player.addEffect(new MobEffectInstance(MobEffects.NAUSEA,200));
                logJackpot(player,"drinking milk");
            }
            if (item.is(Items.APPLE) || item.is(Items.CARROT) || item.is(Items.BREAD)) {
                player.sendSystemMessage(Component.literal("Połamałeś jedynki na tym"));
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
