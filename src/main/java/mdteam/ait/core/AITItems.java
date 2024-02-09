package mdteam.ait.core;

import com.neptunedevelopmentteam.neptunelib.core.init_handlers.CustomName;
import com.neptunedevelopmentteam.neptunelib.core.init_handlers.NeptuneItemInit;
import com.neptunedevelopmentteam.neptunelib.core.itemsettings.NeptuneItemSettings;
import mdteam.ait.AITMod;
import mdteam.ait.core.item.*;
import net.minecraft.item.Item;
import net.minecraft.item.MusicDiscItem;
import net.minecraft.util.Rarity;

public class AITItems implements NeptuneItemInit {
    // TARDIS
    public static final Item TARDIS_ITEM = new TardisItemBuilder(new NeptuneItemSettings().group(() -> AITMod.AIT_ITEM_GROUP).fireproof().maxCount(1));
    public static final Item SIEGE_ITEM = new SiegeTardisItem(new NeptuneItemSettings().fireproof());
    // Functional Items
    public static final Item REMOTE_ITEM = new RemoteItem(new NeptuneItemSettings().group(() -> AITMod.AIT_ITEM_GROUP).maxCount(1));
    public static final Item ARTRON_COLLECTOR = new ArtronCollectorItem(new NeptuneItemSettings().group(() -> AITMod.AIT_ITEM_GROUP).maxCount(1));
    public static final Item RIFT_SCANNER = new RiftScannerItem(new NeptuneItemSettings().group(() -> AITMod.AIT_ITEM_GROUP).maxCount(1));
    public static final Item HAMMER = new HammerItem(new NeptuneItemSettings().group(() -> AITMod.AIT_ITEM_GROUP).maxCount(1).maxDamage(5000));
    // Keys/Key Templates
    public static final Item IRON_KEY = new KeyItem(new NeptuneItemSettings().group(() -> AITMod.AIT_ITEM_GROUP));
    public static final Item GOLD_KEY = new KeyItem(new NeptuneItemSettings().group(() -> AITMod.AIT_ITEM_GROUP), KeyItem.Protocols.SNAP);
    public static final Item NETHERITE_KEY = new KeyItem(new NeptuneItemSettings().group(() -> AITMod.AIT_ITEM_GROUP).fireproof(), KeyItem.Protocols.SNAP, KeyItem.Protocols.HAIL);
    public static final Item CLASSIC_KEY = new KeyItem(new NeptuneItemSettings().group(() -> AITMod.AIT_ITEM_GROUP), KeyItem.Protocols.SNAP, KeyItem.Protocols.HAIL);
    public static final Item GOLD_KEY_UPGRADE_SMITHING_TEMPLATE = new KeySmithingTemplateItem(new NeptuneItemSettings().group(() -> AITMod.AIT_ITEM_GROUP), "Gold Key", "Gold Nugget");
    public static final Item NETHERITE_KEY_UPGRADE_SMITHING_TEMPLATE = new KeySmithingTemplateItem(new NeptuneItemSettings().group(() -> AITMod.AIT_ITEM_GROUP), "Netherite Key", "Netherite Scrap");
    public static final Item CLASSIC_KEY_UPGRADE_SMITHING_TEMPLATE = new KeySmithingTemplateItem(new NeptuneItemSettings().group(() -> AITMod.AIT_ITEM_GROUP), "Classic Key", "Amethyst Shard");
    // Sonic Screwdrivers
    @CustomName("mechanical")
    public static final Item MECHANICAL_SONIC_SCREWDRIVER = new SonicItem(new NeptuneItemSettings().group(() -> AITMod.AIT_ITEM_GROUP));
    @CustomName("renaissance")
    public static final Item RENAISSANCE_SONIC_SCREWDRIVER = new SonicItem(new NeptuneItemSettings().group(() -> AITMod.AIT_ITEM_GROUP));
    @CustomName("coral")
    public static final Item CORAL_SONIC_SCREWDRIVER = new SonicItem(new NeptuneItemSettings().group(() -> AITMod.AIT_ITEM_GROUP));

    // Waypoint-related
    public static final Item WAYPOINT_CARTRIDGE = new WaypointItem(new NeptuneItemSettings().group(() -> AITMod.AIT_ITEM_GROUP).maxCount(1));
    public static final Item DRIFTING_MUSIC_DISC = new MusicDiscItem(1, AITSounds.DRIFTING_MUSIC, new NeptuneItemSettings().group(() -> AITMod.AIT_ITEM_GROUP).maxCount(1).rarity(Rarity.RARE), 169);
}
