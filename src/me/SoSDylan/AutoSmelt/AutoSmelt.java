package me.SoSDylan.AutoSmelt;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Random;

public class AutoSmelt extends JavaPlugin implements Listener {
    @Override
    public void onEnable() {
        this.getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onBlockBreak(final BlockBreakEvent event) {
        // Don't do anything if the event has already been canceled.
        if (event.isCancelled())
            return;

        final Player player = event.getPlayer();

        // Check if the player has the correct permission.
        if (player.hasPermission("autosmelt.mine")) {
            final Block block = event.getBlock();
            final ItemStack hand = player.getInventory().getItemInMainHand();

            // Don't alter dropped items when using a silk touch pickaxe
            if (hand.containsEnchantment(Enchantment.SILK_TOUCH))
                return;

            Material drop;

            // Check what block type we mined, if it was gold or iron, store the smelted version in the 'drop' variable
            switch (block.getType()) {
                case GOLD_ORE:
                    drop = Material.GOLD_INGOT;
                    break;
                case IRON_ORE:
                    drop = Material.IRON_INGOT;
                    break;
                default:
                    drop = Material.AIR;
                    break;
            }

            // Check if the block would normally have dropped an item.
            if (!drop.equals(Material.AIR) && !block.getDrops(hand).isEmpty()) {
                int dropAmount = 1;

                // Check if item in main hand has fortune enchantment, if so randomize the drop rate from the enchantment level.
                if (hand.containsEnchantment(Enchantment.LOOT_BONUS_BLOCKS)) {
                    final Random rand = new Random();
                    dropAmount = rand.nextInt(hand.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS) + 1) + 1;
                }

                // Cancel the event to prevent the default item from being dropped.
                event.setCancelled(true);

                // Set the block broken to air and the event was canceled.
                block.setType(Material.AIR);
                // Drop the new smelted item from the original block position.
                block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(drop, dropAmount));

                // Spawn experience to match experience that emerald, diamond etc... would drop.
                block.getWorld().spawn(block.getLocation(), ExperienceOrb.class).setExperience(5);
            }
        }
    }
}
