package com.echocore.entity;

import com.echocore.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraftforge.common.Tags;

import java.util.EnumSet;
import java.util.List;

/**
 * ECHO: sahibini koruyan, ceviren, tehditleri isaretleyen ve gizemli bir gecmisi olan ucan yapay zeka.
 * Hikaye, hafiza parcalari (Memory Fragment) ile 7 bolumde acilir.
 */
public class EchoEntity extends TamableAnimal implements FlyingAnimal {

    public static final int MAX_CHAPTER = 7;
    private static final int IDLE_LINES = 8;
    private static final int GLITCH_LINES = 5;

    private static final EntityDataAccessor<Boolean> DATA_GLITCH =
            SynchedEntityData.defineId(EchoEntity.class, EntityDataSerializers.BOOLEAN);

    private int chapter = 0;
    private int fragmentsGiven = 0;
    private int staleCycles = 0;
    private int storyTimer = 12000;
    private int chatterTimer = 4800;
    private int warnCooldown = 0;
    private int scanCooldown = 0;
    private int glitchTicks = 0;
    private boolean nightWarned = false;

    public EchoEntity(EntityType<? extends TamableAnimal> type, Level level) {
        super(type, level);
        this.moveControl = new FlyingMoveControl(this, 10, true);
        this.setNoGravity(true);
        this.setPathfindingMalus(BlockPathTypes.DANGER_FIRE, -1.0F);
        this.setPathfindingMalus(BlockPathTypes.WATER, -1.0F);
        this.setPathfindingMalus(BlockPathTypes.WATER_BORDER, 16.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 30.0D)
                .add(Attributes.FLYING_SPEED, 0.6D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.ATTACK_DAMAGE, 4.0D)
                .add(Attributes.FOLLOW_RANGE, 32.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_GLITCH, false);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation navigation = new FlyingPathNavigation(this, level);
        navigation.setCanOpenDoors(false);
        navigation.setCanFloat(true);
        navigation.setCanPassDoors(true);
        return navigation;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new StayGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2D, true));
        this.goalSelector.addGoal(3, new FollowOwnerGoal(this, 1.1D, 5.0F, 2.0F, true));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomFlyingGoal(this, 1.0D));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Monster.class, 10, true, false,
                target -> this.isTame() && !(target instanceof Creeper)));
    }

    /** Bekleme modu: ECHO oldugu yerde havada asili kalir. */
    private static class StayGoal extends Goal {
        private final EchoEntity echo;

        StayGoal(EchoEntity echo) {
            this.echo = echo;
            this.setFlags(EnumSet.of(Goal.Flag.JUMP, Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return echo.isTame() && echo.isOrderedToSit();
        }

        @Override
        public boolean canContinueToUse() {
            return canUse();
        }

        @Override
        public void start() {
            echo.getNavigation().stop();
        }
    }

    // ------------------------------------------------------------------
    // Durum
    // ------------------------------------------------------------------

    public boolean isGlitching() {
        return this.entityData.get(DATA_GLITCH);
    }

    private void startGlitch(int ticks) {
        this.glitchTicks = ticks;
        this.entityData.set(DATA_GLITCH, true);
    }

    @Override
    public boolean isFlying() {
        return !this.onGround();
    }

    public int getChapter() {
        return chapter;
    }

    public void restoreStory(int chapter, int fragmentsGiven) {
        this.chapter = Math.max(0, Math.min(MAX_CHAPTER, chapter));
        this.fragmentsGiven = Math.max(this.chapter, fragmentsGiven);
        if (this.chapter >= MAX_CHAPTER) {
            applyAscension();
        }
    }

    private void applyAscension() {
        AttributeInstance health = this.getAttribute(Attributes.MAX_HEALTH);
        if (health != null) {
            health.setBaseValue(60.0D);
        }
        AttributeInstance damage = this.getAttribute(Attributes.ATTACK_DAMAGE);
        if (damage != null) {
            damage.setBaseValue(8.0D);
        }
        this.setHealth(this.getMaxHealth());
    }

    // ------------------------------------------------------------------
    // Konusma
    // ------------------------------------------------------------------

    private static MutableComponent prefix() {
        return Component.literal("[ECHO] ").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD);
    }

    private static MutableComponent glitchPrefix() {
        return Component.literal("[").withStyle(ChatFormatting.RED)
                .append(Component.literal("ECHO").withStyle(ChatFormatting.RED, ChatFormatting.OBFUSCATED))
                .append(Component.literal("] ").withStyle(ChatFormatting.RED));
    }

    private void speak(Player player, String key, Object... args) {
        player.sendSystemMessage(prefix().append(Component.translatable(key, args).withStyle(ChatFormatting.GRAY)));
    }

    private void speakGlitch(Player player, String key, Object... args) {
        player.sendSystemMessage(glitchPrefix().append(Component.translatable(key, args).withStyle(ChatFormatting.DARK_RED)));
    }

    private void playGlitchSound() {
        this.level().playSound(null, this.blockPosition(), SoundEvents.BEACON_DEACTIVATE, SoundSource.NEUTRAL, 0.6F, 1.6F);
    }

    /** ECHO ilk kez (veya yeniden) uyandiginda calisir. */
    public void activate(Player player) {
        this.setOrderedToSit(false);
        this.setInSittingPose(false);
        this.startGlitch(60);
        this.level().playSound(null, this.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.NEUTRAL, 1.0F, 1.5F);
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.END_ROD, this.getX(), this.getY() + 0.5D, this.getZ(), 30, 0.4D, 0.4D, 0.4D, 0.05D);
        }
        if (this.chapter == 0) {
            speak(player, "echocore.line.boot.1");
            speak(player, "echocore.line.boot.2");
            speakGlitch(player, "echocore.line.boot.3");
        } else {
            speakGlitch(player, "echocore.line.reboot", this.chapter, MAX_CHAPTER);
        }
    }

    // ------------------------------------------------------------------
    // Etkilesim
    // ------------------------------------------------------------------

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        ItemStack stack = player.getItemInHand(hand);
        boolean client = this.level().isClientSide;

        if (!this.isTame()) {
            if (!client) {
                this.tame(player);
                this.activate(player);
            }
            return InteractionResult.sidedSuccess(client);
        }

        if (!this.isOwnedBy(player)) {
            if (!client) {
                speak(player, "echocore.line.stranger");
            }
            return InteractionResult.sidedSuccess(client);
        }

        if (stack.is(ModItems.MEMORY_FRAGMENT.get())) {
            if (!client) {
                consumeFragment(player, stack);
            }
            return InteractionResult.sidedSuccess(client);
        }

        if (stack.isEmpty()) {
            if (!client) {
                if (player.isShiftKeyDown()) {
                    boolean stay = !this.isOrderedToSit();
                    this.setOrderedToSit(stay);
                    this.setInSittingPose(stay);
                    this.getNavigation().stop();
                    speak(player, stay ? "echocore.line.stay" : "echocore.line.follow");
                } else {
                    scan(player);
                }
            }
            return InteractionResult.sidedSuccess(client);
        }

        return super.mobInteract(player, hand);
    }

    private void consumeFragment(Player player, ItemStack stack) {
        if (this.chapter >= MAX_CHAPTER) {
            speak(player, "echocore.line.fragment.done");
            return;
        }
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        this.chapter++;
        this.staleCycles = 0;
        this.fragmentsGiven = Math.max(this.fragmentsGiven, this.chapter);
        this.startGlitch(100);
        this.playGlitchSound();
        revealChapter(player);
    }

    private void revealChapter(Player player) {
        player.sendSystemMessage(Component.translatable("echocore.chapter.title", this.chapter, MAX_CHAPTER)
                .withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD));
        speak(player, "echocore.chapter." + this.chapter + ".1");
        speakGlitch(player, "echocore.chapter." + this.chapter + ".2");
        if (this.chapter >= MAX_CHAPTER) {
            ascend(player);
        }
    }

    private void ascend(Player player) {
        applyAscension();
        this.level().playSound(null, this.blockPosition(), SoundEvents.BEACON_POWER_SELECT, SoundSource.NEUTRAL, 1.0F, 1.2F);
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.END_ROD, this.getX(), this.getY() + 0.5D, this.getZ(), 80, 0.6D, 0.6D, 0.6D, 0.08D);
        }
        speak(player, "echocore.line.ascended");
    }

    private void scan(Player player) {
        if (this.scanCooldown > 0) {
            speak(player, "echocore.line.scan.cooldown", this.scanCooldown / 20 + 1);
            return;
        }
        this.scanCooldown = 600;
        this.level().playSound(null, this.blockPosition(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.NEUTRAL, 1.0F, 1.4F);

        BlockPos origin = this.blockPosition();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        BlockPos bestRare = null;
        BlockPos bestCommon = null;
        double rareDist = Double.MAX_VALUE;
        double commonDist = Double.MAX_VALUE;
        int radius = 12;

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    cursor.set(origin.getX() + x, origin.getY() + y, origin.getZ() + z);
                    if (!this.level().hasChunkAt(cursor)) {
                        continue;
                    }
                    BlockState state = this.level().getBlockState(cursor);
                    if (!state.is(Tags.Blocks.ORES)) {
                        continue;
                    }
                    double dist = cursor.distSqr(origin);
                    boolean common = state.is(Tags.Blocks.ORES_COAL) || state.is(Tags.Blocks.ORES_COPPER) || state.is(Tags.Blocks.ORES_IRON);
                    if (common) {
                        if (dist < commonDist) {
                            commonDist = dist;
                            bestCommon = cursor.immutable();
                        }
                    } else if (dist < rareDist) {
                        rareDist = dist;
                        bestRare = cursor.immutable();
                    }
                }
            }
        }

        BlockPos found = bestRare != null ? bestRare : bestCommon;
        double foundDist = bestRare != null ? rareDist : commonDist;
        if (found == null) {
            speak(player, "echocore.line.scan.none");
            return;
        }

        Component oreName = this.level().getBlockState(found).getBlock().getName();
        speak(player, "echocore.line.scan.found", oreName, found.getX(), found.getY(), found.getZ(), (int) Math.sqrt(foundDist));
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.END_ROD, found.getX() + 0.5D, found.getY() + 0.5D, found.getZ() + 0.5D, 25, 0.3D, 0.3D, 0.3D, 0.02D);
        }
    }

    // ------------------------------------------------------------------
    // Sunucu mantigi
    // ------------------------------------------------------------------

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide) {
            serverTick();
        }
    }

    private void serverTick() {
        if (this.glitchTicks > 0) {
            this.glitchTicks--;
            if (this.tickCount % 4 == 0 && this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, this.getX(), this.getY() + 0.5D, this.getZ(), 3, 0.3D, 0.3D, 0.3D, 0.05D);
            }
            if (this.glitchTicks == 0) {
                this.entityData.set(DATA_GLITCH, false);
            }
        }
        if (this.scanCooldown > 0) {
            this.scanCooldown--;
        }
        if (this.warnCooldown > 0) {
            this.warnCooldown--;
        }
        if (!this.isTame()) {
            return;
        }
        if (!(this.getOwner() instanceof ServerPlayer owner)) {
            return;
        }

        boolean near = this.distanceToSqr(owner) < 256.0D;

        if (near && this.tickCount % 200 == 0 && owner.getHealth() < owner.getMaxHealth()) {
            owner.heal(this.chapter >= MAX_CHAPTER ? 4.0F : 1.0F);
        }
        if (near && this.chapter >= MAX_CHAPTER && this.tickCount % 80 == 0) {
            owner.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 0, true, false));
        }

        if (this.tickCount % 40 == 0) {
            scanThreats(owner);
        }

        if (this.tickCount % 100 == 0) {
            boolean night = this.level().isNight();
            if (night && !this.nightWarned && near) {
                speak(owner, "echocore.line.night");
                this.nightWarned = true;
            } else if (!night) {
                this.nightWarned = false;
            }
        }

        if (near && --this.chatterTimer <= 0) {
            this.chatterTimer = 4800 + this.random.nextInt(4800);
            if (this.chapter >= 2 && this.random.nextInt(10) < 3) {
                speakGlitch(owner, "echocore.line.glitch." + (1 + this.random.nextInt(GLITCH_LINES)));
            } else {
                speak(owner, "echocore.line.idle." + (1 + this.random.nextInt(IDLE_LINES)));
            }
        }

        if (near && this.chapter >= 1 && this.tickCount % 400 == 0 && this.random.nextInt(10) == 0) {
            this.startGlitch(40);
            this.playGlitchSound();
            speakGlitch(owner, "echocore.line.glitch." + (1 + this.random.nextInt(GLITCH_LINES)));
        }

        if (--this.storyTimer <= 0) {
            this.storyTimer = 12000;
            storyBeat(owner);
        }
    }

    private void scanThreats(ServerPlayer owner) {
        List<Monster> monsters = this.level().getEntitiesOfClass(Monster.class, owner.getBoundingBox().inflate(14.0D), Monster::isAlive);
        if (monsters.isEmpty()) {
            return;
        }
        boolean creeper = false;
        for (Monster monster : monsters) {
            monster.addEffect(new MobEffectInstance(MobEffects.GLOWING, 100, 0, false, false));
            if (monster instanceof Creeper) {
                creeper = true;
            }
        }
        if (this.warnCooldown <= 0) {
            this.warnCooldown = 400;
            speak(owner, creeper ? "echocore.line.creeper" : "echocore.line.threat");
        }
    }

    private void storyBeat(ServerPlayer owner) {
        if (this.chapter >= MAX_CHAPTER) {
            return;
        }
        int outstanding = this.fragmentsGiven - this.chapter;
        if (outstanding <= 0 || ++this.staleCycles >= 2) {
            this.staleCycles = 0;
            this.fragmentsGiven = this.chapter + 1;
            this.startGlitch(120);
            this.playGlitchSound();
            speakGlitch(owner, "echocore.line.fragment.found");
            this.spawnAtLocation(new ItemStack(ModItems.MEMORY_FRAGMENT.get()));
        } else {
            speak(owner, "echocore.line.fragment.hint");
        }
    }

    // ------------------------------------------------------------------
    // Kayit / Olum / Ses
    // ------------------------------------------------------------------

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Chapter", this.chapter);
        tag.putInt("FragmentsGiven", this.fragmentsGiven);
        tag.putInt("StaleCycles", this.staleCycles);
        tag.putInt("StoryTimer", this.storyTimer);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.chapter = tag.getInt("Chapter");
        this.fragmentsGiven = tag.getInt("FragmentsGiven");
        this.staleCycles = tag.getInt("StaleCycles");
        if (tag.contains("StoryTimer")) {
            this.storyTimer = tag.getInt("StoryTimer");
        }
        this.setNoGravity(true);
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);
        if (this.isTame()) {
            ItemStack core = new ItemStack(ModItems.ECHO_CORE.get());
            core.getOrCreateTag().putInt("Chapter", this.chapter);
            core.getOrCreateTag().putInt("FragmentsGiven", this.fragmentsGiven);
            this.spawnAtLocation(core);
        }
    }

    @Override
    public void die(DamageSource source) {
        this.setNoGravity(false);
        super.die(source);
    }

    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob mate) {
        return null;
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return false;
    }

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.AMETHYST_BLOCK_CHIME;
    }

    @Override
    public int getAmbientSoundInterval() {
        return 320;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.IRON_GOLEM_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.IRON_GOLEM_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        return 0.5F;
    }
}
