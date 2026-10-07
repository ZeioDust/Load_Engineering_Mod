package com.loadbearing.material;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

public final class BuiltinProfiles {
    public static final MaterialProfile SOIL = new MaterialProfile(1.0D, 8.0D, 1, false, 6.0D, false);

    public static final MaterialProfile GRANULAR = new MaterialProfile(1.0D, 8.0D, 1, false, 3.0D, false);

    public static final MaterialProfile MUD = new MaterialProfile(1.1D, 6.0D, 1, false, 1.0D, false);

    public static final MaterialProfile TIMBER = new MaterialProfile(0.8D, 20.0D, 4, false, 12.0D, false);

    public static final MaterialProfile LOG = new MaterialProfile(1.0D, 28.0D, 5, false, 16.0D, false);

    public static final MaterialProfile MASONRY = new MaterialProfile(1.5D, 45.0D, 3, false, 28.0D, false);

    public static final MaterialProfile DENSE_STONE = new MaterialProfile(1.9D, 60.0D, 3, false, 40.0D, false);

    public static final MaterialProfile IMMOVABLE = new MaterialProfile(2.5D, 100000.0D, 4, false, 100000.0D, false);

    public static final MaterialProfile METAL = new MaterialProfile(3.0D, 120.0D, 7, false, 60.0D, false);

    public static final MaterialProfile GLASSY = new MaterialProfile(0.6D, 12.0D, 2, false, 8.0D, true);

    public static final MaterialProfile SOFT = new MaterialProfile(0.2D, 2.0D, 1, false, 1.0D, false);

    public static final MaterialProfile CERAMIC = new MaterialProfile(1.4D, 38.0D, 3, false, 24.0D, true);

    public static final MaterialProfile SCAFFOLD = new MaterialProfile(0.3D, 34.0D, 6, false, 8.0D, false);

    private BuiltinProfiles() {}

