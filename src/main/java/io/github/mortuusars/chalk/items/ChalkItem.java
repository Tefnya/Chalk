package io.github.mortuusars.chalk.items;

import io.github.mortuusars.chalk.blocks.ChalkMarkBlock;
import io.github.mortuusars.chalk.setup.ModBlocks;
import io.github.mortuusars.chalk.utils.ClickLocationUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

import java.util.Random;

public class ChalkItem extends Item {

    public ChalkItem(Properties properties) {
        super(properties
                .tab(ItemGroup.TAB_TOOLS)
                .stacksTo(1)
                .defaultDurability(64)
                .setNoRepair());

    }

    @Override
    public int getEnchantmentValue() {
        return 0;
    }

    @Override
    public boolean isRepairable(ItemStack stack) {
        return false;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    @Override
    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return false;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return false;
    }

    //This is called when the item is used, before the block is activated.
    //Return PASS to allow vanilla handling, any other to skip normal code.
    @Override
    public ActionResultType onItemUseFirst(ItemStack stack, ItemUseContext context) {



        final World world = context.getLevel();
        final BlockPos pos = context.getClickedPos();
        Direction clickedFace = context.getClickedFace();
        final BlockState clickedBlockState = world.getBlockState(pos);
        BlockPos markPosition = pos.relative(clickedFace);
        final PlayerEntity player = context.getPlayer();

        if (clickedBlockState.getBlock() == ModBlocks.CHALK_MARK_BLOCK.get()) { // replace mark
            clickedFace = clickedBlockState.getValue(ChalkMarkBlock.FACING);
            markPosition = pos;
            world.removeBlock(pos, false);
        } else if (!Block.isFaceFull(clickedBlockState.getCollisionShape(world, pos, ISelectionContext.of(player)), clickedFace))
            return ActionResultType.PASS;
        else if ((!world.isEmptyBlock(markPosition) && world.getBlockState(markPosition).getBlock() != ModBlocks.CHALK_MARK_BLOCK.get()) ||
                stack.getItem() != this)
            return ActionResultType.PASS;

        if (world.isClientSide()) {
            Random r = new Random();
            world.addParticle(ParticleTypes.CLOUD, markPosition.getX() + (0.5 * (r.nextFloat() + 0.4)), markPosition.getY() + 0.65, markPosition.getZ() + (0.5 * (r.nextFloat() + 0.4)), 0.0D, 0.005D, 0.0D);
            return ActionResultType.PASS;
        }

        final int orientation = ClickLocationUtils.getBlockRegion(context.getClickLocation(), pos, clickedFace);

        BlockState blockState = ModBlocks.CHALK_MARK_BLOCK.get().defaultBlockState()
                .setValue(ChalkMarkBlock.FACING, clickedFace)
                .setValue(ChalkMarkBlock.ORIENTATION, orientation);

        if (world.setBlock(markPosition, blockState, 1 | 2)) {

            if (!player.isCreative()) {
                stack.setDamageValue(stack.getDamageValue() + 1);
                if (stack.getDamageValue() >= stack.getMaxDamage()) {
                    player.setItemInHand(context.getHand(), ItemStack.EMPTY);
                    world.playSound(null, markPosition, SoundEvents.GRAVEL_BREAK, SoundCategory.BLOCKS, 0.5f, 1f);
                }
            }

            world.playSound(null, markPosition, SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, SoundCategory.BLOCKS, 0.6f, random.nextFloat() * 0.2f + 0.8f);
            return ActionResultType.SUCCESS;
        }

        return ActionResultType.FAIL;
    }
}