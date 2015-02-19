/*
 * Quartz
 * Copyright (c) 2015, Minecrell <https://github.com/Minecrell>
 *
 * Based on Sponge and SpongeAPI, licensed under the MIT License (MIT).
 * Copyright (c) SpongePowered.org <http://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package net.minecrell.quartz;

import com.google.common.base.Optional;
import org.apache.commons.lang3.NotImplementedException;
import org.spongepowered.api.GameDictionary;
import org.spongepowered.api.GameProfile;
import org.spongepowered.api.GameRegistry;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.meta.BannerPatternShape;
import org.spongepowered.api.block.meta.NotePitch;
import org.spongepowered.api.block.meta.SkullType;
import org.spongepowered.api.effect.particle.ParticleEffectBuilder;
import org.spongepowered.api.effect.particle.ParticleType;
import org.spongepowered.api.effect.sound.SoundType;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.hanging.art.Art;
import org.spongepowered.api.entity.living.animal.DyeColor;
import org.spongepowered.api.entity.living.animal.HorseColor;
import org.spongepowered.api.entity.living.animal.HorseStyle;
import org.spongepowered.api.entity.living.animal.HorseVariant;
import org.spongepowered.api.entity.living.animal.OcelotType;
import org.spongepowered.api.entity.living.animal.RabbitType;
import org.spongepowered.api.entity.living.monster.SkeletonType;
import org.spongepowered.api.entity.living.villager.Career;
import org.spongepowered.api.entity.living.villager.Profession;
import org.spongepowered.api.entity.player.gamemode.GameMode;
import org.spongepowered.api.item.Enchantment;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStackBuilder;
import org.spongepowered.api.item.merchant.TradeOfferBuilder;
import org.spongepowered.api.item.recipe.RecipeRegistry;
import org.spongepowered.api.potion.PotionEffectBuilder;
import org.spongepowered.api.potion.PotionEffectType;
import org.spongepowered.api.status.Favicon;
import org.spongepowered.api.util.rotation.Rotation;
import org.spongepowered.api.world.DimensionType;
import org.spongepowered.api.world.biome.BiomeType;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.UUID;

import javax.inject.Singleton;

@Singleton
public class QuartzGameRegistry implements GameRegistry {

    @Override
    public Optional<BlockType> getBlock(String id) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Collection<BlockType> getBlocks() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Optional<ItemType> getItem(String id) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Collection<ItemType> getItems() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Optional<BiomeType> getBiome(String id) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Collection<BiomeType> getBiomes() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public ItemStackBuilder getItemBuilder() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public TradeOfferBuilder getTradeOfferBuilder() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public PotionEffectBuilder getPotionEffectBuilder() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Optional<ParticleType> getParticleType(String name) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Collection<ParticleType> getParticleTypes() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public ParticleEffectBuilder getParticleEffectBuilder(ParticleType particle) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Optional<SoundType> getSound(String name) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Collection<SoundType> getSounds() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Optional<EntityType> getEntity(String id) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Collection<EntityType> getEntities() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Optional<Art> getArt(String id) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Collection<Art> getArts() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Optional<DyeColor> getDye(String id) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Collection<DyeColor> getDyes() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Optional<HorseColor> getHorseColor(String id) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Collection<HorseColor> getHorseColors() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Optional<HorseStyle> getHorseStyle(String id) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Collection<HorseStyle> getHorseStyles() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Optional<HorseVariant> getHorseVariant(String id) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Collection<HorseVariant> getHorseVariants() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Optional<OcelotType> getOcelotType(String id) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Collection<OcelotType> getOcelotTypes() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Optional<RabbitType> getRabbitType(String id) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Collection<RabbitType> getRabbitTypes() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Optional<SkeletonType> getSkeletonType(String id) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Collection<SkeletonType> getSkeletonTypes() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Optional<Career> getCareer(String id) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Collection<Career> getCareers() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Collection<Career> getCareers(Profession profession) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Optional<Profession> getProfession(String id) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Collection<Profession> getProfessions() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Collection<GameMode> getGameModes() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Collection<PotionEffectType> getPotionEffects() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Optional<Enchantment> getEnchantment(String id) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Collection<Enchantment> getEnchantments() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Collection<String> getDefaultGameRules() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Optional<DimensionType> getDimensionType(String name) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Collection<DimensionType> getDimensionTypes() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Optional<Rotation> getRotationFromDegree(int degrees) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Collection<Rotation> getRotations() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public GameProfile createGameProfile(UUID uuid, String name) {
        return (GameProfile) new com.mojang.authlib.GameProfile(uuid, name);
    }

    @Override
    public Favicon loadFavicon(String raw) throws IOException {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Favicon loadFavicon(File file) throws IOException {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Favicon loadFavicon(URL url) throws IOException {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Favicon loadFavicon(InputStream in) throws IOException {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Favicon loadFavicon(BufferedImage image) throws IOException {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Optional<NotePitch> getNotePitch(String name) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Collection<NotePitch> getNotePitches() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Optional<SkullType> getSkullType(String name) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Collection<SkullType> getSkullTypes() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Optional<BannerPatternShape> getBannerPatternShape(String name) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Optional<BannerPatternShape> getBannerPatternShapeById(String id) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Collection<BannerPatternShape> getBannerPatternShapes() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public GameDictionary getGameDictionary() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public RecipeRegistry getRecipeRegistry() {
        throw new NotImplementedException("TODO");
    }
}
