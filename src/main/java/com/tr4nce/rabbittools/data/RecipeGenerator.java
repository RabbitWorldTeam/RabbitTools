package com.tr4nce.rabbittools.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.SpecialRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SmithingTransformRecipeBuilder;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.Tags;
import com.tr4nce.rabbittools.RabbitTools;
import com.tr4nce.rabbittools.crafting.ModRecipes;
import com.tr4nce.rabbittools.crafting.RecipeWandUpgrade;
import com.tr4nce.rabbittools.items.ModItems;

import java.util.concurrent.CompletableFuture;

public class RecipeGenerator extends RecipeProvider {
    public RecipeGenerator(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(packOutput, lookupProvider);
    }

    @Override
    protected void buildRecipes(RecipeOutput output, HolderLookup.Provider provider) {
        wandRecipe(output, ModItems.WAND_WOOD.get(), Inp.fromItems("wood_planks", Items.CHERRY_PLANKS, Items.BIRCH_PLANKS));
        wandRecipe(output, ModItems.WAND_STONE.get(), Inp.fromTag(ItemTags.STONE_TOOL_MATERIALS));
        wandRecipe(output, ModItems.WAND_IRON.get(), Inp.fromItem(ModItems.IRON_WAND_HEAD.get()));
        wandRecipe(output, ModItems.WAND_GOLD.get(), Inp.fromItem(ModItems.GOLD_WAND_HEAD.get()));
        wandRecipe(output, ModItems.WAND_DIAMOND.get(), Inp.fromItem(ModItems.DIAMOND_WAND_HEAD.get()));
        wandRecipe(output, ModItems.WAND_NETHERITE.get(), Inp.fromItem(ModItems.NETHERITE_WAND_HEAD.get()));
        wandRecipe(output, ModItems.WAND_INFINITY.get(), Inp.fromItem(ModItems.INFINITY_WAND_HEAD.get()));

        coreRecipe(output, ModItems.CORE_ANGEL.get(), Inp.fromTag(Tags.Items.FEATHERS), Inp.fromTag(Tags.Items.INGOTS_GOLD));
        coreRecipe(output, ModItems.CORE_DESTRUCTION.get(), Inp.fromTag(Tags.Items.STORAGE_BLOCKS_DIAMOND), Inp.fromItem(Items.DIAMOND_PICKAXE));
        coreRecipe(output, ModItems.CORE_EXCHANGE.get(), Inp.fromItem(Items.NETHER_STAR), Inp.fromTag(Tags.Items.STORAGE_BLOCKS_NETHERITE));

        componentRecipes(output);
        wandHeadRecipes(output);
        voidSackRecipe(output);

        specialRecipe(output, ModRecipes.WAND_UPGRADE.get());
    }

    private void componentRecipes(RecipeOutput output) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.BUILDING_COMPONENT.get())
                .define('I', Items.IRON_INGOT)
                .define('C', Items.COPPER_INGOT)
                .define('R', Items.REDSTONE)
                .pattern("I I")
                .pattern("CRC")
                .pattern("I I")
                .unlockedBy("has_iron", has(Items.IRON_INGOT))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.PRECISION_BUILDING_COMPONENT.get())
                .define('D', Items.DIAMOND)
                .define('G', Items.GOLD_INGOT)
                .define('B', Items.BLAZE_ROD)
                .define('R', Items.REDSTONE)
                .pattern("DBD")
                .pattern("GRG")
                .pattern("DRD")
                .unlockedBy("has_diamond", has(Items.DIAMOND))
                .save(output);
    }
    private void wandHeadRecipes(RecipeOutput output) {
        basicWandHeadRecipe(output, ModItems.IRON_WAND_HEAD.get(),
                ModItems.BUILDING_COMPONENT.get(), Inp.fromTag(Tags.Items.INGOTS_IRON));
        basicWandHeadRecipe(output, ModItems.GOLD_WAND_HEAD.get(),
                ModItems.BUILDING_COMPONENT.get(), Inp.fromTag(Tags.Items.INGOTS_GOLD));
        basicWandHeadRecipe(output, ModItems.DIAMOND_WAND_HEAD.get(),
                ModItems.PRECISION_BUILDING_COMPONENT.get(), Inp.fromTag(Tags.Items.GEMS_DIAMOND));

        SmithingTransformRecipeBuilder.smithing(
                        Ingredient.of(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE),
                        Ingredient.of(ModItems.DIAMOND_WAND_HEAD.get()),
                        Ingredient.of(Items.NETHERITE_INGOT),
                        RecipeCategory.MISC, ModItems.NETHERITE_WAND_HEAD.get())
                .unlocks("has_diamond_wand_head", has(ModItems.DIAMOND_WAND_HEAD.get()))
                .save(output, RabbitTools.loc("netherite_wand_head"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.INFINITY_WAND_HEAD.get())
                .requires(ModItems.NETHERITE_WAND_HEAD.get())
                .requires(ModItems.PRECISION_BUILDING_COMPONENT.get())
                .requires(Items.NETHER_STAR)
                .unlockedBy("has_netherite_wand_head", has(ModItems.NETHERITE_WAND_HEAD.get()))
                .save(output);
    }

    private void basicWandHeadRecipe(RecipeOutput output, ItemLike head, ItemLike component, Inp material) {
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, head)
                .requires(component)
                .requires(material.ingredient, 2)
                .unlockedBy("has_component", has(component))
                .save(output);
    }
    private void wandRecipe(RecipeOutput output, ItemLike wand, Inp material) {
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, wand)
                .define('X', material.ingredient)
                .define('#', Tags.Items.RODS_WOODEN)
                .pattern("  X")
                .pattern(" # ")
                .pattern("#  ")
                .unlockedBy("has_item", inventoryTrigger(material.predicate))
                .save(output);
    }

    private void coreRecipe(RecipeOutput output, ItemLike core, Inp item1, Inp item2) {
        // Blank cores have no recipe or automatic source: one is consumed per special core.
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, core)
                .define('B', ModItems.BLANK_CORE.get())
                .define('O', item1.ingredient)
                .define('X', item2.ingredient)
                .define('#', Tags.Items.GLASS_PANES)
                .pattern("O#X")
                .pattern("#B#")
                .pattern("X#O")
                .unlockedBy("has_blank_core", has(ModItems.BLANK_CORE.get()))
                .save(output);
    }

    private void voidSackRecipe(RecipeOutput output) {
        Inp coreDestruction = Inp.fromItem(ModItems.CORE_DESTRUCTION.get());
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.VOID_SACK.get())
                .define('C', coreDestruction.ingredient)
                .define('E', Items.DIAMOND)
                .define('O', Items.NETHERITE_INGOT)
                .define('D', Items.EMERALD)
                .pattern("EDE")
                .pattern("OCO")
                .pattern("EDE")
                .unlockedBy("has_core_destruction", inventoryTrigger(coreDestruction.predicate))
                .save(output);
    }

    private void specialRecipe(RecipeOutput output, SimpleCraftingRecipeSerializer<?> serializer) {
        ResourceLocation name = BuiltInRegistries.RECIPE_SERIALIZER.getKey(serializer);
        SpecialRecipeBuilder.special(RecipeWandUpgrade::new).save(output, RabbitTools.loc("dynamic/" + name.getPath()).toString());
    }
}