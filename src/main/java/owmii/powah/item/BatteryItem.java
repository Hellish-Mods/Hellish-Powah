package owmii.powah.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import net.minecraftforge.energy.IEnergyStorage;
import owmii.lib.config.IEnergyConfig;
import owmii.lib.item.EnergyItem;
import owmii.lib.logistics.energy.Energy;
import owmii.powah.api.energy.endernetwork.IEnderExtender;
import owmii.powah.block.Tier;
import owmii.powah.config.Configs;
import owmii.powah.config.item.BatteryConfig;

public class BatteryItem extends EnergyItem<Tier, BatteryConfig, BatteryItem> implements IEnderExtender {
    boolean hasGlint = false;
    public BatteryItem(Item.Properties properties, Tier variant) {
        super(properties, variant);
        if (variant==Tier.CREATIVE) hasGlint = true;
    }

    @Override
    public IEnergyConfig<Tier> getConfig() {
        return Configs.BATTERY;
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int itemSlot, boolean isSelected) {
        if (entity instanceof PlayerEntity && isCharging(stack)) {
            Energy.ifPresent(stack, storage -> {
                if (storage instanceof Energy) {
                    ((Energy) storage).chargeInventory((PlayerEntity) entity, stack1 -> !(stack1.getItem() instanceof BatteryItem));
                }
            });
        }
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (player.isSneaking()) {
            switchCharging(stack);
            return ActionResult.resultSuccess(stack);
        }
        return super.onItemRightClick(world, player, hand);
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        IEnergyStorage energy = Energy.get(stack).orElse(Energy.Item.create(0));
        return energy.getEnergyStored() < energy.getMaxEnergyStored();
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        IEnergyStorage energy = Energy.get(stack).orElse(Energy.Item.create(0));
        return 1.0F - ((double) energy.getEnergyStored() / energy.getMaxEnergyStored());
    }

    @Override
    public boolean hasEffect(ItemStack stack) {
        return isCharging(stack) || hasGlint;
    }

    private void switchCharging(ItemStack stack) {
        setCharging(stack, !isCharging(stack));
    }

    private boolean isCharging(ItemStack stack) {
        return stack.getOrCreateTag().getBoolean("charging");
    }

    private void setCharging(ItemStack stack, boolean charging) {
        stack.getOrCreateTag().putBoolean("charging", charging);
    }

    @Override
    public long getExtendedCapacity(ItemStack stack) {
        return getConfig().getCapacity(getVariant());
    }

    @Override
    public long getExtendedEnergy(ItemStack stack) {
        return Energy.getStored(stack);
    }
}