    public static void registerVanilla(BiSink sink) {
        for (Block b : List.of(Blocks.DIRT, Blocks.COARSE_DIRT, Blocks.ROOTED_DIRT, Blocks.GRASS_BLOCK,
                Blocks.PODZOL, Blocks.MYCELIUM, Blocks.DIRT_PATH, Blocks.FARMLAND, Blocks.SOUL_SOIL,
                Blocks.SOUL_SAND, Blocks.MOSS_BLOCK, Blocks.SNOW_BLOCK, Blocks.CLAY)) {
            sink.accept(b, SOIL);
        }
        for (Block b : List.of(Blocks.SAND, Blocks.RED_SAND, Blocks.GRAVEL, Blocks.SUSPICIOUS_SAND,
                Blocks.SUSPICIOUS_GRAVEL)) {
            sink.accept(b, GRANULAR);
        }
        sink.accept(Blocks.MUD, MUD);
        sink.accept(Blocks.MUDDY_MANGROVE_ROOTS, MUD);
        sink.accept(Blocks.PACKED_MUD, new MaterialProfile(1.2D, 22.0D, 2, false, 14.0D, false));

        for (Block b : List.of(Blocks.STONE, Blocks.COBBLESTONE, Blocks.MOSSY_COBBLESTONE, Blocks.GRANITE,
                Blocks.DIORITE, Blocks.ANDESITE, Blocks.POLISHED_GRANITE, Blocks.POLISHED_DIORITE,
                Blocks.POLISHED_ANDESITE, Blocks.SMOOTH_STONE, Blocks.STONE_BRICKS,
                Blocks.MOSSY_STONE_BRICKS, Blocks.CRACKED_STONE_BRICKS, Blocks.CHISELED_STONE_BRICKS,
                Blocks.BRICKS, Blocks.SANDSTONE, Blocks.CUT_SANDSTONE, Blocks.SMOOTH_SANDSTONE,
                Blocks.CHISELED_SANDSTONE, Blocks.RED_SANDSTONE, Blocks.CUT_RED_SANDSTONE,
                Blocks.SMOOTH_RED_SANDSTONE, Blocks.CHISELED_RED_SANDSTONE, Blocks.PRISMARINE,
                Blocks.PRISMARINE_BRICKS, Blocks.DARK_PRISMARINE, Blocks.QUARTZ_BLOCK,
                Blocks.SMOOTH_QUARTZ, Blocks.CHISELED_QUARTZ_BLOCK, Blocks.QUARTZ_BRICKS,
                Blocks.QUARTZ_PILLAR, Blocks.NETHER_BRICKS, Blocks.RED_NETHER_BRICKS,
                Blocks.CRACKED_NETHER_BRICKS, Blocks.CHISELED_NETHER_BRICKS, Blocks.NETHERRACK,
                Blocks.END_STONE, Blocks.END_STONE_BRICKS, Blocks.PURPUR_BLOCK, Blocks.PURPUR_PILLAR,
                Blocks.CALCITE, Blocks.TUFF, Blocks.POLISHED_TUFF, Blocks.TUFF_BRICKS,
                Blocks.CHISELED_TUFF, Blocks.CHISELED_TUFF_BRICKS, Blocks.DRIPSTONE_BLOCK,
                Blocks.STONE_SLAB, Blocks.STONE_BRICK_SLAB)) {
            sink.accept(b, MASONRY);
        }
        for (Block b : List.of(Blocks.DEEPSLATE, Blocks.COBBLED_DEEPSLATE, Blocks.POLISHED_DEEPSLATE,
                Blocks.DEEPSLATE_BRICKS, Blocks.DEEPSLATE_TILES, Blocks.CRACKED_DEEPSLATE_BRICKS,
                Blocks.CRACKED_DEEPSLATE_TILES, Blocks.CHISELED_DEEPSLATE, Blocks.BASALT,
                Blocks.POLISHED_BASALT, Blocks.SMOOTH_BASALT, Blocks.BLACKSTONE,
                Blocks.POLISHED_BLACKSTONE, Blocks.POLISHED_BLACKSTONE_BRICKS,
                Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS, Blocks.CHISELED_POLISHED_BLACKSTONE,
                Blocks.GILDED_BLACKSTONE)) {
            sink.accept(b, DENSE_STONE);
        }
        for (Block b : List.of(Blocks.BEDROCK, Blocks.OBSIDIAN, Blocks.CRYING_OBSIDIAN,
                Blocks.REINFORCED_DEEPSLATE, Blocks.ANCIENT_DEBRIS)) {
            sink.accept(b, IMMOVABLE);
        }

        for (Block b : Blocks.COPPER_BLOCK.asList()) {
            sink.accept(b, METAL);
        }
        for (Block b : Blocks.CUT_COPPER.asList()) {
            sink.accept(b, METAL);
        }
        for (Block b : List.of(Blocks.IRON_BLOCK, Blocks.GOLD_BLOCK,
                Blocks.RAW_IRON_BLOCK, Blocks.RAW_GOLD_BLOCK,
                Blocks.RAW_COPPER_BLOCK, Blocks.ANVIL, Blocks.CHIPPED_ANVIL, Blocks.DAMAGED_ANVIL)) {
            sink.accept(b, METAL);
        }
        sink.accept(Blocks.NETHERITE_BLOCK, new MaterialProfile(4.0D, 400.0D, 9, false, 200.0D, false));
        sink.accept(Blocks.IRON_BARS, new MaterialProfile(0.5D, 25.0D, 6, false, 12.0D, false));
        sink.accept(Blocks.IRON_CHAIN, MaterialProfile.cable(0.1D, 16));

        sink.accept(Blocks.BAMBOO_MOSAIC, TIMBER);
        sink.accept(Blocks.BAMBOO_BLOCK, LOG);
        sink.accept(Blocks.SCAFFOLDING, SCAFFOLD);

        for (Block b : List.of(Blocks.GLASS, Blocks.TINTED_GLASS, Blocks.GLASS_PANE, Blocks.ICE,
                Blocks.PACKED_ICE, Blocks.BLUE_ICE, Blocks.SEA_LANTERN, Blocks.GLOWSTONE)) {
            sink.accept(b, GLASSY);
        }

        for (Block b : List.of(Blocks.HAY_BLOCK, Blocks.SPONGE, Blocks.WET_SPONGE, Blocks.SLIME_BLOCK,
                Blocks.HONEY_BLOCK, Blocks.DRIED_KELP_BLOCK, Blocks.SNOW, Blocks.POWDER_SNOW)) {
            sink.accept(b, SOFT);
        }
    }

    @FunctionalInterface
    public interface BiSink extends java.util.function.BiConsumer<Block, MaterialProfile> {}

