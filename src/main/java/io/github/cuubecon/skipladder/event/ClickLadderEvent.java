package io.github.cuubecon.skipladder.event;

import io.github.cuubecon.skipladder.config.SkipLadderConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.item.*;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;

import static net.minecraft.world.level.block.StairBlock.FACING;

/**
 * Self-subscribing Event Class
 *
 * @author CubeCon
 */
@Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.FORGE)
public class ClickLadderEvent
{
    private final static int[][] offsetArray = {{0,1}, {1,1}, {1,0}, {1,-1}, {0,-1}, {-1,-1}, {-1,0}, {-1,1}};


    /**
     * Called every time the player right-clicks a Block.
     *
     * @param event RightClickBlockEvent
     */
    @SubscribeEvent
    public static void onBlocksRightClick(PlayerInteractEvent.RightClickBlock event)
    {
        Level world = event.getWorld();

        if(!world.isClientSide)
       {
           BlockPos pos = event.getPos();
           Player player = event.getPlayer();
           int foodLevel = player.getFoodData().getFoodLevel();
           Iterable<ItemStack> stack = player.getHandSlots();

          for (ItemStack itemstack : stack)
          {
               if(isInvalidItem(itemstack.getItem()))
                   return;
           }

           if(foodLevel < 5 && !player.isCreative())
           {
               player.displayClientMessage(new TranslatableComponent("skipladder.message.morefood"), true);
               return;
           }

           if(world.getBlockState(pos).getBlock().equals(Blocks.LADDER))
           {
               double lY;
               BlockPos posL;
               double lookY_UP = pos.getY()+1;
               double lookY_DOWN = pos.getY()-1;
               double lookY_UP2 = pos.getY()+2;
               double lookY_DOWN2 = pos.getY()-2;
               boolean up = false;

               BlockPos posUP = new BlockPos(pos.getX(),lookY_UP,pos.getZ());
               BlockPos posDOWN = new BlockPos(pos.getX(),lookY_DOWN,pos.getZ());

               BlockPos posUP2 = new BlockPos(pos.getX(),lookY_UP2,pos.getZ());
               BlockPos posDOWN2 = new BlockPos(pos.getX(),lookY_DOWN2,pos.getZ());

               if((world.getBlockState(posUP).getBlock().equals(Blocks.LADDER) && world.getBlockState(posUP2).getBlock().equals(Blocks.LADDER))
                       & (world.getBlockState(posDOWN).getBlock().equals(Blocks.LADDER) && world.getBlockState(posDOWN2).getBlock().equals(Blocks.LADDER)))
                   return;

               if(world.getBlockState(posUP).getBlock().equals(Blocks.LADDER) & world.getBlockState(posUP2).getBlock().equals(Blocks.LADDER))
               {
                   up = true;
                   posL = posUP;
               }
               else
                   posL = posDOWN;

               int counter = 0;

               while(world.getBlockState(posL).getBlock().equals(Blocks.LADDER))
               {
                   counter++;

                   if(up)
                       lY = posL.getY()+1;
                   else
                       lY = posL.getY()-1;

                   posL = new BlockPos(posL.getX(),lY,posL.getZ());
               }
               teleportPlayer(world, player, foodLevel, posL, counter);
           }
           else if(world.getBlockState(pos).getBlock().equals(BlockTags.STAIRS))
           {

               BlockState state = world.getBlockState(pos);
               BlockPos posUP = null;
               BlockPos posDOWN = null;
               BlockPos posUP2 = null;
               BlockPos posDOWN2 = null;
               boolean south, north, east, west;
               south = north = east = west = false;
                if(state.getValue(FACING).getName().equals("south"))
                {
                    south = true;
                    posUP = new BlockPos(pos.getX(),pos.getY()+1,pos.getZ()+1);
                    posDOWN = new BlockPos(pos.getX(),pos.getY()-1,pos.getZ()-1);

                    posUP2 = new BlockPos(pos.getX(),pos.getY()+2,pos.getZ()+2);
                    posDOWN2 = new BlockPos(pos.getX(),pos.getY()-2,pos.getZ()-2);
                }
                else if(state.getValue(FACING).getName().equals("north"))
                {
                    north = true;
                    posUP = new BlockPos(pos.getX(),pos.getY()+1,pos.getZ()-1);
                    posDOWN = new BlockPos(pos.getX(),pos.getY()-1,pos.getZ()+1);

                    posUP2 = new BlockPos(pos.getX(),pos.getY()+2,pos.getZ()-2);
                    posDOWN2 = new BlockPos(pos.getX(),pos.getY()-2,pos.getZ()+2);
                }
                else if(state.getValue(FACING).getName().equals("east"))
                {
                    east = true;
                    posUP = new BlockPos(pos.getX()+1,pos.getY()+1,pos.getZ());
                    posDOWN = new BlockPos(pos.getX()-1,pos.getY()-1,pos.getZ());

                    posUP2 = new BlockPos(pos.getX()+2,pos.getY()+2,pos.getZ());
                    posDOWN2 = new BlockPos(pos.getX()-2,pos.getY()-2,pos.getZ());
                }
                else if(state.getValue(FACING).getName().equals("west"))
                {
                    west = true;
                    posUP = new BlockPos(pos.getX()-1,pos.getY()+1,pos.getZ());
                    posDOWN = new BlockPos(pos.getX()+1,pos.getY()-1,pos.getZ());

                    posUP2 = new BlockPos(pos.getX()-2,pos.getY()+2,pos.getZ());
                    posDOWN2 = new BlockPos(pos.getX()+2,pos.getY()-2,pos.getZ());
                }

               BlockPos posL;
               boolean up = false;



               if((world.getBlockState(posUP).getBlock().equals(BlockTags.STAIRS) && world.getBlockState(posUP2).getBlock().equals(BlockTags.STAIRS))
                       & (world.getBlockState(posDOWN).getBlock().equals(BlockTags.STAIRS) && world.getBlockState(posDOWN2).getBlock().equals(BlockTags.STAIRS)))
                   return;

               if(world.getBlockState(posUP).getBlock().equals(BlockTags.STAIRS) & world.getBlockState(posUP2).getBlock().equals(BlockTags.STAIRS))
               {
                   up = true;
                   posL = posUP;
               }
               else
                   posL = posDOWN;

               double lY, lX = posL.getX(), lZ = posL.getZ();
               int counter = 0;
               while(world.getBlockState(posL).getBlock().equals(BlockTags.STAIRS))
               {
                   counter++;
                   if(up)
                   {
                       lY = posL.getY()+1;
                       if(south)
                           lZ = posL.getZ()+1;
                       else if(north)
                           lZ = posL.getZ()-1;
                       else if(west)
                           lX = posL.getX()-1;
                       else if(east)
                           lX = posL.getX()+1;
                   }
                   else
                   {
                       lY = posL.getY()-1;
                       if(south)
                           lZ = posL.getZ()-1;
                       else if(north)
                           lZ = posL.getZ()+1;
                       else if(west)
                           lX = posL.getX()+1;
                       else if(east)
                           lX = posL.getX()-1;
                   }



                   posL = new BlockPos(lX,lY,lZ);
               }

               teleportPlayer(world, player, foodLevel, posL, counter);
           }
       }


    }

