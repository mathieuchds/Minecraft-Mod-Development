package fr.petitzec.summonstorm.item.custom;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;

public class WeatherItem extends Item {

    public WeatherItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        // Seulement côté serveur
        if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
            // Déclenche la pluie pendant 6000 ticks (5 minutes)
            serverLevel.setWeatherParameters(0, 6000, true, false);

            // Message de debug (facultatif)
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("Le ciel s'assombrit..."));
        }

        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
    }
}
