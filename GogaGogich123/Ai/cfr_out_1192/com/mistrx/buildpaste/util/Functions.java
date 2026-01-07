/*
 * Decompiled with CFR 0.152.
 */
package com.mistrx.buildpaste.util;

import com.google.gson.JsonObject;
import com.mistrx.buildpaste.BuildPasteMod;
import com.mistrx.buildpaste.firebase.Firebase;
import com.mistrx.buildpaste.util.Variables;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.math.Vector3d;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.ArrayUtils;

public class Functions {
    public static String blocksFile = "air\nstone\ngranite\npolished_granite\ndiorite\npolished_diorite\nandesite\npolished_andesite\ngrass_block\ndirt\ncoarse_dirt\npodzol\ncobblestone\noak_planks\nspruce_planks\nbirch_planks\njungle_planks\nacacia_planks\ndark_oak_planks\noak_sapling\nspruce_sapling\nbirch_sapling\njungle_sapling\nacacia_sapling\ndark_oak_sapling\nbedrock\nsand\nred_sand\ngravel\ngold_ore\niron_ore\ncoal_ore\noak_log\nspruce_log\nbirch_log\njungle_log\nacacia_log\ndark_oak_log\nstripped_oak_log\nstripped_spruce_log\nstripped_birch_log\nstripped_jungle_log\nstripped_acacia_log\nstripped_dark_oak_log\nstripped_oak_wood\nstripped_spruce_wood\nstripped_birch_wood\nstripped_jungle_wood\nstripped_acacia_wood\nstripped_dark_oak_wood\noak_wood\nspruce_wood\nbirch_wood\njungle_wood\nacacia_wood\ndark_oak_wood\noak_leaves\nspruce_leaves\nbirch_leaves\njungle_leaves\nacacia_leaves\ndark_oak_leaves\nsponge\nwet_sponge\nglass\nlapis_ore\nlapis_block\ndispenser\nsandstone\nchiseled_sandstone\ncut_sandstone\nnote_block\npowered_rail\ndetector_rail\nsticky_piston\ncobweb\ngrass\nfern\ndead_bush\nseagrass\nsea_pickle\npiston\nwhite_wool\norange_wool\nmagenta_wool\nlight_blue_wool\nyellow_wool\nlime_wool\npink_wool\ngray_wool\nlight_gray_wool\ncyan_wool\npurple_wool\nblue_wool\nbrown_wool\ngreen_wool\nred_wool\nblack_wool\ndandelion\npoppy\nblue_orchid\nallium\nazure_bluet\nred_tulip\norange_tulip\nwhite_tulip\npink_tulip\noxeye_daisy\nbrown_mushroom\nred_mushroom\ngold_block\niron_block\noak_slab\nspruce_slab\nbirch_slab\njungle_slab\nacacia_slab\ndark_oak_slab\nstone_slab\nsandstone_slab\npetrified_oak_slab\ncobblestone_slab\nbrick_slab\nstone_brick_slab\nnether_brick_slab\nquartz_slab\nred_sandstone_slab\npurpur_slab\nprismarine_slab\nprismarine_brick_slab\ndark_prismarine_slab\nsmooth_quartz\nsmooth_red_sandstone\nsmooth_sandstone\nsmooth_stone\nbricks\ntnt\nbookshelf\nmossy_cobblestone\nobsidian\ntorch\nend_rod\nchorus_plant\nchorus_flower\npurpur_block\npurpur_pillar\npurpur_stairs\nspawner\noak_stairs\nchest\ndiamond_ore\ndiamond_block\ncrafting_table\nfarmland\nfurnace\nladder\nrail\ncobblestone_stairs\nlever\nstone_pressure_plate\noak_pressure_plate\nspruce_pressure_plate\nbirch_pressure_plate\njungle_pressure_plate\nacacia_pressure_plate\ndark_oak_pressure_plate\nredstone_ore\nredstone_torch\nstone_button\nsnow\nice\nsnow_block\ncactus\nclay\njukebox\noak_fence\nspruce_fence\nbirch_fence\njungle_fence\nacacia_fence\ndark_oak_fence\npumpkin\ncarved_pumpkin\nnetherrack\nsoul_sand\nglowstone\njack_o_lantern\noak_trapdoor\nspruce_trapdoor\nbirch_trapdoor\njungle_trapdoor\nacacia_trapdoor\ndark_oak_trapdoor\ninfested_stone\ninfested_cobblestone\ninfested_stone_bricks\ninfested_mossy_stone_bricks\ninfested_cracked_stone_bricks\ninfested_chiseled_stone_bricks\nstone_bricks\nmossy_stone_bricks\ncracked_stone_bricks\nchiseled_stone_bricks\nbrown_mushroom_block\nred_mushroom_block\nmushroom_stem\niron_bars\nglass_pane\nmelon\nvine\noak_fence_gate\nspruce_fence_gate\nbirch_fence_gate\njungle_fence_gate\nacacia_fence_gate\ndark_oak_fence_gate\nbrick_stairs\nstone_brick_stairs\nmycelium\nlily_pad\nnether_bricks\nnether_brick_fence\nnether_brick_stairs\nenchanting_table\nend_portal_frame\nend_stone\nend_stone_bricks\ndragon_egg\nredstone_lamp\nsandstone_stairs\nemerald_ore\nender_chest\ntripwire_hook\nemerald_block\nspruce_stairs\nbirch_stairs\njungle_stairs\ncommand_block\nbeacon\ncobblestone_wall\nmossy_cobblestone_wall\noak_button\nspruce_button\nbirch_button\njungle_button\nacacia_button\ndark_oak_button\nanvil\nchipped_anvil\ndamaged_anvil\ntrapped_chest\nlight_weighted_pressure_plate\nheavy_weighted_pressure_plate\ndaylight_detector\nredstone_block\nnether_quartz_ore\nhopper\nchiseled_quartz_block\nquartz_block\nquartz_pillar\nquartz_stairs\nactivator_rail\ndropper\nwhite_terracotta\norange_terracotta\nmagenta_terracotta\nlight_blue_terracotta\nyellow_terracotta\nlime_terracotta\npink_terracotta\ngray_terracotta\nlight_gray_terracotta\ncyan_terracotta\npurple_terracotta\nblue_terracotta\nbrown_terracotta\ngreen_terracotta\nred_terracotta\nblack_terracotta\nbarrier\niron_trapdoor\nhay_block\nwhite_carpet\norange_carpet\nmagenta_carpet\nlight_blue_carpet\nyellow_carpet\nlime_carpet\npink_carpet\ngray_carpet\nlight_gray_carpet\ncyan_carpet\npurple_carpet\nblue_carpet\nbrown_carpet\ngreen_carpet\nred_carpet\nblack_carpet\nterracotta\ncoal_block\npacked_ice\nacacia_stairs\ndark_oak_stairs\nslime_block\ngrass_path\nsunflower\nlilac\nrose_bush\npeony\ntall_grass\nlarge_fern\nwhite_stained_glass\norange_stained_glass\nmagenta_stained_glass\nlight_blue_stained_glass\nyellow_stained_glass\nlime_stained_glass\npink_stained_glass\ngray_stained_glass\nlight_gray_stained_glass\ncyan_stained_glass\npurple_stained_glass\nblue_stained_glass\nbrown_stained_glass\ngreen_stained_glass\nred_stained_glass\nblack_stained_glass\nwhite_stained_glass_pane\norange_stained_glass_pane\nmagenta_stained_glass_pane\nlight_blue_stained_glass_pane\nyellow_stained_glass_pane\nlime_stained_glass_pane\npink_stained_glass_pane\ngray_stained_glass_pane\nlight_gray_stained_glass_pane\ncyan_stained_glass_pane\npurple_stained_glass_pane\nblue_stained_glass_pane\nbrown_stained_glass_pane\ngreen_stained_glass_pane\nred_stained_glass_pane\nblack_stained_glass_pane\nprismarine\nprismarine_bricks\ndark_prismarine\nprismarine_stairs\nprismarine_brick_stairs\ndark_prismarine_stairs\nsea_lantern\nred_sandstone\nchiseled_red_sandstone\ncut_red_sandstone\nred_sandstone_stairs\nrepeating_command_block\nchain_command_block\nmagma_block\nnether_wart_block\nred_nether_bricks\nbone_block\nstructure_void\nobserver\nshulker_box\nwhite_shulker_box\norange_shulker_box\nmagenta_shulker_box\nlight_blue_shulker_box\nyellow_shulker_box\nlime_shulker_box\npink_shulker_box\ngray_shulker_box\nlight_gray_shulker_box\ncyan_shulker_box\npurple_shulker_box\nblue_shulker_box\nbrown_shulker_box\ngreen_shulker_box\nred_shulker_box\nblack_shulker_box\nwhite_glazed_terracotta\norange_glazed_terracotta\nmagenta_glazed_terracotta\nlight_blue_glazed_terracotta\nyellow_glazed_terracotta\nlime_glazed_terracotta\npink_glazed_terracotta\ngray_glazed_terracotta\nlight_gray_glazed_terracotta\ncyan_glazed_terracotta\npurple_glazed_terracotta\nblue_glazed_terracotta\nbrown_glazed_terracotta\ngreen_glazed_terracotta\nred_glazed_terracotta\nblack_glazed_terracotta\nwhite_concrete\norange_concrete\nmagenta_concrete\nlight_blue_concrete\nyellow_concrete\nlime_concrete\npink_concrete\ngray_concrete\nlight_gray_concrete\ncyan_concrete\npurple_concrete\nblue_concrete\nbrown_concrete\ngreen_concrete\nred_concrete\nblack_concrete\nwhite_concrete_powder\norange_concrete_powder\nmagenta_concrete_powder\nlight_blue_concrete_powder\nyellow_concrete_powder\nlime_concrete_powder\npink_concrete_powder\ngray_concrete_powder\nlight_gray_concrete_powder\ncyan_concrete_powder\npurple_concrete_powder\nblue_concrete_powder\nbrown_concrete_powder\ngreen_concrete_powder\nred_concrete_powder\nblack_concrete_powder\nturtle_egg\ndead_tube_coral_block\ndead_brain_coral_block\ndead_bubble_coral_block\ndead_fire_coral_block\ndead_horn_coral_block\ntube_coral_block\nbrain_coral_block\nbubble_coral_block\nfire_coral_block\nhorn_coral_block\ntube_coral\nbrain_coral\nbubble_coral\nfire_coral\nhorn_coral\ndead_brain_coral\ndead_bubble_coral\ndead_fire_coral\ndead_horn_coral\ndead_tube_coral\ntube_coral_fan\nbrain_coral_fan\nbubble_coral_fan\nfire_coral_fan\nhorn_coral_fan\ndead_tube_coral_fan\ndead_brain_coral_fan\ndead_bubble_coral_fan\ndead_fire_coral_fan\ndead_horn_coral_fan\nblue_ice\nconduit\niron_door\noak_door\nspruce_door\nbirch_door\njungle_door\nacacia_door\ndark_oak_door\nrepeater\ncomparator\nstructure_block\nsign\nsugar_cane\nkelp\ndried_kelp_block\nlapis_lazuli\ncake\nwhite_bed\norange_bed\nmagenta_bed\nlight_blue_bed\nyellow_bed\nlime_bed\npink_bed\ngray_bed\nlight_gray_bed\ncyan_bed\npurple_bed\nblue_bed\nbrown_bed\ngreen_bed\nred_bed\nblack_bed\nmelon_stem\npumpkin_stem\nbrewing_stand\ncauldron\nflower_pot\nskeleton_skull\nwither_skeleton_skull\nplayer_head\nzombie_head\ncreeper_head\ndragon_head\nwhite_banner\norange_banner\nmagenta_banner\nlight_blue_banner\nyellow_banner\nlime_banner\npink_banner\ngray_banner\nlight_gray_banner\ncyan_banner\npurple_banner\nblue_banner\nbrown_banner\ngreen_banner\nred_banner\nblack_banner\nwater\nlava\ntall_seagrass\npiston_head\nmoving_piston\nwall_torch\nfire\nredstone_wire\nwall_sign\nredstone_wall_torch\nnether_portal\nattached_pumpkin_stem\nattached_melon_stem\npumpkin_stem\nmelon_stem\nend_portal\ncocoa\ntripwire\npotted_oak_sapling\npotted_spruce_sapling\npotted_birch_sapling\npotted_jungle_sapling\npotted_acacia_sapling\npotted_dark_oak_sapling\npotted_fern\npotted_dandelion\npotted_poppy\npotted_blue_orchid\npotted_allium\npotted_azure_bluet\npotted_red_tulip\npotted_orange_tulip\npotted_white_tulip\npotted_pink_tulip\npotted_oxeye_daisy\npotted_red_mushroom\npotted_brown_mushroom\npotted_dead_bush\npotted_cactus\ncarrots\npotatoes\nskeleton_wall_skull\nwither_skeleton_wall_skull\nzombie_wall_head\nplayer_wall_head\ncreeper_wall_head\ndragon_wall_head\nwhite_wall_banner\norange_wall_banner\nmagenta_wall_banner\nlight_blue_wall_banner\nyellow_wall_banner\nlime_wall_banner\npink_wall_banner\ngray_wall_banner\nlight_gray_wall_banner\ncyan_wall_banner\npurple_wall_banner\nblue_wall_banner\nbrown_wall_banner\ngreen_wall_banner\nred_wall_banner\nblack_wall_banner\nbeetroots\nend_gateway\nfrosted_ice\nkelp_plant\ndead_tube_coral_wall_fan\ndead_brain_coral_wall_fan\ndead_bubble_coral_wall_fan\ndead_fire_coral_wall_fan\ndead_horn_coral_wall_fan\ntube_coral_wall_fan\nbrain_coral_wall_fan\nbubble_coral_wall_fan\nfire_coral_wall_fan\nhorn_coral_wall_fan\nvoid_air\ncave_air\nbubble_column\ncornflower\nlily_of_the_valley\nwither_rose\nsmooth_stone_slab\ncut_sandstone_slab\ncut_red_sandstone_slab\nbrick_wall\nprismarine_wall\nred_sandstone_wall\nmossy_stone_brick_wall\ngranite_wall\nstone_brick_wall\nnether_brick_wall\nandesite_wall\nred_nether_brick_wall\nsandstone_wall\nend_stone_brick_wall\ndiorite_wall\npolished_granite_stairs\nsmooth_red_sandstone_stairs\nmossy_stone_brick_stairs\npolished_diorite_stairs\nmossy_cobblestone_stairs\nend_stone_brick_stairs\nstone_stairs\nsmooth_sandstone_stairs\nsmooth_quartz_stairs\ngranite_stairs\nandesite_stairs\nred_nether_brick_stairs\npolished_andesite_stairs\ndiorite_stairs\npolished_granite_slab\nsmooth_red_sandstone_slab\nmossy_stone_brick_slab\npolished_diorite_slab\nmossy_cobblestone_slab\nend_stone_brick_slab\nsmooth_sandstone_slab\nsmooth_quartz_slab\ngranite_slab\nandesite_slab\nred_nether_brick_slab\npolished_andesite_slab\ndiorite_slab\nscaffolding\njigsaw\ncomposter\noak_sign\nspruce_sign\nbirch_sign\njungle_sign\nacacia_sign\ndark_oak_sign\nbamboo\nloom\nbarrel\nsmoker\nblast_furnace\ncartography_table\nfletching_table\ngrindstone\nlectern\nsmithing_table\nstonecutter\nbell\nlantern\ncampfire\nbee_nest\nbeehive\nhoney_block\nhoneycomb_block\noak_wall_sign\nspruce_wall_sign\nbirch_wall_sign\nacacia_wall_sign\njungle_wall_sign\ndark_oak_wall_sign\npotted_cornflower\npotted_lily_of_the_valley\npotted_wither_rose\nbamboo_sapling\npotted_bamboo\nsweet_berry_bush\ncrimson_nylium\nwarped_nylium\ncrimson_planks\nwarped_planks\nnether_gold_ore\ncrimson_stem\nwarped_stem\nstripped_crimson_stem\nstripped_warped_stem\nstripped_crimson_hyphae\nstripped_warped_hyphae\ncrimson_hyphae\nwarped_hyphae\ncrimson_fungus\nwarped_fungus\ncrimson_roots\nwarped_roots\nnether_sprouts\nweeping_vines\ntwisting_vines\ncrimson_slab\nwarped_slab\ncrimson_pressure_plate\nwarped_pressure_plate\npolished_blackstone_pressure_plate\ncrimson_fence\nwarped_fence\nsoul_soil\nbasalt\npolished_basalt\nsoul_torch\ncrimson_trapdoor\nwarped_trapdoor\nchain\ncrimson_fence_gate\nwarped_fence_gate\ncracked_nether_bricks\nchiseled_nether_bricks\ncrimson_stairs\nwarped_stairs\nblackstone_wall\npolished_blackstone_wall\npolished_blackstone_brick_wall\ncrimson_button\nwarped_button\npolished_blackstone_button\nquartz_bricks\nwarped_wart_block\ncrimson_door\nwarped_door\ncrimson_sign\nwarped_sign\nsoul_lantern\nsoul_campfire\nshroomlight\nlodestone\nnetherite_block\nancient_debris\ntarget\ncrying_obsidian\nblackstone\nblackstone_slab\nblackstone_stairs\ngilded_blackstone\npolished_blackstone\npolished_blackstone_slab\npolished_blackstone_stairs\nchiseled_polished_blackstone\npolished_blackstone_bricks\npolished_blackstone_brick_slab\npolished_blackstone_brick_stairs\ncracked_polished_blackstone_bricks\nrespawn_anchor\nsoul_fire\nsoul_wall_torch\nweeping_vines_plant\ntwisting_vines_plant\ncrimson_wall_sign\nwarped_wall_sign\npotted_crimson_fungus\npotted_warped_fungus\npotted_crimson_roots\npotted_warped_roots\nwheat\namethyst_block\namethyst_bud\namethyst_cluster\nazalea\nazalea_leaves\nbig_dripleaf\nblack_candle\nblue_candle\nbrown_candle\nbudding_amethyst\ncalcite\ncandle\nchiseled_deepslate\ncobbled_deepslate\ncobbled_deepslate_slab\ncobbled_deepslate_stairs\ncobbled_deepslate_wall\ncopper_block\ncopper_ore\ncracked_deepslate_bricks\ncracked_deepslate_tiles\ncut_copper\ncut_copper_slab\ncut_copper_stairs\ncyan_candle\ndeepslate\ndeepslate_bricks\ndeepslate_brick_slab\ndeepslate_brick_stairs\ndeepslate_brick_wall\ndeepslate_coal_ore\ndeepslate_copper_ore\ndeepslate_diamond_ore\ndeepslate_emerald_ore\ndeepslate_gold_ore\ndeepslate_iron_ore\ndeepslate_lapis_ore\ndeepslate_redstone_ore\ndeepslate_tiles\ndeepslate_tile_slab\ndeepslate_tile_stairs\ndeepslate_tile_wall\ndirt_path\ndripstone_block\nexposed_copper\nexposed_cut_copper\nexposed_cut_copper_slab\nexposed_cut_copper_stairs\nflowering_azalea\nflowering_azalea_leaves\nglow_lichen\ngray_candle\ngreen_candle\nhanging_roots\ninfested_deepslate\nlarge_amethyst_bud\nlightning_rod\nlight_blue_candle\nlight_gray_candle\nlime_candle\nmagenta_candle\nmedium_amethyst_bud\nmoss_block\nmoss_carpet\norange_candle\noxidized_copper\noxidized_cut_copper\noxidized_cut_copper_slab\noxidized_cut_copper_stairs\npink_candle\npointed_dripstone\npolished_deepslate\npolished_deepslate_slab\npolished_deepslate_stairs\npolished_deepslate_wall\npurple_candle\nraw_copper_block\nraw_gold_block\nred_candle\nrooted_dirt\nsculk_sensor\nsmall_amethyst_bud\nsmall_dripleaf\nsmooth_basalt\nspore_blossom\ntinted_glass\ntuff\nwaxed_copper_block\nwaxed_cut_copper\nwaxed_cut_copper_slab\nwaxed_cut_copper_stairs\nwaxed_exposed_copper\nwaxed_exposed_cut_copper\nwaxed_exposed_cut_copper_slab\nwaxed_exposed_cut_copper_stairs\nwaxed_oxidized_copper\nwaxed_oxidized_cut_copper\nwaxed_oxidized_cut_copper_slab\nwaxed_oxidized_cut_copper_stairs\nwaxed_weathered_copper\nwaxed_weathered_cut_copper\nwaxed_weathered_cut_copper_slab\nwaxed_weathered_cut_copper_stairs\nweathered_copper\nweathered_cut_copper\nweathered_cut_copper_slab\nweathered_cut_copper_stairs\nwhite_candle\nyellow_candle\nwater_cauldron\nlava_cauldron\nochre_froglight\npearlescent_froglight\nverdant_froglight\nfrogspawn\nmangrove_button\nmangrove_door\nmangrove_fence\nmangrove_fence_gate\nmangrove_leaves\nmangrove_log\nmangrove_planks\nmangrove_pressure_plate\nmangrove_propagule\nmangrove_roots\nmangrove_sign\nmangrove_slab\nmangrove_stairs\nmangrove_trapdoor\nmangrove_wall_sign\nmangrove_wood\nmuddy_mangrove_roots\npotted_mangrove_propagule\nstripped_mangrove_log\nstripped_mangrove_wood\nmud\nmud_brick_slab\nmud_brick_stairs\nmud_brick_wall\nmud_bricks\nreinforced_deepslate\nsculk\nsculk_catalyst\nsculk_shrieker\nsculk_vein\n";
    public static Integer[] pasteBlockBlackListArray = new Integer[0];
    public static ArrayList<Integer> pasteBlockBlackList = new ArrayList<Integer>(Arrays.asList(pasteBlockBlackListArray));
    public static String[] blockDataBlacklist = new String[]{"level=0", "snowy=false", "waterlogged=true"};
    public static String[] blocksArray = blocksFile.split("\n", -1);
    public static Integer i = 0;
    public static int newerBlocksAmountFound = 0;
    public static ArrayList<BlockPos> observerBlockPos = new ArrayList();
    public static ArrayList<BlockState> observerBlockStates = new ArrayList();
    public static Boolean uuidMessageSent = false;
    public static HashMap<String, String> blockEqualsItemHashmap = new HashMap();
    static Vector3d lastBlockPlacePos;
    static Vector3d firstBlockPlacePos;

