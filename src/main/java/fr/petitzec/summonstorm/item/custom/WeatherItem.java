package fr.petitzec.summonstorm.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class WeatherItem extends BowItem {
    public WeatherItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        player.startUsingItem(hand);
        System.out.println("useeeeeeeeee");
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }


    //@Override
    /*public int getUseDuration(ItemStack stack) {
        return 80;
    }*/


    @Override
    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack stack) {
        System.out.println("anim");
        return UseAnim.BOW; // Animation de charge (arc)
    }

    @Override
    public void onUseTick(Level level, @NotNull LivingEntity livingEntity, @NotNull ItemStack stack, int remainingUseTicks) {
        System.out.println("tick tick");
        if (level.isClientSide && livingEntity instanceof Player player) {
            int elapsed = Math.max(0, getUseDuration(stack, livingEntity) - remainingUseTicks);
            System.out.format("elapsed : %d", elapsed);

            for (int i = 0; i < elapsed / 10 + 1; i++) {
                level.addParticle(ParticleTypes.ENCHANT,
                        player.getX() + (level.random.nextDouble() - 0.5) * 1.5,
                        player.getY() + 1.0 + (level.random.nextDouble() * 0.5),
                        player.getZ() + (level.random.nextDouble() - 0.5) * 1.5,
                        0, 0.05, 0
                );
            }

            if (elapsed == 1) {
                player.playSound(SoundEvents.TOTEM_USE, 0.5f, 1.2f);
            }
        }
    }


    @Override
    public void releaseUsing(@NotNull ItemStack stack, Level level, @NotNull LivingEntity livingEntity, int remainingUseTicks) {
        int chargeTime = Math.max(0, getUseDuration(stack, livingEntity) - remainingUseTicks);
        System.out.format("charge time : %d", chargeTime);
        if (!level.isClientSide && livingEntity instanceof Player player && level instanceof ServerLevel serverLevel) {
            if (chargeTime < 80) {
                player.displayClientMessage(Component.literal("Charging interrupted.").withStyle(ChatFormatting.RED), true);
                return;
            }

            boolean isRaining = serverLevel.getLevelData().isRaining();
            boolean isThundering = serverLevel.getLevelData().isThundering();

            serverLevel.sendParticles(ParticleTypes.EXPLOSION, player.getX(), player.getY() + 1, player.getZ(), 20, 0.5, 0.5, 0.5, 0.1);

            if (!isRaining && !isThundering) {
                serverLevel.setWeatherParameters(0, 6000, true, false);
                player.displayClientMessage(Component.literal("The weather turns rainy...").withStyle(ChatFormatting.DARK_BLUE), true);
                serverLevel.playSound(null, player.blockPosition(), SoundEvents.WEATHER_RAIN, SoundSource.PLAYERS, 1.0f, 1.0f);
            } else if (isRaining && !isThundering) {
                serverLevel.setWeatherParameters(0, 6000, true, true);
                player.displayClientMessage(Component.literal("A storm is coming...").withStyle(ChatFormatting.DARK_GRAY), true);
                serverLevel.playSound(null, player.blockPosition(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 1.0f, 1.0f);
            } else {
                serverLevel.setWeatherParameters(0, 6000, false, false);
                player.displayClientMessage(Component.literal("Calm returns to the world.").withStyle(ChatFormatting.GOLD), true);
                serverLevel.playSound(null, player.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0f, 1.0f);
            }
        }
    }

}
