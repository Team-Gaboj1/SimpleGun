package com.gaboj1.hdl.item;

import com.gaboj1.hdl.HDLMod;
import com.gaboj1.hdl.item.renderer.DesertEagleItemRenderer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;
import java.util.function.Consumer;

import static com.gaboj1.hdl.item.DesertEagleItem.numAmmoItemsInGun;

public class AWPItem extends Item implements GeoItem{
    public final static int DURABILITY = 10; //耐久
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, HDLMod.MOD_ID);

    public static final RegistryObject<Item> AWP = ITEMS.register("awp",
            () -> new Item(new Item.Properties()));

    public AWPItem() {
        super(new Properties().stacksTo(1).fireResistant().rarity(Rarity.EPIC).defaultDurability(DURABILITY));
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    public void zoomAnim(Level level, Player player, ItemStack stack){
        if (level instanceof ServerLevel serverLevel){
            triggerAnim(player, GeoItem.getOrAssignId(stack, serverLevel), "Zoom", "zoom");
        }

    }

    public void reloadAnim(Level level, Player player, ItemStack stack){
        if (level instanceof ServerLevel serverLevel)
            triggerAnim(player, GeoItem.getOrAssignId(stack, serverLevel), "Reload", "reload");
    }

    public void fireAnim(Level level, Player player, ItemStack stack){
        if (level instanceof ServerLevel serverLevel){
            triggerAnim(player, GeoItem.getOrAssignId(stack, serverLevel), "Fire", "fire");
        }

    }
    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        pTooltipComponents.add(Component.translatable("tooltip.holydinglegend.awp.tooltip"));
        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
    }

    public static ItemStack getBulletItemStack(ItemStack gun, int id) {
        // 如果枪械没有 NBT 标签，给它添加一个
        if (!gun.hasTag()) {
            gun.setTag(new CompoundTag());
            return ItemStack.EMPTY;
        }
        // 如果枪械的 NBT 标签中没有 "ammo" 标签，给它添加一个
        if (!gun.getTag().contains("ammo")) {
            ListTag ammoTagsList = new ListTag();
            for (int i = 0; i < numAmmoItemsInGun; i++) {
                ammoTagsList.add(new CompoundTag());
            }
            gun.getTag().put("ammo", ammoTagsList);
            return ItemStack.EMPTY;
        }
        // 获取子弹的 NBT 标签列表
        ListTag ammoTagsList = gun.getTag().getList("ammo", Tag.TAG_COMPOUND);
        // 获取特定位置的子弹的 NBT 标签
        CompoundTag ammoTags = ammoTagsList.getCompound(id);
        return ItemStack.of(ammoTags);
    }


    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {

        consumer.accept(new IClientItemExtensions() {
            private DesertEagleItemRenderer renderer;
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if(this.renderer == null){
                    renderer = new DesertEagleItemRenderer();
                }
                return this.renderer;
            }
        });
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack handItemStake = (hand == InteractionHand.MAIN_HAND?player.getMainHandItem():player.getOffhandItem());
        if(handItemStake.getItem() instanceof AWPItem){
            if (world instanceof ServerLevel serverLevel) {
                if(handItemStake.getItem() instanceof AWPItem handItem) {
                    handItem.zoomAnim(serverLevel, player, hand == InteractionHand.MAIN_HAND?player.getMainHandItem():player.getOffhandItem());
                }
            }
        }
        return InteractionResultHolder.pass(player.getItemInHand(hand));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
                controllerRegistrar.add(new AnimationController<GeoAnimatable>(this,"Fire",0,state -> PlayState.STOP)
                        .triggerableAnim("fire",RawAnimation.begin().thenPlay("animation.awp.fire")));

                controllerRegistrar.add(new AnimationController<GeoAnimatable>(this,"Zoom",0,state -> PlayState.STOP)
                         .triggerableAnim("zoom",RawAnimation.begin().thenPlay("animation.awp.zoom")));

                  controllerRegistrar.add(new AnimationController<GeoAnimatable>(this,"Reload",0,state -> PlayState.STOP)
                           .triggerableAnim("reload",RawAnimation.begin().thenPlay("animation.awp.reload")));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
