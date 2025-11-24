package com.duckblade.osrs.sailing.features.barracudatrials;

import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;
import net.runelite.api.gameval.ObjectID;

@RequiredArgsConstructor
enum TrialInteractable
{
	JJ_BOAT(ObjectID.SAILING_BT_JUBBLY_JIVE_TOAD_SUPPLIES_PARENT, j -> !j.hasSupplyBoatItem),
	JJ_GREEN(ObjectID.SAILING_BT_JUBBLY_JIVE_PILLAR_CLICKBOX_0_PARENT),
	JJ_YELLOW(ObjectID.SAILING_BT_JUBBLY_JIVE_PILLAR_CLICKBOX_1_PARENT),
	JJ_RED(ObjectID.SAILING_BT_JUBBLY_JIVE_PILLAR_CLICKBOX_2_PARENT),
	JJ_BLUE(ObjectID.SAILING_BT_JUBBLY_JIVE_PILLAR_CLICKBOX_3_PARENT),
	JJ_ORANGE(ObjectID.SAILING_BT_JUBBLY_JIVE_PILLAR_CLICKBOX_4_PARENT),
	JJ_LIGHT_BLUE(ObjectID.SAILING_BT_JUBBLY_JIVE_PILLAR_CLICKBOX_5_PARENT),
	JJ_PURPLE(ObjectID.SAILING_BT_JUBBLY_JIVE_PILLAR_CLICKBOX_6_PARENT),
	JJ_WHITE(ObjectID.SAILING_BT_JUBBLY_JIVE_PILLAR_CLICKBOX_7_PARENT),
	;

	final int object;
	final Predicate<BarracudaTrialHelper> predicate;

	TrialInteractable(int object)
	{
		this(object, j -> j.hasSupplyBoatItem && j.client.getObjectDefinition(object).getImpostor().getActions()[0] != null);
	}
}
