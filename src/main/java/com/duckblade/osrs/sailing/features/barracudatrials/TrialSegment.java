package com.duckblade.osrs.sailing.features.barracudatrials;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import net.runelite.api.gameval.ObjectID;

@RequiredArgsConstructor
enum TrialSegment
{
	JJ3_LAP1_START(ImmutableSet.of(
		ObjectID.SAILING_BT_JUBBLY_JIVE_COLLECTABLE_21,
		ObjectID.SAILING_BT_JUBBLY_JIVE_COLLECTABLE_2,
		ObjectID.SAILING_BT_JUBBLY_JIVE_COLLECTABLE_3,
		ObjectID.SAILING_BT_JUBBLY_JIVE_COLLECTABLE_4,
		ObjectID.SAILING_BT_JUBBLY_JIVE_COLLECTABLE_5,
		ObjectID.SAILING_BT_JUBBLY_JIVE_COLLECTABLE_6,
		ObjectID.SAILING_BT_JUBBLY_JIVE_COLLECTABLE_26,
		ObjectID.SAILING_BT_JUBBLY_JIVE_COLLECTABLE_7,
		ObjectID.SAILING_BT_JUBBLY_JIVE_COLLECTABLE_27
	), ImmutableList.of(TrialInteractable.JJ_BOAT), new BoatPath.Builder()
		.pt(2437, 3018)
		.pt(2420, 3024)
		.pt(2407, 3024)
		.pt(2403, 3019)
		.pt(2400, 3012)
		.pt(2390, 3008)
		.pt(2374, 3008)
		.pt(2364, 2999)
		.pt(2359, 2986)
		.pt(2350, 2977)
		.pt(2339, 2973)
		.pt(2310, 2973)
		.pt(2303, 2976)
		.pt(2292, 2976)// todo: join these better
		.build()),
	JJ3_LAP1_SUPPLY(ImmutableSet.of(
		ObjectID.SAILING_BT_JUBBLY_JIVE_COLLECTABLE_8,
		ObjectID.SAILING_BT_JUBBLY_JIVE_COLLECTABLE_9,
		ObjectID.SAILING_BT_JUBBLY_JIVE_COLLECTABLE_28,
		ObjectID.SAILING_BT_JUBBLY_JIVE_COLLECTABLE_10,
		ObjectID.SAILING_BT_JUBBLY_JIVE_COLLECTABLE_29,// cehk
		ObjectID.SAILING_BT_JUBBLY_JIVE_COLLECTABLE_30,
		ObjectID.SAILING_BT_JUBBLY_JIVE_COLLECTABLE_31,
		ObjectID.SAILING_BT_JUBBLY_JIVE_COLLECTABLE_11,
		ObjectID.SAILING_BT_JUBBLY_JIVE_COLLECTABLE_12
	), ImmutableList.of(
		TrialInteractable.JJ_YELLOW,
		TrialInteractable.JJ_RED
	), new BoatPath.Builder()
		.pt(2292, 2976)
		.pt(2277, 2981)
		.pt(2269, 2989)
		.pt(2258, 2989)
		.pt(2241, 3006)
		.pt(2242, 3017)
		.pt(2249, 3024)
		.pt(2256, 3024)
		.pt(2280, 3000)
		.pt(2294, 3000)
		.pt(2299, 3005)
		.pt(2299, 3011)
		.pt(2303, 3018)
		.pt(2322, 3018)
		.pt(2330, 3015)
		.build()
	),
	JJ3_LAP1_CIRCLE(ImmutableSet.of(
		ObjectID.SAILING_BT_JUBBLY_JIVE_COLLECTABLE_25,
		ObjectID.SAILING_BT_JUBBLY_JIVE_COLLECTABLE_14,
		ObjectID.SAILING_BT_JUBBLY_JIVE_COLLECTABLE_15,
		ObjectID.SAILING_BT_JUBBLY_JIVE_COLLECTABLE_16,
		ObjectID.SAILING_BT_JUBBLY_JIVE_COLLECTABLE_17,
		ObjectID.SAILING_BT_JUBBLY_JIVE_COLLECTABLE_18,
		ObjectID.SAILING_BT_JUBBLY_JIVE_COLLECTABLE_19,
		ObjectID.SAILING_BT_JUBBLY_JIVE_COLLECTABLE_20
	), ImmutableList.of(
		TrialInteractable.JJ_ORANGE,
		TrialInteractable.JJ_LIGHT_BLUE,
		TrialInteractable.JJ_PURPLE,
		TrialInteractable.JJ_WHITE
	), new BoatPath.Builder()
		.pt(2330, 3015)
		.pt(2342, 3003)
		.pt(2344, 2998)
		.pt(2356, 2987)
		.pt(2358, 2981)
		.pt(2358, 2962)
		.pt(2362, 2953)
		.pt(2377, 2938)
		.pt(2385, 2940)
		.pt(2428, 2940)
		.pt(2437, 2950)
		.pt(2437, 2955)
		.pt(2434, 2963)
		.pt(2434, 2985)
		.pt(2437, 2989)
		.pt(2437, 2993)
		.pt(2434, 3001)
		.build())
	;

	final Set<Integer> crates;
	final List<TrialInteractable> interactables;
	final BoatPath path;
}
