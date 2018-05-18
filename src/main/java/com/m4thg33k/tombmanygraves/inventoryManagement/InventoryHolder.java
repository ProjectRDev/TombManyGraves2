package com.m4thg33k.tombmanygraves.inventoryManagement;

import com.m4thg33k.tombmanygraves.blocks.ModBlocks;
import com.m4thg33k.tombmanygraves.items.ItemDeathList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class InventoryHolder {
    public static final String TAG_NAME = "InventoryHolder";
    public static final String EMPTY = "IsEmpty";
    public static final String TIMESTAMP = "Timestamp";
    public static final String PLAYER_NAME = "PlayerName";
    public static final String X = "Xcoord";
    public static final String Y = "Ycoord";
    public static final String Z = "Zcoord";

    private NBTTagCompound compound = new NBTTagCompound();
    private boolean isEmpty = true;
    private String timestamp = "";
    private int xcoord = - 1;
    private int ycoord = - 1;
    private int zcoord = - 1;
    private String playerName = "unknown";

    public InventoryHolder() {

    }

    public boolean isInventoryEmpty() {
        return isEmpty;
    }


    public void grabPlayerData(EntityPlayer player) {
        compound = SpecialInventoryManager.getInstance().grabItemsFromPlayer(player);
        isEmpty = compound == null;
        if (isEmpty) {
            compound = new NBTTagCompound();
        }

        playerName = player.getName();
        compound.setString(PLAYER_NAME, playerName);

        setTimestamp(new SimpleDateFormat("MM_dd_YYYY_HH_mm_ss").format(new Date()));
    }

    public void grabPlayerData(EntityPlayer player, BlockPos pos) {
        grabPlayerData(player);

        setPosition(pos);
    }

    public void setPosition(BlockPos pos) {
        xcoord = pos.getX();
        ycoord = pos.getY();
        zcoord = pos.getZ();

        compound.setInteger(X, xcoord);
        compound.setInteger(Y, ycoord);
        compound.setInteger(Z, zcoord);
    }

    public String getPlayerName() {
        return playerName;
    }

    public BlockPos getPosition() {
        return new BlockPos(xcoord, ycoord, zcoord);
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
        compound.setString(TIMESTAMP, timestamp);
    }

    public String getTimestamp() {
        return timestamp;
    }

    public static boolean isItemValidForGrave(ItemStack stack) {
        if (stack.isEmpty()
                || stack.getItem() instanceof ItemDeathList
                || stack.getItem() == Item.getItemFromBlock(ModBlocks.blockGrave)) {
            return false;
        }
        return true;
    }

    public NBTTagCompound writeToNBT(NBTTagCompound inCompound) {
        inCompound.setTag(TAG_NAME, this.compound);
        inCompound.setBoolean(EMPTY, this.isEmpty);
        return inCompound;
    }

    public void readFromNBT(NBTTagCompound inCompound) {
        if (inCompound.hasKey(TAG_NAME)) {
            this.compound = inCompound.getCompoundTag(TAG_NAME);
            this.isEmpty = inCompound.getBoolean(EMPTY);

            this.xcoord = compound.getInteger(X);
            this.ycoord = compound.getInteger(Y);
            this.zcoord = compound.getInteger(Z);

            this.timestamp = compound.getString(TIMESTAMP);
            this.playerName = compound.getString(PLAYER_NAME);
        } else {
            this.compound = new NBTTagCompound();
            this.isEmpty = true;

            this.xcoord = - 1;
            this.ycoord = - 1;
            this.zcoord = - 1;

            this.timestamp = "";
            this.playerName = "unknown";
        }
    }

    // Used to insert (put not replace) inventory items to a player
    public void insertInventory(EntityPlayer player) {
        if (isEmpty) {
            return;
        }

        SpecialInventoryManager.getInstance().insertInventory(player, compound, false);
    }

    // Used to force (replace) inventory items on the player
    public void forceInventory(EntityPlayer player) {
        if (isEmpty) {
            return;
        }

        SpecialInventoryManager.getInstance().insertInventory(player, compound, true);
    }

    // Used to drop all items at a specific player's location
    public void dropInventory(EntityPlayer player) {
        dropInventory(player.getEntityWorld(), player.getPosition());
    }

    // Used to drop all items at a specific position in a world
    public void dropInventory(World world, BlockPos pos) {
        SpecialInventoryManager
                .getInstance()
                .generateDrops(compound)
                .forEach(
                        itemStack -> InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), itemStack)
                );
    }

    public static void dropItem(EntityPlayer player, ItemStack stack) {
        InventoryHelper.spawnItemStack(player.getEntityWorld(), player.posX, player.posY, player.posZ, stack);
    }

    public static void dropItem(World world, BlockPos pos, ItemStack stack) {
        InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), stack);
    }

    public Map<String, Tuple<String, List<String>>> getItemStackStringsForGui() {
        return SpecialInventoryManager.getInstance().createItemListForGui(compound);
    }

}