    public static void setPlayerVariables(Player player) {
        Variables.player = player;
        Variables.uuid = player.m_20149_();
    }

    public static Boolean sendInvalidUUIDMessage(Player player) {
        if (player.m_20149_().equals("00000000-0000-0000-0000-000000000000") && !uuidMessageSent.booleanValue()) {
            uuidMessageSent = true;
            return true;
        }
        return false;
    }

    public static void setItemEqualsBlockHashmap() {
        Object toAdd = "";
        toAdd = (String)toAdd + "attached_melon_stem=melon_seeds\n";
        toAdd = (String)toAdd + "attached_pumpkin_stem=pumpkin_seeds\n";
        toAdd = (String)toAdd + "melon_stem=melon_seeds\n";
        toAdd = (String)toAdd + "pumpkin_stem=pumpkin_seeds\n";
        toAdd = (String)toAdd + "bamboo_sapling=bamboo\n";
        toAdd = (String)toAdd + "chorus_plant=chorus_fruit\n";
        toAdd = (String)toAdd + "cocoa=cocoa_beans\n";
        toAdd = (String)toAdd + "farmland=dirt\n";
        toAdd = (String)toAdd + "frosted_ice=ice\n";
        toAdd = (String)toAdd + "grass_path=grass\n";
        toAdd = (String)toAdd + "kelp_plant=kelp\n";
        toAdd = (String)toAdd + "moving_piston=piston\n";
        toAdd = (String)toAdd + "redstone_wire=redstone\n";
        toAdd = (String)toAdd + "snow=snowball\n";
        toAdd = (String)toAdd + "sweet_berry_bush=sweet_berries\n";
        toAdd = (String)toAdd + "beetroots=beetroot\n";
        toAdd = (String)toAdd + "potatoes=potato\n";
        toAdd = (String)toAdd + "carrots=carrot\n";
        toAdd = (String)toAdd + "wheat=wheat_seeds\n";
        String[] toAddArray = ((String)toAdd).split("\n");
        for (int i = 0; i < toAddArray.length; ++i) {
            String[] keyAndValue = toAddArray[i].split("=");
            blockEqualsItemHashmap.put("minecraft:" + keyAndValue[0], "minecraft:" + keyAndValue[1]);
        }
    }

