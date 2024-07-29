package loqor.ait.client.sounds;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;

// fixme long permission

public class PlayerFollowingLoopingSound extends LoopingSound {
	public PlayerFollowingLoopingSound(SoundEvent soundEvent, SoundCategory soundCategory, float volume, float pitch) {
		super(soundEvent, soundCategory);

		ClientPlayerEntity client = MinecraftClient.getInstance().player;
		this.setPosition(client.getBlockPos());
		this.setVolume(volume);
		this.setPitch(pitch);
		this.repeat = true;
	}

	public PlayerFollowingLoopingSound(SoundEvent soundEvent, SoundCategory soundCategory, float volume) {
		this(soundEvent, soundCategory, volume, 1);
	}

	public PlayerFollowingLoopingSound(SoundEvent soundEvent, SoundCategory soundCategory) {
		this(soundEvent, soundCategory, 1, 1);
	}

	@Override
	public void tick() {
		super.tick();
		this.setCoordsToPlayerCoords();
	}

	private void setCoordsToPlayerCoords() {
		ClientPlayerEntity player = MinecraftClient.getInstance().player;
		if (player == null) return;
		this.setPosition(player.getBlockPos());
	}
}