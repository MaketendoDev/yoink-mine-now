package loqor.ait.tardis.console.variant.coral;

import loqor.ait.AITMod;
import loqor.ait.core.data.schema.console.ConsoleVariantSchema;
import loqor.ait.tardis.console.type.CoralType;
import loqor.ait.tardis.data.loyalty.Loyalty;
import net.minecraft.util.Identifier;

public class WhiteCoralVariant extends ConsoleVariantSchema {
	public static final Identifier REFERENCE = new Identifier(AITMod.MOD_ID, "console/coral_white");

	public WhiteCoralVariant() {
		super(CoralType.REFERENCE, REFERENCE, new Loyalty(Loyalty.Type.PILOT));
	}
}