package dzwdz.microdurability;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.World;

public class EntryPoint implements ClientModInitializer {

    public static ModConfig CONFIG;

    @Override
    public void onInitializeClient() {
        AutoConfig.register(ModConfig.class, JanksonConfigSerializer::new);
        CONFIG = AutoConfig.getConfigHolder(ModConfig.class).getConfig();

        DurabilityRenderer.EVENT.register(new DurabilityRenderer());
    }

    public static boolean shouldWarn(final ItemStack stack, final World world) {
        if (stack == null || !stack.isDamageable())
            return false;
        if (CONFIG.requireMending) {
            final var mending = world.getRegistryManager().getWrapperOrThrow(RegistryKeys.ENCHANTMENT)
                    .getOptional(Enchantments.MENDING);
            if (mending.isPresent() && EnchantmentHelper.getLevel(mending.get(), stack) <= 0) {
                return false;
            }
        }
        int durability = stack.getMaxDamage() - stack.getDamage();
        return durability < CONFIG.minDurability
                && durability * 100f / CONFIG.minPercent < stack.getMaxDamage();
    }
}
