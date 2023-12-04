package mdteam.ait.core.blockentities;

import com.mojang.logging.LogUtils;
import io.wispforest.owo.nbt.NbtKey;
import mdteam.ait.AITMod;
import mdteam.ait.api.tardis.ILinkable;
import mdteam.ait.client.animation.ExteriorAnimation;
import mdteam.ait.client.animation.PulsatingAnimation;
import mdteam.ait.client.renderers.exteriors.ExteriorEnum;
import mdteam.ait.client.renderers.exteriors.MaterialStateEnum;
import mdteam.ait.core.AITBlockEntityTypes;
import mdteam.ait.core.AITItems;
import mdteam.ait.core.helper.TardisUtil;
import mdteam.ait.core.item.KeyItem;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.AnimationState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import the.mdteam.ait.*;

import java.util.Objects;
import java.util.UUID;

import static mdteam.ait.AITMod.EXTERIORNBT;
import static the.mdteam.ait.TardisTravel.State.LANDED;

public class ExteriorBlockEntity extends BlockEntity implements ILinkable {

    private Tardis tardis;
    public final AnimationState ANIMATION_STATE = new AnimationState();
    private ExteriorAnimation animation;

    public ExteriorBlockEntity(BlockPos pos, BlockState state) {
        super(AITBlockEntityTypes.EXTERIOR_BLOCK_ENTITY_TYPE, pos, state);
    }

    public void useOn(ServerWorld world, boolean sneaking, PlayerEntity player) {

        if(player == null)
            return;

        if(player.getMainHandStack().getItem() == AITItems.GOLDEN_TARDIS_KEY) {
            ItemStack key = player.getMainHandStack();
            NbtCompound tag = key.getOrCreateNbt();
            if(!tag.contains("tardis")) {
                return;
            }
            if(Objects.equals(this.getTardis().getUuid().toString(), tag.getUuid("tardis").toString())) {
                this.tardis.setLockedTardis(!this.getTardis().getLockedTardis());
                this.setLeftDoorRot(0);
                this.setRightDoorRot(0);
                String lockedState = this.getTardis().getLockedTardis() ? "\uD83D\uDD12" : "\uD83D\uDD13";
                player.sendMessage(Text.literal(lockedState), true);
                world.playSound(null, pos, SoundEvents.BLOCK_CHAIN_BREAK, SoundCategory.BLOCKS, 0.6F, 1F);
            } else {
                world.playSound(null, pos, SoundEvents.BLOCK_NOTE_BLOCK_BIT.value(), SoundCategory.BLOCKS, 1F, 0.2F);
                player.sendMessage(Text.literal("TARDIS does not identify with key"), true);
            }
            return;
        }

        System.out.println("WHAT?? " + this.getTardis().getTravel().getState());

        // fixme this sucks
        if(this.tardis.getTravel().getState() == LANDED) {
            if (!this.tardis.getLockedTardis()) {
                if(this.tardis.getExterior().getType().isDoubleDoor()) {
                    if (this.getRightDoorRotation() == 1.2f && this.getLeftDoorRotation() == 1.2f) {
                        this.setLeftDoorRot(0);
                        this.setRightDoorRot(0);
                    } else {
                        this.setRightDoorRot(this.getLeftDoorRotation() == 0 ? 0 : 1.2f);
                        this.setLeftDoorRot(1.2f);
                    }
                }
                else
                    this.setLeftDoorRot(this.getLeftDoorRotation() == 0 ? 1.2f : 0);
                world.playSound(null, pos, SoundEvents.BLOCK_IRON_DOOR_OPEN, SoundCategory.BLOCKS, 0.6f, 1f);
            } else {
                world.playSound(null, pos, SoundEvents.BLOCK_CHAIN_STEP, SoundCategory.BLOCKS, 0.6F, 1F);
                player.sendMessage(Text.literal("\uD83D\uDD12"), true);
            }
        }

        if (sneaking)
            return;

        DoorBlockEntity door = TardisUtil.getDoor(this.tardis);

        if(this.tardis.getTravel().getState() == LANDED)
            if (door != null) {
                TardisUtil.getTardisDimension().getChunk(door.getPos()); // force load the chunk

                door.setLeftDoorRot(this.getLeftDoorRotation());
                door.setRightDoorRot(this.getRightDoorRotation());
            }
    }

    public float[] getCorrectDoorRotations() {
        if(this.tardis != null) {
            return this.tardis.getExterior().getType().isDoubleDoor() ? new float[]{this.getLeftDoorRotation(), this.getRightDoorRotation()} : new float[]{this.getLeftDoorRotation()};
        }
        return null;
    }

    public void setExterior(ExteriorEnum exterior) {
        EXTERIORNBT.get(this).setExterior(exterior);
    }

    @Deprecated
    public ExteriorEnum getExterior() {
        return EXTERIORNBT.get(this).getExterior();
    }

