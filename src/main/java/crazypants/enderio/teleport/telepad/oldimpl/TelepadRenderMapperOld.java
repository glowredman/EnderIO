package crazypants.enderio.teleport.telepad.oldimpl;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.apache.commons.lang3.tuple.Pair;

import crazypants.enderio.render.EnumRenderMode;
import crazypants.enderio.render.IBlockStateWrapper;
import crazypants.enderio.render.ICacheKey;
import crazypants.enderio.render.IOMode.EnumIOMode;
import crazypants.enderio.render.IRenderMapper;
import crazypants.enderio.render.pipeline.ItemQuadCollector;
import crazypants.enderio.render.pipeline.QuadCollector;

public class TelepadRenderMapperOld implements IRenderMapper.IBlockRenderMapper.IRenderLayerAware, IRenderMapper.IItemRenderMapper.IItemStateMapper {

  public static final TelepadRenderMapperOld instance = new TelepadRenderMapperOld();

  private static final EnumRenderMode GLASS_TOP_MODEL = EnumRenderMode.FRONT_ON;
  private static final EnumRenderMode FULL_MODEL = EnumRenderMode.FRONT_EAST;
  private static final EnumRenderMode SINGLE_MODEL = EnumRenderMode.FRONT;
  private static final EnumRenderMode SINGLE_MODEL_INVENTORY = EnumRenderMode.FRONT_SOUTH;

  protected TelepadRenderMapperOld() {
  }

  @Override
  @SideOnly(Side.CLIENT)
  public List<IBlockState> mapBlockRender(IBlockStateWrapper state, IBlockAccess world, BlockPos pos, BlockRenderLayer blockLayer,
                                          QuadCollector quadCollector) {
    TileEntity tileEntity = state.getTileEntity();

    if (tileEntity instanceof TileTelePadOld) {
      TileTelePadOld telePad = (TileTelePadOld) tileEntity;

      switch (blockLayer) {
      case SOLID:
        if (!telePad.inNetwork()) {
          return Collections.singletonList(state.getState().withProperty(EnumRenderMode.RENDER, SINGLE_MODEL));
        } else if (telePad.isMaster()) {
          return Collections.singletonList(state.getState().withProperty(EnumRenderMode.RENDER, FULL_MODEL));
        } else {
          return null;
        }
      case TRANSLUCENT:
        if (telePad.inNetwork() && telePad.isMaster()) {
          return Collections.singletonList(state.getState().withProperty(EnumRenderMode.RENDER, GLASS_TOP_MODEL));
        }
      default:
        return null;
      }

    } else if (blockLayer == BlockRenderLayer.CUTOUT) {
      return Collections.singletonList(state.getState().withProperty(EnumRenderMode.RENDER, SINGLE_MODEL));
    }

    return null;
  }

  @SuppressWarnings("deprecation")
  @Override
  @SideOnly(Side.CLIENT)
  public List<Pair<IBlockState, ItemStack>> mapItemRender(Block block, ItemStack stack, ItemQuadCollector itemQuadCollector) {
    return Collections.singletonList(Pair.of(block.getStateFromMeta(stack.getMetadata()).withProperty(EnumRenderMode.RENDER, SINGLE_MODEL_INVENTORY),
        (ItemStack) null));
  }

  @Override
  @SideOnly(Side.CLIENT)
  public EnumMap<EnumFacing, EnumIOMode> mapOverlayLayer(IBlockStateWrapper state, IBlockAccess world, BlockPos pos, boolean isPainted) {
    return null;
  }

  @Override
  @SideOnly(Side.CLIENT)
  public @Nonnull ICacheKey getCacheKey(@Nonnull Block block, @Nonnull ItemStack stack, @Nonnull ICacheKey cacheKey) {
    return cacheKey;
  }

}