    public static String getItemFromBlock(String block) {
        if (blockEqualsItemHashmap.containsKey(block)) {
            return blockEqualsItemHashmap.get(block);
        }
        if (block.contains("wall_")) {
            return block.replace("wall_", "");
        }
        if (block.contains("infested_")) {
            return block.replace("infested_", "");
        }
        if (block.contains("potted_")) {
            return "flower_pot";
        }
        return block;
    }

    public static void addBlockToBlocksInBuild(String block) {
        if (!Variables.blocksInBuild.contains(block = block.replace("minecraft:", "")) && !block.equals("air")) {
            Variables.blocksInBuild.add(block);
        }
    }

    public static String getData(Level world, Vector3d pos) {
        BlockState state = world.m_8055_(new BlockPos(pos.f_86214_, pos.f_86215_, pos.f_86216_));
        int length = state.m_61148_().keySet().toArray().length;
        Object data = "";
        for (int i = 0; i < length; ++i) {
            String thisKey = state.m_61148_().keySet().toArray()[i].toString();
            thisKey = thisKey.substring(thisKey.indexOf("=") + 1, thisKey.indexOf(",")).toLowerCase();
            String thisValue = state.m_61148_().values().toArray()[i].toString().toLowerCase();
            String thisData = thisKey + "=" + thisValue;
            boolean isBlacklisted = false;
            for (int j = 0; j < blockDataBlacklist.length; ++j) {
                if (!thisData.contains(blockDataBlacklist[j])) continue;
                isBlacklisted = true;
            }
            if (i == 0) {
                data = (String)data + "[";
            }
            if (i == length - 1) {
                if (isBlacklisted) {
                    if (((String)data).endsWith(",")) {
                        data = ((String)data).substring(0, ((String)data).length() - 1);
                    }
                } else {
                    data = (String)data + thisData;
                }
                data = (String)data + "]";
                continue;
            }
            data = (String)data + thisData + ",";
        }
        if (data == "[]" || data == "" || ((String)data).length() == 2) {
            return null;
        }
        return data;
    }

