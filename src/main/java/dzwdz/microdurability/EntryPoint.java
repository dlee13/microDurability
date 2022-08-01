package dzwdz.microdurability;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;

public class EntryPoint implements ClientModInitializer {

    public static ModConfig CONFIG;

    @Override
    public void onInitializeClient() {
        AutoConfig.register(ModConfig.class, JanksonConfigSerializer::new);
        CONFIG = AutoConfig.getConfigHolder(ModConfig.class).getConfig();

        DurabilityRenderer.EVENT.register(new DurabilityRenderer());
    }

    public static boolean shouldWarn(ItemStack stack) {
        if (stack == null || !stack.isDamageable())
            return false;
        if (CONFIG.requireMending && EnchantmentHelper.getLevel(Enchantments.MENDING, stack) <= 0)
            return false;
        int durability = stack.getMaxDamage() - stack.getDamage();
        return durability < CONFIG.minDurability
                && durability * 100f / CONFIG.minPercent < stack.getMaxDamage();
    }
}
