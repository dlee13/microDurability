package dzwdz.microdurability;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;

public class EntryPoint implements ClientModInitializer {
    public static ModConfig config;
    public static Renderer renderer;

    @Override
    public void onInitializeClient() {
        renderer = new Renderer();

        AutoConfig.register(ModConfig.class, JanksonConfigSerializer::new);
        config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
    }

    public static boolean shouldWarn(ItemStack stack) {
        if (stack == null || !stack.isDamageable())
            return false;
        if (config.requireMending && EnchantmentHelper.getLevel(Enchantments.MENDING, stack) <= 0)
            return false;
        int durability = stack.getMaxDamage() - stack.getDamage();
        return durability < config.minDurability
                && durability * 100f / config.minPercent < stack.getMaxDamage();
    }
}