    public static String getSetblockCommand(Vector3d pos, String blockname, String data, String nbt) {
        int x = Math.toIntExact(Math.round(pos.f_86214_));
        int y = Math.toIntExact(Math.round(pos.f_86215_));
        int z = Math.toIntExact(Math.round(pos.f_86216_));
        if (data == null || data.equals("null") || !data.startsWith("[")) {
            data = "";
        }
        Object command = "";
        command = "_setblocknoresponse " + x + " " + y + " " + z + " " + blockname + data + nbt.replaceAll("'", "\"");
        return command;
    }

    public static String setBuilding(Vector3d pos, String dir, String[] args, Boolean placeBlocksOnFinish) {
        String buildIDinArgs = null;
        String buildArgs = "nopastemodifier";
        for (int i = 0; i < args.length; ++i) {
            if (args[i] == null) continue;
            if (!args[i].equals(Variables.suggestion)) {
                buildIDinArgs = args[i];
                continue;
            }
            buildArgs = args[i];
        }
        String getBuildResponse = null;
        if (buildIDinArgs == null) {
            getBuildResponse = Firebase.getSelectedBuildingID();
        } else {
            getBuildResponse = "success";
            Variables.buildID = buildIDinArgs;
        }
        BuildPasteMod.LOGGER.info(getBuildResponse + " sent");
        if (getBuildResponse == "success") {
            BuildPasteMod.LOGGER.info("success sent, Firebase.getBuilding executed");
            String getBuildByURLResponse = Firebase.getBuilding(Variables.buildID, placeBlocksOnFinish, pos, dir, buildArgs);
            BuildPasteMod.LOGGER.info("After Firebase.getBuilding");
            if (getBuildResponse == "success" && getBuildByURLResponse == "success") {
                return "success";
            }
            if (getBuildByURLResponse == "error-404") {
                return "error-404";
            }
            return "random-error";
        }
        return getBuildResponse;
    }