    public void setLeftDoorRot(float rotation) {
        EXTERIORNBT.get(this).setLeftDoorRotation(rotation);
    }

    public void setRightDoorRot(float rotation) {
        EXTERIORNBT.get(this).setRightDoorRotation(rotation);
    }

    public float getLeftDoorRotation() {
        return EXTERIORNBT.get(this).getLeftDoorRotation();
    }

    public float getRightDoorRotation() {
        return EXTERIORNBT.get(this).getRightDoorRotation();
    }

    public void setMaterialState(MaterialStateEnum materialState) {
        EXTERIORNBT.get(this).setMaterialState(materialState);
    }

    public MaterialStateEnum getMaterialState() {
        return EXTERIORNBT.get(this).getCurrentMaterialState();
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);

        if(this.getAnimation() != null)
            this.getAnimation().setAlpha(nbt.getFloat("alpha"));

        if (this.tardis != null) {
            nbt.putUuid("tardis", this.tardis.getUuid());
        }
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);

        nbt.putFloat("alpha", this.getAlpha());

        if (nbt.contains("tardis")) {
            TardisManager.getInstance().link(nbt.getUuid("tardis"), this);
        }
    }

    public void onEntityCollision(Entity entity) {
        if (!(entity instanceof ServerPlayerEntity player))
            return;

        if (this.getTardis() != null && (this.getLeftDoorRotation() > 0 || this.getRightDoorRotation() > 0)) {
            if(!this.getTardis().getLockedTardis())
                TardisUtil.teleportInside(this.getTardis(), player);
        }
    }

    // flip you im readdng it (the client doesnt have the tardis and this fixes it)
    public void refindTardisClient() {
        if (this.tardis != null) // No issue
            return;
        if (!this.getWorld().isClient())
            return;

        ClientTardisManager manager = ClientTardisManager.getInstance();

        if (manager.getLookup().isEmpty()) {
            manager.ask(this.getPos());
            return;
        }

        for (Tardis tardis : manager.getLookup().values()) {
            if (!tardis.getTravel().getPosition().equals(this.pos)) continue;

            this.setTardis(tardis);
            return;
        }
        manager.ask(this.getPos());
    }
    // millionth jank sync method
    public void syncFromClientManager() {
        if (this.tardis == null)
            return;

        TardisTravel.State last = this.tardis.getTravel().getState();

        ClientTardisManager.getInstance().getTardis(this.tardis.getUuid(), (var) -> this.tardis = var);

        if (last != this.tardis.getTravel().getState()) {
            this.animation = null;
            this.animation = this.getExterior().createAnimation(this);
            AITMod.LOGGER.debug("Created new ANIMATION for " + this);
            this.animation.setupAnimation(this.getTardis().getTravel().getState());
            // this.getAnimation();
        }
    }
    // same here
    public void refindTardis() {
        System.out.println(this.tardis);
        if (this.tardis != null) // No issue
            return;
        if (this.getWorld().isClient())
            return;

        ServerTardisManager manager = ServerTardisManager.getInstance();

        for (Tardis tardis : manager.getLookup().values()) {
            if (!tardis.getTravel().getPosition().equals(this.pos)) continue;

            this.setTardis(tardis);
            return;
        }

        AITMod.LOGGER.warn("Deleting exterior block at " + this.pos + " due to lack of Tardis!");
        this.getWorld().removeBlock(this.pos, false);
    }

    @Override
    public Tardis getTardis() {
        if (this.tardis == null && this.getWorld() != null) {
            if (!this.getWorld().isClient())
                refindTardis();
            else
                refindTardisClient();
        }

        if (this.getWorld() != null && this.getWorld().isClient() && this.tardis != null)
            syncFromClientManager(); // i accidentally made it make a bajillion different instances when it syncs so this fixes that and i dont know how to fix the other thing so theo can do it

        return tardis;
    }

    @Override
    public void setTardis(Tardis tardis) {
        this.tardis = tardis;

        this.getAnimation().setupAnimation(this.tardis.getTravel().getState());
    }

    public static <T extends BlockEntity> void tick(World world, BlockPos pos, BlockState blockState, T exterior) {
        if (((ExteriorBlockEntity) exterior).animation != null)
            ((ExteriorBlockEntity) exterior).getAnimation().tick();
    }

    // theo please stop deleting my shit theres a reason its there rarely its not just schizophrenic code rambles that are useless
    public void verifyAnimation() {
        if (this.animation != null)
            return;
        if (this.getTardis() == null)
            return;

        this.animation = this.getExterior().createAnimation(this);
        AITMod.LOGGER.debug("Created new ANIMATION for " + this);
        this.animation.setupAnimation(this.getTardis().getTravel().getState());
    }

    public ExteriorAnimation getAnimation() {
        this.verifyAnimation();

        return this.animation;
    }

    public float getAlpha() {
        if (this.getAnimation() == null) {
            return 1f;
        }

        return this.getAnimation().getAlpha();
    }

    public void onBroken() {}
}