    /**
     * Teleport the player to a safe space, remove foodlevel when not in creative and play a teleport sound.
     *
     * @param world Minecraft world
     * @param player Minecraft Player
     * @param foodLevel Foodlevel of the player
     * @param posL Position to teleport
     * @param counter Distance in blocks, needed to calculate foodlevel amount to remove
     */
    private static void teleportPlayer(Level world, Player player, int foodLevel, BlockPos posL, int counter) {
        if(counter > SkipLadderConfig.max_amount.get())
            counter = SkipLadderConfig.max_amount.get();

        int fooddecrease = (counter / SkipLadderConfig.hunger_amount.get());

        if(fooddecrease >= foodLevel)
            fooddecrease = foodLevel - 2;

        if(fooddecrease <= 0)
            fooddecrease = 1;

        Vec3 targetblock = findSafeTeleportLocation(world, posL);
        if(targetblock != null)
            player.teleportTo(targetblock.x, targetblock.y, targetblock.z);
        else
            player.teleportTo(posL.getX()+0.5, posL.getY()+1.0, posL.getZ()+0.5);


        if(!player.isCreative() && SkipLadderConfig.remove_hunger.get())
            player.getFoodData().setFoodLevel(foodLevel - fooddecrease);

        if(SkipLadderConfig.play_teleportsound.get())
            world.playSound(null, posL, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 10, 1);
    }

    /**
     * Find a safe location to teleport the player. Copy from {@link  net.minecraft.world.level.block.BedBlock#findStandUpPositionAtOffset(EntityType, CollisionGetter, BlockPos, int[][], boolean)}
     *
     * @param world Minecraft World
     * @param pos Startposition
     * @return Vector3d or null if no location found
     */
    @Nullable
    private static Vec3 findSafeTeleportLocation(Level world, BlockPos pos)
    {
        BlockPos.MutableBlockPos blockpos$mutable = new BlockPos.MutableBlockPos();
        for (int[] offset: offsetArray)
        {
            blockpos$mutable.set(pos.getX() + offset[0],pos.getY(), pos.getZ() + offset[1]);
            Vec3 vector3d = DismountHelper.findSafeDismountLocation(EntityType.PLAYER, world, blockpos$mutable, false);
            if(vector3d != null)
                return vector3d;
        }
        return null;
    }

    /**
     * Check if with this item in hand we can teleport a player without side effects
     *
     * @param item Minecraft Item in hand
     * @return boolean
     */
    private static boolean isInvalidItem(Item item)
    {
        boolean is_invalid = false;

        if(item instanceof BlockItem)
            is_invalid = true;
        else if(item instanceof PotionItem)
            is_invalid = true;
        else if(item instanceof EnderpearlItem)
            is_invalid = true;
        else if(item instanceof SnowballItem)
            is_invalid = true;

        return is_invalid;
    }
}
