package loqor.ait.tardis.control.impl;

import loqor.ait.core.AITSounds;
import loqor.ait.core.blockentities.EngineCoreBlockEntity;
import loqor.ait.tardis.Tardis;
import loqor.ait.tardis.TardisTravel;
import loqor.ait.tardis.control.Control;
import loqor.ait.tardis.data.properties.PropertiesHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;

public class ThrottleControl extends Control {
	public ThrottleControl() {
		super("throttle");
	}

	@Override
	public boolean runServer(Tardis tardis, ServerPlayerEntity player, ServerWorld world, boolean leftClick) {

		if (tardis.getHandlers().getSequenceHandler().hasActiveSequence()) {
			if (tardis.getHandlers().getSequenceHandler().controlPartOfSequence(this)) {
				this.addToControlSequence(tardis, player);
			}
		}

		if (tardis.isInDanger())
			return false;

		TardisTravel travel = tardis.getTravel();

		if (!leftClick) {
			if (player.isSneaking()) {
				travel.setSpeed(travel.getMaxSpeed());
			} else {
				travel.increaseSpeed();
			}
		} else {
			if (player.isSneaking()) {
				travel.setSpeed(0);
			} else {
				travel.decreaseSpeed();
			}
		}

		if(travel.getState() == TardisTravel.State.DEMAT) {
			tardis.getHandlers().getSequenceHandler().setActivePlayer(player);
		}

		return true;
	}

	@Override
	public SoundEvent getSound() {
		return AITSounds.DEMAT_LEVER_PULL;
	}

	@Override
	public boolean shouldFailOnNoPower() {
		return false;
	}
}
