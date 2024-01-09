
package com.gaboj1.hdl.item;

import com.gaboj1.hdl.item.renderer.DesertEagleItemRenderer;
import com.gaboj1.hdl.procedures.DesertEagleRightClickAirProcedure;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.*;
import net.minecraftforge.registries.RegistryObject;

import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionHand;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;
import java.util.function.Consumer;

import static com.gaboj1.hdl.init.HDLModItems.DESERT_EAGLE_AMMO;

/**
 * 为了子沙鹰类做准备，省的写很多重复的代码
 */

public class DesertEagleItem extends Item implements GeoItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    public String animationprocedure = "empty";

    //public static final String RELOADING_DONE_TAG = "isReloading";
    private String textureResource = "textures/item/texturecrc.png";
    public boolean isReloading = false;
    public static ItemDisplayContext transformType;

    public static final int RELOAD_TIME = 2000;

    protected float fireDamage = (float) 0.45;;//伤害值

    protected int coolDownTick = 10;

    protected float power = 15;//初速度

    public static final int numAmmoItemsInGun = 1;

    public final static int MAX_AMMO = 7;

    protected RegistryObject<Item> ammoType = DESERT_EAGLE_AMMO;;

    public DesertEagleItem() {
        super(new Item.Properties().stacksTo(1).fireResistant().rarity(Rarity.EPIC).defaultDurability(MAX_AMMO));//引入弹匣了再把这个删了
        System.out.println(this.textureResource);
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    public DesertEagleItem(String textureResource) {
        super(new Item.Properties().stacksTo(1).fireResistant().rarity(Rarity.EPIC).defaultDurability(MAX_AMMO));//引入弹匣了再把这个删了
        this.textureResource = textureResource;
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    public DesertEagleItem(String textureResource, float fireDamage, int coolDownTick, float power) {
        super(new Item.Properties().stacksTo(1).fireResistant().rarity(Rarity.EPIC).defaultDurability(MAX_AMMO));//引入弹匣了再把这个删了
        this.textureResource = textureResource;
        this.fireDamage = fireDamage;
        this.coolDownTick = coolDownTick;
        this.power = power;
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return new DesertEagleItemRenderer(textureResource);//MCreator写final害我调半天...
            }
        });
    }


    public void getTransformType(ItemDisplayContext type) {
        this.transformType = type;
    }

    public void fireAnim(Level level, Player player, ItemStack stack){
        if (level instanceof ServerLevel serverLevel){
            triggerAnim(player, GeoItem.getOrAssignId(stack, serverLevel), "Fire", "fire");
        }

    }

    public void reloadAnim(Level level, Player player, ItemStack stack){
        if (level instanceof ServerLevel serverLevel)
            triggerAnim(player, GeoItem.getOrAssignId(stack, serverLevel), "Reload", "reload");
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar data) {
        data.add(new AnimationController<>(this, "Fire", 0, state -> PlayState.STOP)
                .triggerableAnim("fire", RawAnimation.begin().thenPlay("animation.DesertEagle.fire")));
        data.add(new AnimationController<>(this, "Reload", 0, state -> PlayState.STOP)
                .triggerableAnim("reload", RawAnimation.begin().thenPlay("animation.DesertEagle.reload")));

    }

    @Override
    public void verifyTagAfterLoad(CompoundTag p_150898_) {
        super.verifyTagAfterLoad(p_150898_);
        isReloading = false;//修复莫名换弹不了的bug
    }

    @Override
    public void onCraftedBy(ItemStack stack, Level p_41448_, Player p_41449_) {
        super.onCraftedBy(stack, p_41448_, p_41449_);
        ItemStack bullet = stack.copy();
        bullet.setCount(1);
        bullet.setDamageValue(bullet.getMaxDamage());
        setBulletItemStack(stack,bullet,0);//初始弹药应该为零。。
        //stack.setDamageValue(MAX_AMMO);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public int getUseDuration(ItemStack itemstack) {
        return 1;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player entity, InteractionHand hand) {
//		MouseHandler mouseHandler = Minecraft.getInstance().mouseHandler;
//		if(!mouseHandler.isRightPressed()){
        DesertEagleRightClickAirProcedure.execute(world, entity, hand);
        //}

        return InteractionResultHolder.pass(entity.getItemInHand(hand));

    }

    @Override
    public void appendHoverText(ItemStack itemstack, Level world, List<Component> list, TooltipFlag flag) {
        ItemStack bulletItemStack = getBulletItemStack(itemstack,0);
        int ammo = bulletItemStack.getMaxDamage()-bulletItemStack.getDamageValue();
        list.add(Component.translatable("info.simpledeserteagle.ammo_count").append(ammo+"/"+MAX_AMMO));
        list.add(Component.translatable("info.simpledeserteagle.ammo_damage").append(String.valueOf(fireDamage*16)));
        list.add(Component.translatable("info.simpledeserteagle.ammo_cooldown").append(String.format("%.2fs", coolDownTick*0.05)));
    }


    public float getFireDamage(){
        return fireDamage;
    }

    public int getCoolDownTick(){
        return coolDownTick;
    }

    public float getPower(){
        return power;
    }

    public RegistryObject<Item> getAmmoType(){
        return ammoType;
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

    public static void setBulletItemStack(ItemStack gun, ItemStack bullet, int id) {
        // 如果枪械没有 NBT 标签，给它添加一个
        if (!gun.hasTag()) {
            gun.setTag(new CompoundTag());
        }
        // 如果枪械的 NBT 标签中没有 "ammo" 标签，给它添加一个
        if (!gun.getTag().contains("ammo")) {
            ListTag ammoTagsList = new ListTag();
            for (int i = 0; i < numAmmoItemsInGun; i++) {
                ammoTagsList.add(new CompoundTag());
            }
            gun.getTag().put("ammo", ammoTagsList);
        }
        // 获取子弹的 NBT 标签列表
        ListTag ammoTagsList = gun.getTag().getList("ammo", Tag.TAG_COMPOUND);
        // 获取特定位置的子弹的 NBT 标签
        CompoundTag ammoTags = ammoTagsList.getCompound(id);
        // 如果子弹为空，将对应位置的 NBT 标签设为 null
        if (bullet.isEmpty()) {
            ammoTags = new CompoundTag();
        } else {
            // 将子弹的 NBT 标签应用到特定位置
            bullet.save(ammoTags);
        }

    }

}
