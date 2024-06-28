package loqor.ait.tardis.data;

import loqor.ait.AITMod;
import loqor.ait.api.tardis.TardisEvents;
import loqor.ait.core.advancement.TardisCriterions;
import loqor.ait.core.util.DeltaTimeManager;
import loqor.ait.registry.impl.DesktopRegistry;
import loqor.ait.tardis.Tardis;
import loqor.ait.tardis.TardisDesktopSchema;
import loqor.ait.tardis.base.TardisComponent;
import loqor.ait.tardis.base.TardisTickable;
import loqor.ait.tardis.data.properties.PropertiesHandler;
import loqor.ait.tardis.util.TardisUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public class InteriorChangingHandler extends TardisComponent implements TardisTickable {
    public static final String IS_REGENERATING = "is_regenerating";
	public static final String QUEUED_INTERIOR = "queued_interior";
	public static final Identifier CHANGE_DESKTOP = new Identifier(AITMod.MOD_ID, "change_desktop");
    private int ticks;

	public InteriorChangingHandler() {
		super(Id.INTERIOR);
	}

	static {
		TardisEvents.DEMAT.register((tardis -> {
			if (tardis.isGrowth() || tardis.<InteriorChangingHandler>handler(TardisComponent.Id.INTERIOR).isGenerating()
					|| tardis.travel2().handbrake() || PropertiesHandler.getBool(
					tardis.properties(), PropertiesHandler.IS_FALLING
			) || tardis.isRefueling())
				return true; // cancelled

			if (tardis.door().isOpen())
				return true;

			for (ServerPlayerEntity player : TardisUtil.getPlayersInInterior(tardis)) {
				TardisCriterions.TAKEOFF.trigger(player);
			}

			return false;
		}));
	}

	private void setGenerating(boolean var) {
		PropertiesHandler.set(this.tardis(), IS_REGENERATING, var);
	}

	public boolean isGenerating() {
		return PropertiesHandler.getBool(this.tardis().properties(), IS_REGENERATING);
	}

	private void setTicks(int var) {
		this.ticks = var;
	}

	public int getTicks() {
		return this.ticks;
	}

	private void setQueuedInterior(TardisDesktopSchema schema) {
		PropertiesHandler.set(this.tardis(), QUEUED_INTERIOR, schema.id());
	}

	public TardisDesktopSchema getQueuedInterior() {
		return DesktopRegistry.getInstance().get(PropertiesHandler.getIdentifier(this.tardis().properties(), QUEUED_INTERIOR));
	}

	public void queueInteriorChange(TardisDesktopSchema schema) {
		Tardis tardis = this.tardis();

		if (!this.canQueue())
			return;

		if (tardis.fuel().getCurrentFuel() < 5000) {
			for (PlayerEntity player : TardisUtil.getPlayersInsideInterior(tardis)) {
				player.sendMessage(Text.translatable("tardis.message.interiorchange.not_enough_fuel").formatted(Formatting.RED), true);
				return;
			}
		}

		AITMod.LOGGER.info("Queueing interior change for {} to {}", this.tardis, schema);

		setQueuedInterior(schema);
		setTicks(0);
		setGenerating(true);
		DeltaTimeManager.createDelay("interior_change-" + tardis.getUuid().toString(), 100L);
		tardis.alarm().enable();

		tardis.getDesktop().getConsolePos().clear();

		if (!tardis.hasGrowthDesktop())
			tardis.removeFuel(5000 * (tardis.tardisHammerAnnoyance + 1));
	}

	private void onCompletion() {
		Tardis tardis = this.tardis();

        this.setGenerating(false);
		clearedOldInterior = false;

		tardis.alarm().disable();

		boolean previouslyLocked = tardis.door().previouslyLocked();
		DoorData.lockTardis(previouslyLocked, tardis, null, false);

		tardis.engine().hasEngineCore().set(false);

		if (tardis.hasGrowthExterior()) {
			TravelHandlerV2 travel = tardis.travel2();

			travel.handbrake(false);
			travel.autopilot().set(true);

			// TODO(travel) replace with proper demat method
			travel.dematerialize();
		}
	}

	private void warnPlayers() {
		for (PlayerEntity player : TardisUtil.getPlayersInsideInterior(this.tardis())) {
			player.sendMessage(Text.translatable("tardis.message.interiorchange.warning").formatted(Formatting.RED), true);
		}
	}

	private boolean isInteriorEmpty() {
		return TardisUtil.getPlayersInsideInterior(this.tardis()).isEmpty();
	}

	private boolean clearedOldInterior = false;

	@Override
	public void tick(MinecraftServer server) {
		if (!isGenerating())
			return;

		if (DeltaTimeManager.isStillWaitingOnDelay("interior_change-" + this.tardis().getUuid().toString()))
			return;

		TravelHandlerV2 travel = this.tardis().travel2();

		// TODO(travel): move this to travelhandler
		//if (travel.getState() == TravelHandler.State.FLIGHT)
		//	travel.crash();

		if (this.isGenerating()) {
			if (!this.tardis().alarm().isEnabled())
				this.tardis().alarm().enable();
		}

		if (!this.canQueue()) {
			this.setGenerating(false);
			this.tardis().alarm().disable();
			return;
		}

		if (!isInteriorEmpty()) {
			warnPlayers();
			return;
		}

		if (isInteriorEmpty() && !this.tardis().door().locked()) {
			DoorData.lockTardis(true, this.tardis(), null, true);
		}
		if (isInteriorEmpty() && !clearedOldInterior) {
			this.tardis().getDesktop().clearOldInterior(getQueuedInterior());
			DeltaTimeManager.createDelay("interior_change-" + this.tardis().getUuid().toString(), 15000L);
			clearedOldInterior = true;
			return;
		}
		if (isInteriorEmpty() && clearedOldInterior) {
			this.tardis().getDesktop().changeInterior(getQueuedInterior());
			onCompletion();
		}
	}

	private boolean canQueue() {
		return tardis.isGrowth() || tardis.engine().hasPower() || tardis.crash().isToxic();
	}
}
