package mdteam.ait.tardis.wrapper.server;

import mdteam.ait.client.renderers.exteriors.ExteriorEnum;
import mdteam.ait.core.util.data.AbsoluteBlockPos;
import mdteam.ait.tardis.*;
import mdteam.ait.tardis.wrapper.server.manager.ServerTardisManager;

import java.util.UUID;
import java.util.function.Function;


public class ServerTardis implements ITardis {

    private final ServerTardisTravel travel;
    private final UUID uuid;
    private ServerTardisDesktop desktop;
    private final ServerTardisExterior exterior;
    private final ServerTardisDoor door;

    public ServerTardis(UUID uuid, AbsoluteBlockPos.Directed pos, TardisDesktopSchema schema, ExteriorEnum exteriorType) {
        this(uuid, tardis -> new ServerTardisTravel(tardis, pos), tardis -> new ServerTardisDesktop(tardis, schema),
                tardis -> new ServerTardisExterior(tardis, exteriorType), ServerTardisDoor::new);
    }

    protected ServerTardis(UUID uuid, Function<ITardis, ServerTardisTravel> travel, Function<ITardis, ServerTardisDesktop> desktop,
                           Function<ITardis, ServerTardisExterior> exterior, Function<ITardis, ServerTardisDoor> door) {
        this.uuid = uuid;
        this.travel = travel.apply(this);
        this.desktop = desktop.apply(this);
        this.exterior = exterior.apply(this);
        this.door = door.apply(this);

        this.init();
    }

    @Override
    public UUID getUuid() {
        return uuid;
    }

    @Override
    public TardisExterior getExterior() {
        return exterior;
    }

    @Override
    public TardisTravel getTravel() {
        return travel;
    }

    @Override
    public TardisDoor getDoor() {
        return door;
    }


    @Override
    public TardisDesktop getDesktop() {
        return desktop;
    }

    @Override
    public void setDesktop(TardisDesktop desktop) {
        this.desktop = (ServerTardisDesktop) desktop;
        this.sync();
    }

    public void sync() {
        ServerTardisManager.getInstance().sendToSubscribers(this);
    }
}