    public static void addToLastBlocks(Vector3d pos) {
        String blockName = Functions.getBlocknameFromVector(pos);
        if (Functions.getBlockIdByName(blockName) == null) {
            Variables.lastBlockIDs.add(blockName);
        } else {
            Variables.lastBlockIDs.add(Functions.getBlockIdByName(blockName));
        }
        Variables.lastBlockData.add(Functions.getData(Variables.player.m_20193_(), pos));
    }

    public static void setBlock(Vector3d currentPos, String id, String data, String nbt, String direction, String pasteModifier) {
        String placeBlockName = id;
        String blockName = Functions.getBlocknameFromVector(currentPos);
        Functions.addToLastBlocks(currentPos);
        if (pasteModifier.equals(Variables.suggestion)) {
            if (!blockName.equals(placeBlockName) && !placeBlockName.equals("minecraft:air")) {
                if (placeBlockName.equals("minecraft:observer")) {
                    BlockPos blockPos = new BlockPos(currentPos.f_86214_, currentPos.f_86215_, currentPos.f_86216_);
                    String rotation = Functions.rotateBlock(data, direction, true);
                    Direction dir = Direction.SOUTH;
                    if (rotation.equals("south")) {
                        dir = Direction.SOUTH;
                    } else if (rotation.equals("east")) {
                        dir = Direction.EAST;
                    } else if (rotation.equals("west")) {
                        dir = Direction.WEST;
                    } else if (rotation.equals("north")) {
                        dir = Direction.NORTH;
                    } else if (rotation.equals("up")) {
                        dir = Direction.UP;
                    } else if (rotation.equals("down")) {
                        dir = Direction.DOWN;
                    }
                    Level world = Variables.player.m_20193_();
                    BlockState observerState = (BlockState)Blocks.f_50455_.m_49966_().m_61124_((Property)BlockStateProperties.f_61372_, (Comparable)dir);
                    observerBlockPos.add(blockPos);
                    observerBlockStates.add(observerState);
                    world.m_46597_(blockPos, Blocks.f_50069_.m_49966_());
                } else {
                    String thisData = Functions.rotateBlock(data, direction);
                    String command = Functions.getSetblockCommand(currentPos, placeBlockName, thisData, nbt);
                    try {
                        Variables.player.m_20193_().m_7654_().m_129892_().m_82094_().execute(command, (Object)Variables.player.m_20203_());
                    }
                    catch (CommandSyntaxException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else if (pasteModifier.equals("nopastemodifier") && !blockName.equals(placeBlockName)) {
            if (placeBlockName.equals("minecraft:observer")) {
                BlockPos blockPos = new BlockPos(currentPos.f_86214_, currentPos.f_86215_, currentPos.f_86216_);
                String rotation = Functions.rotateBlock(data, direction, true);
                Direction dir = Direction.SOUTH;
                if (rotation.equals("south")) {
                    dir = Direction.SOUTH;
                } else if (rotation.equals("east")) {
                    dir = Direction.EAST;
                } else if (rotation.equals("west")) {
                    dir = Direction.WEST;
                } else if (rotation.equals("north")) {
                    dir = Direction.NORTH;
                } else if (rotation.equals("up")) {
                    dir = Direction.UP;
                } else if (rotation.equals("down")) {
                    dir = Direction.DOWN;
                }
                Level world = Variables.player.m_20193_();
                BlockState observerState = (BlockState)Blocks.f_50455_.m_49966_().m_61124_((Property)BlockStateProperties.f_61372_, (Comparable)dir);
                observerBlockPos.add(blockPos);
                observerBlockStates.add(observerState);
                world.m_46597_(blockPos, Blocks.f_50069_.m_49966_());
            } else {
                String thisData = Functions.rotateBlock(data, direction);
                String command = Functions.getSetblockCommand(currentPos, placeBlockName, thisData, nbt);
                try {
                    Variables.player.m_20193_().m_7654_().m_129892_().m_82094_().execute(command, (Object)Variables.player.m_20203_());
                }
                catch (CommandSyntaxException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static String pasteCurrentBuilding(Vector3d pos, String direction, String pasteModifier, Boolean undoPaste, Boolean isConstructing) {
        Vector3d size = null;
        ArrayList<Object> ids = null;
        ArrayList<String> data = null;
        JsonObject nbt = null;
        newerBlocksAmountFound = 0;
        if (!undoPaste.booleanValue()) {
            size = Firebase.size;
            ids = new ArrayList<Object>(Firebase.blockIDs);
            data = new ArrayList<String>(Firebase.blockData);
            nbt = Firebase.blockNBT;
        } else {
            size = Firebase.size;
            ids = new ArrayList<Object>(Variables.lastBlockIDs);
            data = new ArrayList<String>(Variables.lastBlockData);
            nbt = Variables.lastNBT;
        }
        Variables.lastPos = pos;
        Variables.lastPasteDirection = direction;
        Variables.lastBlockSize = Firebase.size;
        Variables.lastBlockIDs = new ArrayList<Object>();
        Variables.lastBlockData = new ArrayList<String>();
        Variables.lastNBT = nbt;
        HashMap<Object, Integer> usedMaterials = new HashMap<Object, Integer>();
        Variables.blocksInBuild = new ArrayList();
        i = 0;
        if (Firebase.uploadDirection.equals("north") || Firebase.uploadDirection.equals("south")) {
            if (Objects.equals(direction, "north")) {
                for (x = size.f_86214_; x > 0.0; x -= 1.0) {
                    for (y = 0.0; y < size.f_86215_; y += 1.0) {
                        for (z = size.f_86216_; z > 0.0; z -= 1.0) {
                            currentPos = new Vector3d(pos.f_86214_ + x, pos.f_86215_ + y, pos.f_86216_ + z - size.f_86216_);
                            block = Functions.getBlockByIdOrName(ids.get(i).toString());
                            nbtData = "";
                            if (nbt.has(i.toString())) {
                                nbtData = nbt.get(i.toString()).getAsString();
                            }
                            Functions.addBlockToBlocksInBuild(block);
                            if (isConstructing.booleanValue()) {
                                itemFromBlock = Functions.getItemFromBlock(block);
                                if (Variables.playerItems.containsKey(itemFromBlock)) {
                                    if (Variables.playerItems.get(itemFromBlock) > 0) {
                                        Functions.setBlock(currentPos, block, (String)data.get(i), nbtData, direction, pasteModifier);
                                        Variables.playerItems.merge((String)itemFromBlock, -1, Integer::sum);
                                        usedMaterials.merge(itemFromBlock, 1, Integer::sum);
                                    } else {
                                        Functions.addToLastBlocks(currentPos);
                                    }
                                } else {
                                    Functions.addToLastBlocks(currentPos);
                                }
                            } else {
                                Functions.setBlock(currentPos, block, (String)data.get(i), nbtData, direction, pasteModifier);
                            }
                            itemFromBlock = i;
                            i = i + 1;
                        }
                    }
                }
            } else if (Objects.equals(direction, "south")) {
                for (x = 0.0; x < size.f_86214_; x += 1.0) {
                    for (y = 0.0; y < size.f_86215_; y += 1.0) {
                        for (z = 0.0; z < size.f_86216_; z += 1.0) {
                            currentPos = new Vector3d(pos.f_86214_ + x - size.f_86214_, pos.f_86215_ + y, pos.f_86216_ + z);
                            block = Functions.getBlockByIdOrName(ids.get(i).toString());
                            nbtData = "";
                            if (nbt.has(i.toString())) {
                                nbtData = nbt.get(i.toString()).getAsString();
                            }
                            Functions.addBlockToBlocksInBuild(block);
                            if (isConstructing.booleanValue()) {
                                itemFromBlock = Functions.getItemFromBlock(block);
                                if (Variables.playerItems.containsKey(itemFromBlock)) {
                                    if (Variables.playerItems.get(itemFromBlock) > 0) {
                                        Functions.setBlock(currentPos, block, (String)data.get(i), nbtData, direction, pasteModifier);
                                        Variables.playerItems.merge((String)itemFromBlock, -1, Integer::sum);
                                        usedMaterials.merge(itemFromBlock, 1, Integer::sum);
                                    } else {
                                        Functions.addToLastBlocks(currentPos);
                                    }
                                } else {
                                    Functions.addToLastBlocks(currentPos);
                                }
                            } else {
                                Functions.setBlock(currentPos, block, (String)data.get(i), nbtData, direction, pasteModifier);
                            }
                            itemFromBlock = i;
                            i = i + 1;
                        }
                    }
                }
            } else if (Objects.equals(direction, "east")) {
                for (x = size.f_86214_; x > 0.0; x -= 1.0) {
                    for (y = 0.0; y < size.f_86215_; y += 1.0) {
                        for (z = 0.0; z < size.f_86216_; z += 1.0) {
                            currentPos = new Vector3d(pos.f_86214_ + z, pos.f_86215_ + y, pos.f_86216_ + x);
                            block = Functions.getBlockByIdOrName(ids.get(i).toString());
                            nbtData = "";
                            if (nbt.has(i.toString())) {
                                nbtData = nbt.get(i.toString()).getAsString();
                            }
                            Functions.addBlockToBlocksInBuild(block);
                            if (isConstructing.booleanValue()) {
                                itemFromBlock = Functions.getItemFromBlock(block);
                                if (Variables.playerItems.containsKey(itemFromBlock)) {
                                    if (Variables.playerItems.get(itemFromBlock) > 0) {
                                        Functions.setBlock(currentPos, block, (String)data.get(i), nbtData, direction, pasteModifier);
                                        Variables.playerItems.merge((String)itemFromBlock, -1, Integer::sum);
                                        usedMaterials.merge(itemFromBlock, 1, Integer::sum);
                                    } else {
                                        Functions.addToLastBlocks(currentPos);
                                    }
                                } else {
                                    Functions.addToLastBlocks(currentPos);
                                }
                            } else {
                                Functions.setBlock(currentPos, block, (String)data.get(i), nbtData, direction, pasteModifier);
                            }
                            itemFromBlock = i;
                            i = i + 1;
                        }
                    }
                }
            } else if (Objects.equals(direction, "west")) {
                for (x = 0.0; x < size.f_86214_; x += 1.0) {
                    for (y = 0.0; y < size.f_86215_; y += 1.0) {
                        for (z = size.f_86216_; z > 0.0; z -= 1.0) {
                            currentPos = new Vector3d(pos.f_86214_ + z - size.f_86216_, pos.f_86215_ + y, pos.f_86216_ + x - size.f_86214_);
                            block = Functions.getBlockByIdOrName(ids.get(i).toString());
                            nbtData = "";
                            if (nbt.has(i.toString())) {
                                nbtData = nbt.get(i.toString()).getAsString();
                            }
                            Functions.addBlockToBlocksInBuild(block);
                            if (isConstructing.booleanValue()) {
                                itemFromBlock = Functions.getItemFromBlock(block);
                                if (Variables.playerItems.containsKey(itemFromBlock)) {
                                    if (Variables.playerItems.get(itemFromBlock) > 0) {
                                        Functions.setBlock(currentPos, block, (String)data.get(i), nbtData, direction, pasteModifier);
                                        Variables.playerItems.merge((String)itemFromBlock, -1, Integer::sum);
                                        usedMaterials.merge(itemFromBlock, 1, Integer::sum);
                                    } else {
                                        Functions.addToLastBlocks(currentPos);
                                    }
                                } else {
                                    Functions.addToLastBlocks(currentPos);
                                }
                            } else {
                                Functions.setBlock(currentPos, block, (String)data.get(i), nbtData, direction, pasteModifier);
                            }
                            itemFromBlock = i;
                            i = i + 1;
                        }
                    }
                }
            }
        } else if (direction.equals("north")) {
            for (x = size.f_86214_; x > 0.0; x -= 1.0) {
                for (y = 0.0; y < size.f_86215_; y += 1.0) {
                    for (z = size.f_86216_; z > 0.0; z -= 1.0) {
                        currentPos = new Vector3d(pos.f_86214_ + z, pos.f_86215_ + y, pos.f_86216_ + x - size.f_86214_);
                        block = Functions.getBlockByIdOrName(ids.get(i).toString());
                        nbtData = "";
                        if (nbt.has(i.toString())) {
                            nbtData = nbt.get(i.toString()).getAsString();
                        }
                        Functions.addBlockToBlocksInBuild(block);
                        if (isConstructing.booleanValue()) {
                            itemFromBlock = Functions.getItemFromBlock(block);
                            if (Variables.playerItems.containsKey(itemFromBlock)) {
                                if (Variables.playerItems.get(itemFromBlock) > 0) {
                                    Functions.setBlock(currentPos, block, (String)data.get(i), nbtData, direction, pasteModifier);
                                    Variables.playerItems.merge((String)itemFromBlock, -1, Integer::sum);
                                    usedMaterials.merge(itemFromBlock, 1, Integer::sum);
                                } else {
                                    Functions.addToLastBlocks(currentPos);
                                }
                            } else {
                                Functions.addToLastBlocks(currentPos);
                            }
                        } else {
                            Functions.setBlock(currentPos, block, (String)data.get(i), nbtData, direction, pasteModifier);
                        }
                        itemFromBlock = i;
                        i = i + 1;
                    }
                }
            }
        } else if (direction.equals("south")) {
            for (x = 0.0; x < size.f_86214_; x += 1.0) {
                for (y = 0.0; y < size.f_86215_; y += 1.0) {
                    for (z = 0.0; z < size.f_86216_; z += 1.0) {
                        currentPos = new Vector3d(pos.f_86214_ + z - size.f_86216_, pos.f_86215_ + y, pos.f_86216_ + x);
                        block = Functions.getBlockByIdOrName(ids.get(i).toString());
                        nbtData = "";
                        if (nbt.has(i.toString())) {
                            nbtData = nbt.get(i.toString()).getAsString();
                        }
                        Functions.addBlockToBlocksInBuild(block);
                        if (isConstructing.booleanValue()) {
                            itemFromBlock = Functions.getItemFromBlock(block);
                            if (Variables.playerItems.containsKey(itemFromBlock)) {
                                if (Variables.playerItems.get(itemFromBlock) > 0) {
                                    Functions.setBlock(currentPos, block, (String)data.get(i), nbtData, direction, pasteModifier);
                                    Variables.playerItems.merge((String)itemFromBlock, -1, Integer::sum);
                                    usedMaterials.merge(itemFromBlock, 1, Integer::sum);
                                } else {
                                    Functions.addToLastBlocks(currentPos);
                                }
                            } else {
                                Functions.addToLastBlocks(currentPos);
                            }
                        } else {
                            Functions.setBlock(currentPos, block, (String)data.get(i), nbtData, direction, pasteModifier);
                        }
                        itemFromBlock = i;
                        i = i + 1;
                    }
                }
            }
        } else if (direction.equals("east")) {
            for (x = 0.0; x < size.f_86214_; x += 1.0) {
                for (y = 0.0; y < size.f_86215_; y += 1.0) {
                    for (z = size.f_86216_; z > 0.0; z -= 1.0) {
                        currentPos = new Vector3d(pos.f_86214_ + x, pos.f_86215_ + y, pos.f_86216_ + z);
                        block = Functions.getBlockByIdOrName(ids.get(i).toString());
                        nbtData = "";
                        if (nbt.has(i.toString())) {
                            nbtData = nbt.get(i.toString()).getAsString();
                        }
                        Functions.addBlockToBlocksInBuild(block);
                        if (isConstructing.booleanValue()) {
                            itemFromBlock = Functions.getItemFromBlock(block);
                            if (Variables.playerItems.containsKey(itemFromBlock)) {
                                if (Variables.playerItems.get(itemFromBlock) > 0) {
                                    Functions.setBlock(currentPos, block, (String)data.get(i), nbtData, direction, pasteModifier);
                                    Variables.playerItems.merge((String)itemFromBlock, -1, Integer::sum);
                                    usedMaterials.merge(itemFromBlock, 1, Integer::sum);
                                } else {
                                    Functions.addToLastBlocks(currentPos);
                                }
                            } else {
                                Functions.addToLastBlocks(currentPos);
                            }
                        } else {
                            Functions.setBlock(currentPos, block, (String)data.get(i), nbtData, direction, pasteModifier);
                        }
                        itemFromBlock = i;
                        i = i + 1;
                    }
                }
            }
        } else if (direction.equals("west")) {
            for (x = size.f_86214_; x > 0.0; x -= 1.0) {
                for (y = 0.0; y < size.f_86215_; y += 1.0) {
                    for (z = 0.0; z < size.f_86216_; z += 1.0) {
                        currentPos = new Vector3d(pos.f_86214_ + x - size.f_86214_, pos.f_86215_ + y, pos.f_86216_ + z - size.f_86216_);
                        block = Functions.getBlockByIdOrName(ids.get(i).toString());
                        nbtData = "";
                        if (nbt.has(i.toString())) {
                            nbtData = nbt.get(i.toString()).getAsString();
                        }
                        Functions.addBlockToBlocksInBuild(block);
                        if (isConstructing.booleanValue()) {
                            itemFromBlock = Functions.getItemFromBlock(block);
                            if (Variables.playerItems.containsKey(itemFromBlock)) {
                                if (Variables.playerItems.get(itemFromBlock) > 0) {
                                    Functions.setBlock(currentPos, block, (String)data.get(i), nbtData, direction, pasteModifier);
                                    Variables.playerItems.merge((String)itemFromBlock, -1, Integer::sum);
                                    usedMaterials.merge(itemFromBlock, 1, Integer::sum);
                                } else {
                                    Functions.addToLastBlocks(currentPos);
                                }
                            } else {
                                Functions.addToLastBlocks(currentPos);
                            }
                        } else {
                            Functions.setBlock(currentPos, block, (String)data.get(i), nbtData, direction, pasteModifier);
                        }
                        Integer n = i;
                        i = i + 1;
                    }
                }
            }
        }
        final Level world = Variables.player.m_20193_();
        if (observerBlockPos.size() != 0) {
            new Timer().schedule(new TimerTask(){

                @Override
                public void run() {
                    for (int j = 0; j < observerBlockPos.size(); ++j) {
                        world.m_46597_(observerBlockPos.get(j), (BlockState)observerBlockStates.get(j).m_61124_((Property)BlockStateProperties.f_61448_, (Comparable)Boolean.valueOf(false)));
                    }
                    observerBlockPos = new ArrayList();
                    observerBlockStates = new ArrayList();
                }
            }, 1L);
        }
        if (isConstructing.booleanValue()) {
            for (String item : usedMaterials.keySet()) {
                Integer amount = (Integer)usedMaterials.get(item);
                String command = "/_clearnoresponse " + Variables.player.m_7755_().getString() + " " + item + " " + amount.toString();
                try {
                    Variables.player.m_20193_().m_7654_().m_129892_().m_82094_().execute(command, (Object)Variables.player.m_20203_());
                }
                catch (CommandSyntaxException e) {
                    e.printStackTrace();
                }
            }
        }
        Vector3d adjustedSize = size;
        if (Firebase.uploadDirection.equals("east") || Firebase.uploadDirection.equals("west")) {
            adjustedSize = new Vector3d(size.f_86216_, size.f_86215_, size.f_86214_);
        }
        if (direction.equals("north")) {
            firstBlockPlacePos = new Vector3d(Variables.lastPos.f_86214_ + 1.0, Variables.lastPos.f_86215_, Variables.lastPos.f_86216_);
            lastBlockPlacePos = Functions.getLastSecondPos(firstBlockPlacePos, adjustedSize, new Vector3d(1.0, 1.0, -1.0));
        } else if (direction.equals("east")) {
            Vector3d modifiedSize = new Vector3d(adjustedSize.f_86216_, adjustedSize.f_86215_, adjustedSize.f_86214_);
            firstBlockPlacePos = new Vector3d(Variables.lastPos.f_86214_, Variables.lastPos.f_86215_, Variables.lastPos.f_86216_ + 1.0);
            lastBlockPlacePos = Functions.getLastSecondPos(firstBlockPlacePos, modifiedSize, new Vector3d(1.0, 1.0, 1.0));
        } else if (direction.equals("south")) {
            firstBlockPlacePos = new Vector3d(Variables.lastPos.f_86214_ - 1.0, Variables.lastPos.f_86215_, Variables.lastPos.f_86216_);
            lastBlockPlacePos = Functions.getLastSecondPos(firstBlockPlacePos, adjustedSize, new Vector3d(-1.0, 1.0, 1.0));
        } else if (direction.equals("west")) {
            Vector3d modifiedSize = new Vector3d(adjustedSize.f_86216_, adjustedSize.f_86215_, adjustedSize.f_86214_);
            firstBlockPlacePos = new Vector3d(Variables.lastPos.f_86214_, Variables.lastPos.f_86215_, Variables.lastPos.f_86216_ - 1.0);
            lastBlockPlacePos = Functions.getLastSecondPos(firstBlockPlacePos, modifiedSize, new Vector3d(-1.0, 1.0, -1.0));
        }
        Variables.lastFirstPos = firstBlockPlacePos;
        Variables.lastSecondPos = lastBlockPlacePos;
        BuildPasteMod.LOGGER.info(String.valueOf(Functions.lastBlockPlacePos.f_86214_), (Object)Functions.lastBlockPlacePos.f_86215_, (Object)Functions.lastBlockPlacePos.f_86216_);
        return null;
    }

    private static Vector3d getLastSecondPos(Vector3d firstPos, Vector3d size, Vector3d positiveOrNegative) {
        return new Vector3d(firstPos.f_86214_ + positiveOrNegative.f_86214_ * (size.f_86214_ - 1.0), firstPos.f_86215_ + positiveOrNegative.f_86215_ * (size.f_86215_ - 1.0), firstPos.f_86216_ + positiveOrNegative.f_86216_ * (size.f_86216_ - 1.0));
    }

    public static String rotateBlock(String thisData, String pasteDirection, Boolean returnDirection) {
        if (thisData != null) {
            if (thisData.contains("facing=") && thisData.length() >= 11) {
                String blockDirection = thisData.substring(thisData.indexOf("facing=") + 7);
                blockDirection = blockDirection.contains(",") ? blockDirection.substring(0, blockDirection.indexOf(",")) : blockDirection.substring(0, blockDirection.indexOf("]"));
                return Functions.calculateBlockRotation(blockDirection, pasteDirection, Firebase.uploadDirection);
            }
        } else {
            return "";
        }
        return thisData;
    }

    public static String rotateBlock(String thisData, String pasteDirection) {
        if (thisData != null) {
            if (thisData.contains("facing=") && thisData.length() >= 11) {
                String blockDirection = thisData.substring(thisData.indexOf("facing=") + 7);
                blockDirection = blockDirection.contains(",") ? blockDirection.substring(0, blockDirection.indexOf(",")) : blockDirection.substring(0, blockDirection.indexOf("]"));
                String finalBlockDirection = Functions.calculateBlockRotation(blockDirection, pasteDirection, Firebase.uploadDirection);
                thisData = thisData.replace("facing=" + blockDirection, "facing=" + finalBlockDirection);
                return thisData;
            }
        } else {
            return "";
        }
        return thisData;
    }

    public static String calculateBlockRotation(String blockDir, String pasteDir, String uploadDir) {
        String[] possibleBlockDirectionsArray = new String[]{"north", "south", "east", "west"};
        List<String> possibleBlockDirections = Arrays.asList(possibleBlockDirectionsArray);
        if (possibleBlockDirections.contains(blockDir)) {
            Integer uploadDirectionInDegrees = Functions.facingToDegrees(uploadDir);
            Integer pasteDirInt = Functions.facingToDegrees(pasteDir);
            Integer differenceUploadPasteDirection = pasteDirInt - uploadDirectionInDegrees;
            Integer result = (360 + (Functions.facingToDegrees(blockDir) + differenceUploadPasteDirection)) % 360;
            return Functions.degreesToFacing(result);
        }
        return blockDir;
    }

    public static Integer facingToDegrees(String facing) {
        if (facing.equals("north")) {
            return 0;
        }
        if (facing.equals("east")) {
            return 90;
        }
        if (facing.equals("south")) {
            return 180;
        }
        if (facing.equals("west")) {
            return 270;
        }
        BuildPasteMod.LOGGER.info("No rotation is passend, return 0");
        return 0;
    }

    public static String degreesToFacing(Integer degrees) {
        if (degrees == 0) {
            return "north";
        }
        if (degrees == 90) {
            return "east";
        }
        if (degrees == 180) {
            return "south";
        }
        if (degrees == 270) {
            return "west";
        }
        return "north";
    }

    public static String getLookDirection(float yaw) {
        if (yaw < 0.0f) {
            yaw += 360.0f;
        }
        if (yaw >= 315.0f || yaw < 45.0f) {
            return "south";
        }
        if (yaw < 135.0f) {
            return "west";
        }
        if (yaw < 225.0f) {
            return "north";
        }
        if (yaw < 315.0f) {
            return "east";
        }
        return "north";
    }

    public static String getBlockByID(Integer id) {
        return "minecraft:" + blocksArray[id];
    }

    public static Integer getBlockIdByName(String name) {
        if (Arrays.asList(blocksArray).contains(name)) {
            return ArrayUtils.indexOf((Object[])blocksArray, (Object)name.replace("minecraft:", ""));
        }
        return null;
    }

    public static String getBlockByIdOrName(Object nameOrId) {
        try {
            int index = Math.round(Float.parseFloat(nameOrId.toString()));
            if (index >= blocksArray.length || index < 0) {
                ++newerBlocksAmountFound;
                return "minecraft:air";
            }
            return "minecraft:" + blocksArray[index];
        }
        catch (NumberFormatException e) {
            return nameOrId.toString();
        }
        catch (Exception e) {
            e.printStackTrace();
            return "minecraft:air";
        }
    }

    public static String getBlocknameFromVector(Vector3d pos) {
        Block block = Variables.player.m_20193_().m_8055_(new BlockPos(pos.f_86214_, pos.f_86215_, pos.f_86216_)).m_60734_();
        return ForgeRegistries.BLOCKS.getKey((Object)block).toString();
    }

    public static String getBlocknameFromBlock(Block block) {
        return ForgeRegistries.BLOCKS.getKey((Object)block).toString();
    }

    public static String PosToString(Vector3d pos) {
        return Math.round(pos.f_86214_) + " " + Math.round(pos.f_86215_) + " " + Math.round(pos.f_86216_);
    }

    public static String PosToString(BlockPos pos) {
        return pos.m_123341_() + " " + pos.m_123342_() + " " + pos.m_123343_();
    }
}

