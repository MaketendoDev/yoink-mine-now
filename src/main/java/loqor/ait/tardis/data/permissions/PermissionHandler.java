package loqor.ait.tardis.data.permissions;

import loqor.ait.AITMod;
import loqor.ait.tardis.base.KeyedTardisComponent;
import loqor.ait.tardis.data.loyalty.Loyalty;
import loqor.ait.tardis.data.properties.v2.Property;
import loqor.ait.tardis.data.properties.v2.Value;
import loqor.ait.tardis.wrapper.client.ClientTardis;
import loqor.ait.tardis.wrapper.server.manager.ServerTardisManager;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PermissionHandler extends KeyedTardisComponent {

    private static final Identifier P19_LOYALTY_SYNC = new Identifier(AITMod.MOD_ID, "p19_loyalty");

    private static final Property<Loyalty.Type> P19_LOYALTY = Property.forEnum("p19_loyalty", Loyalty.Type.class, Loyalty.Type.COMPANION);

    private final Map<UUID, PermissionMap> data;
    private final Value<Loyalty.Type> p19Loyalty = P19_LOYALTY.create(this);

    public PermissionHandler(Map<UUID, PermissionMap> map) {
        super(Id.PERMISSIONS);
        this.data = map;
    }

    public PermissionHandler() {
        this(new HashMap<>());
    }

    static {
        // TODO: make properties have a built-in util to C->S
        ServerPlayNetworking.registerGlobalReceiver(P19_LOYALTY_SYNC, (server, player, handler, buf, responseSender) -> {
            ServerTardisManager.getInstance().getTardis(server, buf.readUuid(), tardis -> {
                if (tardis == null)
                    return;

                PermissionHandler permissions = tardis.handler(Id.PERMISSIONS);
                Loyalty.Type type = buf.readEnumConstant(Loyalty.Type.class);

                permissions.p19Loyalty.set(type);
            });
        });
    }

    @Override
    public void onLoaded() {
        p19Loyalty.of(this, P19_LOYALTY);
    }

    public static void p19Loyalty(ClientTardis tardis, Loyalty.Type type) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeUuid(tardis.getUuid());
        buf.writeEnumConstant(type);

        ClientPlayNetworking.send(P19_LOYALTY_SYNC, buf);
    }

    public Value<Loyalty.Type> p19Loyalty() {
        return p19Loyalty;
    }

    public boolean check(ServerPlayerEntity player, Permission permission) {
        return this.getPermissionMap(player).get(permission);
    }

    public boolean set(ServerPlayerEntity player, Permission permission, boolean value) {
        PermissionMap map = this.getPermissionMap(player);
        map.put(permission, value);

        this.sync();
        return value;
    }

    private PermissionMap getPermissionMap(ServerPlayerEntity player) {
        PermissionMap result = data.get(player.getUuid());

        if (result != null)
            return result;

        result = new PermissionMap();
        data.put(player.getUuid(), result);
        return result;
    }
}
