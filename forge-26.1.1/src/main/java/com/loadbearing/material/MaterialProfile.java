package com.loadbearing.material;

import com.loadbearing.Config;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record MaterialProfile(
        double weight,
        double compressiveStrength,
        int maxSpan,
        boolean tensionOnly,
        double soilBearing,
        boolean brittle) {
    public static final MaterialProfile DEFAULT = new MaterialProfile(1.0D, 20.0D, 2, false, 10.0D, false);

    public static final MaterialProfile VOID = new MaterialProfile(0.0D, 0.0D, 0, false, 0.0D, false);

    public static final MapCodec<MaterialProfile> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Codec.DOUBLE.optionalFieldOf("weight", 1.0D).forGetter(MaterialProfile::weight),
            Codec.DOUBLE.optionalFieldOf("compressive_strength", 20.0D).forGetter(MaterialProfile::compressiveStrength),
            Codec.INT.optionalFieldOf("max_span", 2).forGetter(MaterialProfile::maxSpan),
            Codec.BOOL.optionalFieldOf("tension_only", false).forGetter(MaterialProfile::tensionOnly),
            Codec.DOUBLE.optionalFieldOf("soil_bearing", 10.0D).forGetter(MaterialProfile::soilBearing),
            Codec.BOOL.optionalFieldOf("brittle", false).forGetter(MaterialProfile::brittle)
    ).apply(i, MaterialProfile::new));

    public static final Codec<MaterialProfile> CODEC = MAP_CODEC.codec();

    public static final int REINFORCEMENT_FACTOR = 2;

    public static MaterialProfile brittle(double weight, double strength, int maxSpan, double soilBearing) {
        return new MaterialProfile(weight, strength, maxSpan, false, soilBearing, true);
    }

    public static MaterialProfile cable(double weight, int maxSpan) {
        return new MaterialProfile(weight, 0.0D, maxSpan, true, 0.0D, false);
    }

    public double scaledWeight() {
        return this.weight * Config.WEIGHT_MULTIPLIER.get();
    }

    public double scaledStrength() {
        return this.compressiveStrength * Config.STRENGTH_MULTIPLIER.get();
    }

    public int scaledSpan() {
        return Math.max(0, (int) Math.round(this.maxSpan * Config.SPAN_MULTIPLIER.get()));
    }

    public double scaledSoilBearing() {
        return this.soilBearing * Config.STRENGTH_MULTIPLIER.get();
    }

    public MaterialProfile reinforced() {
        return new MaterialProfile(
                this.weight,
                this.compressiveStrength * REINFORCEMENT_FACTOR,
                this.maxSpan * REINFORCEMENT_FACTOR,
                this.tensionOnly,
                this.soilBearing * REINFORCEMENT_FACTOR,
                false);
    }

    public boolean carriesCompression() {
        return !this.tensionOnly && this.compressiveStrength > 0.0D;
    }

    public double safetyMargin(double load) {
        if (this.tensionOnly) {
            return 100.0D;
        }
        double strength = scaledStrength();
        if (strength <= 0.0D) {
            return load <= 0.0D ? 100.0D : 0.0D;
        }
        return (strength - load) / strength * 100.0D;
    }
}
