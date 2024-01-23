
package com.gaboj1.hdl.item;

import com.gaboj1.hdl.KeyMappingsTest;
import com.gaboj1.hdl.entity.BulletEntity;
import com.gaboj1.hdl.init.HDLModEntities;
import com.gaboj1.hdl.init.HDLModSounds;
import com.gaboj1.hdl.item.renderer.DesertEagleItemRenderer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.*;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
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
import software.bernie.geckolib.util.RenderUtils;

import java.util.List;
import java.util.function.Consumer;

import static com.gaboj1.hdl.init.HDLModItems.DESERT_EAGLE_AMMO;

/**
 * 为了子沙鹰类做准备，省的写很多重复的代码
 */

public class DesertEagleItem extends Item implements GeoItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

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
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    @Override
    public double getTick(Object itemStack) {
        return RenderUtils.getCurrentTick();
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
                return this.renderer;//MCreator写final害我调半天...
            }
        });
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
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        double x = player.getX();
        double y = player.getY();
        double z = player.getZ();
        if (player == null)
            return InteractionResultHolder.pass(player.getItemInHand(hand));
            ItemStack handItemStake = (hand == InteractionHand.MAIN_HAND?player.getMainHandItem():player.getOffhandItem());
            if(handItemStake.getItem() instanceof DesertEagleItem handItem){
                boolean isCooldown;
                isCooldown = player.getCooldowns().isOnCooldown(handItem);
                ItemStack bulletStack = DesertEagleItem.getBulletItemStack(handItemStake, 0);
                if (!handItem.isReloading && !bulletStack.isEmpty()&&bulletStack.getDamageValue() < bulletStack.getMaxDamage() &&!isCooldown) {

                    if (!player.isCreative()){
                        final ItemStack bullet = bulletStack;
                        final Integer bulletID1 = 0;
                        bullet.setDamageValue(bullet.getDamageValue() + 1);
                        //Update the stack in the gun
                        DesertEagleItem.setBulletItemStack(handItemStake, bullet, bulletID1);
                    }

                    if (world instanceof ServerLevel projectileLevel) {
                        Projectile _entityToSpawn =	new Object() {
                            public Projectile getArrow(Level level, Entity shooter, float damage, int knockBack, byte piercing) {
                                AbstractArrow entityToSpawn = new BulletEntity(HDLModEntities.DESERT_EAGLE_BULLET.get(), level);
                                entityToSpawn.setOwner(shooter);
                                entityToSpawn.setNoGravity(true);
                                entityToSpawn.setBaseDamage(damage);
                                entityToSpawn.setKnockback(knockBack);
                                entityToSpawn.setSilent(true);
                                entityToSpawn.setPierceLevel(piercing);
                                entityToSpawn.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
                                return entityToSpawn;
                            }
                        }.getArrow(projectileLevel, player, handItem.getFireDamage(), 1, (byte) 5);
                        _entityToSpawn.setPos(x, player.getEyeY() - (double)0.15F, z);
                        _entityToSpawn.shoot(player.getViewVector(1).x, player.getViewVector(1).y, player.getViewVector(1).z, handItem.getPower(), 0);
                        projectileLevel.addFreshEntity(_entityToSpawn);
                    }

                    if (world instanceof ServerLevel serverLevel) {
                        handItem.fireAnim(serverLevel, player, hand == InteractionHand.MAIN_HAND?player.getMainHandItem():player.getOffhandItem());

                    }

                    player.getCooldowns().addCooldown(handItem, handItem.getCoolDownTick());

                    if (!world.isClientSide()) {
                        //播放音效
                        world.playSound(null, BlockPos.containing(x, y, z), HDLModSounds.DESERTEAGLECRCFIRE.get(), SoundSource.PLAYERS, 1, 1);
                    } else {
                        //实现抖动
                        double[] recoilTimer = {0}; // 后坐力计时器
                        double totalTime = 100;
                        int sleepTime = 2; //sleeptime后坐力需要修改
                        double recoilDuration = totalTime / sleepTime; // 后坐力持续时间
                        float speed = (float) ((Math.random() * 2) - 1) / 10;
                        Runnable recoilRunnable = () -> {
                            //开始抖动(简单匀速运动，不够真实。。)
                            while (recoilTimer[0] < recoilDuration) {
                                // 逐渐调整玩家的视角
                                float newPitch = player.getXRot() - (float) 0.2;//实时获取，以防鼠标冲突
                                float newYaw = player.getYRot() - speed;
                                player.setYRot(newYaw);
                                player.setXRot(newPitch);
                                player.yRotO = player.getYRot();
                                player.xRotO = player.getXRot();
                                recoilTimer[0]++; // 计时器递增
                                try {
                                    Thread.sleep(sleepTime);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                            //归位
                            while (recoilTimer[0] > 0) {
                                float newPitch = player.getXRot() + (float) 0.2;
                                float newYaw = player.getYRot() + speed;
                                player.setXRot(newPitch);
                                player.setYRot(newYaw);
                                player.xRotO = player.getXRot();
                                player.yRotO = player.getYRot();
                                recoilTimer[0]--; // 计时器递增
                                try {
                                    Thread.sleep(sleepTime);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        };
                        Thread recoilThread = new Thread(recoilRunnable);
                        recoilThread.start();
                    }

                    //显示当前左右手的弹药数
                    showAmmoCount(world, hand == InteractionHand.MAIN_HAND,player,handItemStake);

                } else if (hand == InteractionHand.MAIN_HAND && player.getOffhandItem().getItem() instanceof DesertEagleItem) {//如果副手有枪就使用副手试试
                    player.getOffhandItem().getItem().use(world, player,InteractionHand.OFF_HAND);
                } else {//都没有就需要换弹了
//					DesertEagleReloadProcedure.execute(world, player);
                    if (world instanceof ServerLevel _level)
                        _level.getServer().getCommands().performPrefixedCommand(new CommandSourceStack(CommandSource.NULL, new Vec3(x, y, z), Vec2.ZERO, _level, 4, "", Component.literal(""), _level.getServer(), null).withSuppressedOutput(),
                                "title @p actionbar \""+I18n.get("tips.simpledeserteagle.reloadbutton", KeyMappingsTest.RELOAD.saveString().toUpperCase())+"\"");

                }
            }
        return InteractionResultHolder.pass(player.getItemInHand(hand));
    }

    /**
     * 显示当前双手子弹数量
     */
    public static void showAmmoCount(Level world,boolean isMainHand,Player player,ItemStack handItemStake){
        if(!(handItemStake.getItem() instanceof DesertEagleItem)){
            return;
        }
        if (world instanceof ServerLevel _level){

            ItemStack anotherHandItemStake = player.getItemInHand(isMainHand?InteractionHand.OFF_HAND:InteractionHand.MAIN_HAND);
            String content = (isMainHand?I18n.get("tips.simpledeserteagle.main_hand_ammo"):" "+I18n.get("tips.simpledeserteagle.off_hand_ammo")) +getBulletCount(handItemStake)+ "/" + DesertEagleItem.MAX_AMMO;

            if(anotherHandItemStake.getItem() instanceof DesertEagleItem){
                content = I18n.get("tips.simpledeserteagle.off_hand_ammo")+ ( isMainHand?getBulletCount(anotherHandItemStake):getBulletCount(handItemStake)) + "/"+ DesertEagleItem.MAX_AMMO+
                        "      "+I18n.get("tips.simpledeserteagle.main_hand_ammo")+( isMainHand?getBulletCount(handItemStake):getBulletCount(anotherHandItemStake) )+ "/"+ DesertEagleItem.MAX_AMMO;
            }

            _level.getServer().getCommands().performPrefixedCommand(new CommandSourceStack(CommandSource.NULL, new Vec3(player.getX(), player.getY(), player.getZ()), Vec2.ZERO, _level, 4, "", Component.literal(""), _level.getServer(), null).withSuppressedOutput(),
                    "title @p actionbar \"" +content+"\"");
        }
    }

    @Override
    public void appendHoverText(ItemStack itemstack, Level world, List<Component> list, TooltipFlag flag) {
        ItemStack bulletItemStack = getBulletItemStack(itemstack,0);
        int ammo = bulletItemStack.getMaxDamage()-bulletItemStack.getDamageValue();
        list.add(Component.translatable("info.simpledeserteagle.ammo_count").append(ammo+"/"+MAX_AMMO));
        //list.add(Component.translatable("info.simpledeserteagle.ammo_damage").append(String.valueOf(fireDamage*16)));
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

    /**
     * 复合NBT，实现换弹的同时不会让物品播放切换动画，从Flan枪械那里改的
    * */
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

    public static void reload(LevelAccessor world, Entity entity){
        if (entity == null)
            return;
        double x = entity.getX();
        double y = entity.getY();
        double z = entity.getZ();

        //分别判断左右手是否满弹药并做出换弹处理（为什么当时打算搞双持啊啊啊）
        if(entity instanceof LivingEntity _livEnt){
            ItemStack mainHandItem = _livEnt.getMainHandItem();
            ItemStack offhandItem = _livEnt.getOffhandItem();
            if(mainHandItem.getItem() instanceof DesertEagleItem item /*mainHandItem.getOrCreateTag().getBoolean(FatherDesertEagleItem.RELOADING_DONE_TAG)*/){
                if(isFull(mainHandItem)){
                    if (world instanceof ServerLevel _level)
                        _level.getServer().getCommands().performPrefixedCommand(new CommandSourceStack(CommandSource.NULL, new Vec3(x, y, z), Vec2.ZERO, _level, 4, "", Component.literal(""), _level.getServer(), null).withSuppressedOutput(),
                                "title @p actionbar \""+ I18n.get("tips.simpledeserteagle.main_ammo_full")+"\"");
                } else if (item.isReloading) {
                    if (world instanceof ServerLevel _level)
                        _level.getServer().getCommands().performPrefixedCommand(new CommandSourceStack(CommandSource.NULL, new Vec3(x, y, z), Vec2.ZERO, _level, 4, "", Component.literal(""), _level.getServer(), null).withSuppressedOutput(),
                                "title @p actionbar \""+I18n.get("tips.simpledeserteagle.reloading")+"\"");
                }else doReload(_livEnt.getMainHandItem(),entity,world,item.getAmmoType().get(),true);
            }
            if(offhandItem.getItem() instanceof DesertEagleItem item && /*offhandItem.getOrCreateTag().getBoolean(FatherDesertEagleItem.RELOADING_DONE_TAG)*/!item.isReloading  && !isFull(offhandItem)){
                if(isFull(offhandItem)){
                    if (world instanceof ServerLevel _level)
                        _level.getServer().getCommands().performPrefixedCommand(new CommandSourceStack(CommandSource.NULL, new Vec3(x, y, z), Vec2.ZERO, _level, 4, "", Component.literal(""), _level.getServer(), null).withSuppressedOutput(),
                                "title @p actionbar \""+I18n.get("tips.simpledeserteagle.off_ammo_full")+"\"");
                } else if (item.isReloading) {
                    if (world instanceof ServerLevel _level)
                        _level.getServer().getCommands().performPrefixedCommand(new CommandSourceStack(CommandSource.NULL, new Vec3(x, y, z), Vec2.ZERO, _level, 4, "", Component.literal(""), _level.getServer(), null).withSuppressedOutput(),
                                "title @p actionbar \""+I18n.get("tips.simpledeserteagle.reloading")+"\"");
                }else doReload(_livEnt.getOffhandItem(),entity,world,item.getAmmoType().get(),false);
            }
        }

    }

    private static void doReload(ItemStack handItemStake, Entity entity, LevelAccessor world, Item ammo, boolean isMainHand) {
        double x = entity.getX();
        double y = entity.getY();
        double z = entity.getZ();

        //延迟实现换弹逻辑，等动画和音效放完
        new Thread(() -> {//防止sleep卡死
            try {
                int need = 0;
                ItemStack bullet = DesertEagleItem.getBulletItemStack(handItemStake,0);
                if(bullet.isEmpty()){
                    need = DesertEagleItem.MAX_AMMO;
                }else need = bullet.getDamageValue();
                Player player = (Player)entity;
                int total = searchItem(player,ammo,need);
                if(total>0){
                    DesertEagleItem handItem = (DesertEagleItem) handItemStake.getItem();
                    handItem.isReloading = true;//限制同时换弹
                    //handItemStake.getOrCreateTag().putBoolean(FatherDesertEagleItem.RELOADING_DONE_TAG,false);
                    //播放动画
                    if (world instanceof ServerLevel serverLevel) {
                        //防止开火时换弹
                        if(player.getCooldowns().isOnCooldown(handItemStake.getItem()))return;
                        //播放动画
                        ((DesertEagleItem)handItemStake.getItem()).reloadAnim(serverLevel, player, handItemStake);
                        //播放音效
                        //serverLevel.playSound(player, x,y,z, HDLModSounds.DESERTEAGLECRCRELOAD.get(), SoundSource.HOSTILE, 1, 1);
                        serverLevel.playSound(null, BlockPos.containing(x, y, z), HDLModSounds.DESERTEAGLECRCRELOAD.get(), SoundSource.PLAYERS, 1, 1);
                    }

                    Thread.sleep(DesertEagleItem.RELOAD_TIME);
                    ItemStack newBullet = handItemStake.copy();
                    newBullet.setCount(1);
                    newBullet.setDamageValue(need - total);
                    DesertEagleItem.setBulletItemStack(handItemStake,newBullet,0);
                    handItem.isReloading = false;
                    //显示子弹数量信息
//                    if (world instanceof ServerLevel _level){
//
//                        ItemStack anotherHandItemStake = player.getItemInHand(isMainHand?InteractionHand.OFF_HAND:InteractionHand.MAIN_HAND);
//                        String content = (isMainHand? I18n.get("tips.simpledeserteagle.main_hand_ammo"):" "+I18n.get("tips.simpledeserteagle.off_hand_ammo")) +getBulletCount(handItemStake)+ "/" + DesertEagleItem.MAX_AMMO;
//
//                        if(anotherHandItemStake.getItem() instanceof DesertEagleItem){
//                            content = I18n.get("tips.simpledeserteagle.off_hand_ammo")+ ( isMainHand?getBulletCount(anotherHandItemStake):getBulletCount(handItemStake)) + "/"+ DesertEagleItem.MAX_AMMO+
//                                    "      "+I18n.get("tips.simpledeserteagle.main_hand_ammo")+( isMainHand?getBulletCount(handItemStake):getBulletCount(anotherHandItemStake) )+ "/"+ DesertEagleItem.MAX_AMMO;
//                        }
//
//                        _level.getServer().getCommands().performPrefixedCommand(new CommandSourceStack(CommandSource.NULL, new Vec3(x, y, z), Vec2.ZERO, _level, 4, "", Component.literal(""), _level.getServer(), null).withSuppressedOutput(),
//                                "title @p actionbar \"" +content+"\"");
//                    }
                    showAmmoCount((Level) world, isMainHand, player, handItemStake);
                }else{
                    if (world instanceof ServerLevel _level)
                        _level.getServer().getCommands().performPrefixedCommand(new CommandSourceStack(CommandSource.NULL, new Vec3(x, y, z), Vec2.ZERO, _level, 4, "", Component.literal(""), _level.getServer(), null).withSuppressedOutput(),
                                "title @p actionbar \""+I18n.get("tips.simpledeserteagle.no_ammo")+"\"");
                }
            } catch (Exception e) {//Exception高发地，实在不会搞
                throw new RuntimeException(e);
            }

        }).start();
    }

    //递归搜索物品栈
    private static int searchItem(Player player, Item ammo,int need){
        int total = 0;
        ItemStack stack = ItemStack.EMPTY;
        if(ammo == player.getMainHandItem().getItem()){
            stack = player.getMainHandItem();
        }else if(ammo == player.getOffhandItem().getItem()){
            stack = player.getOffhandItem();
        }else {
            for (int i = 0; i < player.getInventory().items.size(); i++) {
                ItemStack teststack = player.getInventory().items.get(i);
                if (teststack != null && teststack.getItem() == ammo ) {
                    stack = teststack;
                    break;
                }
            }
        }

        if (stack != ItemStack.EMPTY) {
            if (stack.getCount() >= need) {
                stack.shrink(need);
                return need;
            } else {
                int cnt = stack.getCount();
                stack.shrink(cnt);
                total += cnt;
                total += searchItem(player,ammo,need - cnt);
                return total;
            }
        }else{
            return 0;
        }
    }

    private static int getBulletCount(ItemStack stack){
        if(stack.getItem() instanceof DesertEagleItem){
            ItemStack bullet = DesertEagleItem.getBulletItemStack(stack,0);
            return bullet.getMaxDamage()-bullet.getDamageValue();
        }
        return 0;
    }

    private static boolean isFull(ItemStack gun){
        ItemStack bullet = DesertEagleItem.getBulletItemStack(gun,0);
        if(bullet.isEmpty())return false;
        return bullet.getDamageValue()==0;
    }

}