    private static final List<TagRule> TAG_RULES = List.of(
            new TagRule(BlockTags.PLANKS, TIMBER),
            new TagRule(BlockTags.WOODEN_SLABS, scale(TIMBER, 0.5D)),
            new TagRule(BlockTags.WOODEN_STAIRS, scale(TIMBER, 0.75D)),
            new TagRule(BlockTags.WOODEN_FENCES, scale(TIMBER, 0.4D)),
            new TagRule(BlockTags.WOODEN_DOORS, scale(TIMBER, 0.4D)),
            new TagRule(BlockTags.WOODEN_TRAPDOORS, scale(TIMBER, 0.4D)),
            new TagRule(BlockTags.LOGS, LOG),
            new TagRule(BlockTags.STONE_BRICKS, MASONRY),
            new TagRule(BlockTags.BASE_STONE_OVERWORLD, MASONRY),
            new TagRule(BlockTags.BASE_STONE_NETHER, MASONRY),
            new TagRule(BlockTags.TERRACOTTA, CERAMIC),
            new TagRule(BlockTags.CONCRETE_POWDERS, GRANULAR),
            new TagRule(BlockTags.SAND, GRANULAR),
            new TagRule(BlockTags.DIRT, SOIL),
            new TagRule(BlockTags.MUD, MUD),
            new TagRule(BlockTags.ICE, GLASSY),
            new TagRule(BlockTags.IMPERMEABLE, GLASSY),
            new TagRule(BlockTags.WOOL, SOFT),
            new TagRule(BlockTags.WOOL_CARPETS, SOFT),
            new TagRule(BlockTags.LEAVES, SOFT),
            new TagRule(BlockTags.FLOWERS, SOFT),
            new TagRule(BlockTags.CROPS, SOFT),
            new TagRule(BlockTags.SLABS, scale(MASONRY, 0.5D)),
            new TagRule(BlockTags.STAIRS, scale(MASONRY, 0.75D)),
            new TagRule(BlockTags.WALLS, scale(MASONRY, 0.6D))
    );

    private record TagRule(TagKey<Block> tag, MaterialProfile profile) {}

    public static MaterialProfile infer(BlockState state) {
        for (TagRule rule : TAG_RULES) {
            if (state.is(rule.tag())) {
                return rule.profile();
            }
        }

        SoundType sound = state.getSoundType();
        MapColor color = state.getMapColor(null, null);

        if (sound == SoundType.METAL || sound == SoundType.ANVIL || color == MapColor.METAL) {
            return METAL;
        }
        if (sound == SoundType.WOOD || sound == SoundType.LADDER || sound == SoundType.BAMBOO
                || color == MapColor.WOOD) {
            return TIMBER;
        }
        if (sound == SoundType.SCAFFOLDING) {
            return SCAFFOLD;
        }
        if (sound == SoundType.GLASS) {
            return GLASSY;
        }
        if (sound == SoundType.SAND || sound == SoundType.GRAVEL || sound == SoundType.SNOW
                || sound == SoundType.POWDER_SNOW) {
            return GRANULAR;
        }
        if (sound == SoundType.GRASS || sound == SoundType.WOOL || sound == SoundType.WET_GRASS
                || sound == SoundType.LILY_PAD || sound == SoundType.CROP
                || sound == SoundType.SWEET_BERRY_BUSH || sound == SoundType.SLIME_BLOCK
                || sound == SoundType.HONEY_BLOCK) {
            return SOFT;
        }
        if (color == MapColor.DEEPSLATE) {
            return DENSE_STONE;
        }
        if (sound == SoundType.STONE || color == MapColor.STONE || color == MapColor.QUARTZ
                || color == MapColor.CLAY || color == MapColor.NETHER) {
            return MASONRY;
        }
        if (color == MapColor.DIRT || color == MapColor.PODZOL || color == MapColor.GRASS) {
            return SOIL;
        }
        if (color == MapColor.SAND) {
            return GRANULAR;
        }
        return MaterialProfile.DEFAULT;
    }

    public static MaterialProfile scale(MaterialProfile base, double factor) {
        return new MaterialProfile(
                base.weight() * factor,
                base.compressiveStrength() * factor,
                Math.max(1, (int) Math.round(base.maxSpan() * factor)),
                base.tensionOnly(),
                base.soilBearing() * factor,
                base.brittle());
    }

    static Map<Block, MaterialProfile> buildVanillaMap() {
        Map<Block, MaterialProfile> map = new IdentityHashMap<>(256);
        Consumer<Void> unused = v -> {};
        registerVanilla(map::put);
        return map;
    }
}
