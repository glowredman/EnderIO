package crazypants.enderio.teleport.telepad;

import java.util.List;

import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.registry.GameRegistry;
import crazypants.enderio.EnderIOTab;
import crazypants.enderio.ModObject;
import crazypants.enderio.api.teleport.ITelePad;
import crazypants.enderio.gui.IResourceTooltipProvider;
import crazypants.enderio.teleport.anchor.BlockTravelAnchor;
import crazypants.util.BlockCoord;
import crazypants.util.Lang;
import crazypants.util.Util;
import crazypants.vecmath.Vector3d;

public class ItemCoordSelector extends Item implements IResourceTooltipProvider {

  public static ItemCoordSelector create() {
    ItemCoordSelector ret = new ItemCoordSelector();
    GameRegistry.registerItem(ret, ModObject.itemCoordSelector.unlocalisedName);
    return ret;
  }

  private ItemCoordSelector() {
    setCreativeTab(EnderIOTab.tabEnderIO);
    setUnlocalizedName(ModObject.itemCoordSelector.unlocalisedName);
    setTextureName("EnderIO:" + ModObject.itemCoordSelector.unlocalisedName);
    setMaxStackSize(1);
  }

  @SuppressWarnings("unchecked")
  @Override
  public void getSubItems(Item item, CreativeTabs p_150895_2_, @SuppressWarnings("rawtypes") List list) {
    ItemStack stack = new ItemStack(item);
    init(stack);
    list.add(stack);
  }

  @Override
  public void onCreated(ItemStack stack, World world, EntityPlayer player) {
    super.onCreated(stack, world, player);
    init(stack);
  }

  private void init(ItemStack stack) {
    stack.stackTagCompound = new NBTTagCompound();
    new BlockCoord().writeToNBT(stack.stackTagCompound);
    stack.stackTagCompound.setBoolean("default", true);
  }
  
  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Override
  public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean p_77624_4_) {
    if(!stack.stackTagCompound.getBoolean("default")) {
      list.add(getCoords(stack).chatString(EnumChatFormatting.GRAY));
    }
    super.addInformation(stack, player, list, p_77624_4_);
  }

  @Override
  public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
    if (rayTraceCoords(stack, world, player)) {
      player.swingItem();
    }
    return super.onItemRightClick(stack, world, player);
  }

  @Override
  public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {

    if (!rayTraceCoords(stack, world, player)) {
      return false;
    }

    TileEntity te = world.getTileEntity(x, y, z);
    if(te instanceof ITelePad) {
      ITelePad tp = (ITelePad) te;
      if(tp.canBlockBeAccessed(player)) {
        BlockCoord bc = getCoords(stack);
        BlockCoord cur = new BlockCoord(tp.getX(), tp.getY(), tp.getZ());
        if(!bc.equals(cur)) {
          tp.setCoords(bc);
          if(!world.isRemote) {
            player.addChatMessage(new ChatComponentText(Lang.localize("itemCoordSelector.chat.setCoords", bc.chatString())));
          }
        } else {
          return false;
        }
      } else {
        BlockTravelAnchor.sendPrivateChatMessage(player, tp.getPlacedBy());
      }
    }
    
    
    if(world.isRemote) {
      sendItemUsePacket(stack, player, world, x, y, z, side, hitX, hitY, hitZ);
    }
    
    return true;
  }

  // returns false if the raytrace provided no new information
  private boolean rayTraceCoords(ItemStack stack, World world, EntityPlayer player) {
    Vector3d headVec = Util.getEyePositionEio(player);
    Vec3 start = headVec.getVec3();
    Vec3 lookVec = player.getLook(1.0F);
    double reach = 500;
    headVec.add(lookVec.xCoord * reach, lookVec.yCoord * reach, lookVec.zCoord * reach);
    MovingObjectPosition mop = world.rayTraceBlocks(start, headVec.getVec3());
    if (mop == null) {
      return false;
    }
    
    BlockCoord bc = new BlockCoord(mop);
    BlockCoord onStack = getCoords(stack);
    TileEntity te = bc.getTileEntity(world);
    if(te instanceof ITelePad) {
      return true;
    }

    if(!player.isSneaking()) {
      ForgeDirection dir = ForgeDirection.VALID_DIRECTIONS[mop.sideHit];
      bc = bc.getLocation(dir);
    }

    if(!bc.equals(onStack)) {
      setCoords(stack, bc);
      onCoordsChanged(player, bc);
      return true;
    }
    
    return false;
  }

  private void sendItemUsePacket(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
    NetHandlerPlayClient netClientHandler = (NetHandlerPlayClient) FMLClientHandler.instance().getClientPlayHandler();
    netClientHandler.addToSendQueue(new C08PacketPlayerBlockPlacement(x, y, z, side, player.inventory.getCurrentItem(), hitX, hitY, hitZ));
  }

  private void onCoordsChanged(EntityPlayer player, BlockCoord bc) {
    if(!player.worldObj.isRemote) {
      player.addChatMessage(new ChatComponentText(Lang.localize("itemCoordSelector.chat.newCoords", bc.chatString())));
    }
  }

  public void setCoords(ItemStack stack, BlockCoord bc) {
    stack.stackTagCompound.setBoolean("default", false);
    bc.writeToNBT(stack.stackTagCompound);
  }

  public BlockCoord getCoords(ItemStack stack) {
    return new BlockCoord().readFromNBT(stack.stackTagCompound);
  }

  @Override
  public String getUnlocalizedNameForTooltip(ItemStack itemStack) {
    return getUnlocalizedName();
  }
}
