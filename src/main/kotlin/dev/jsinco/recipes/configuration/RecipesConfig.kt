package dev.jsinco.recipes.configuration

import com.dre.brewery.api.addons.AddonConfigFile
import com.dre.brewery.configuration.annotation.CommentSpace
import com.dre.brewery.configuration.annotation.DefaultCommentSpace
import com.dre.brewery.configuration.annotation.OkaeriConfigFileOptions
import com.dre.brewery.configuration.sector.AbstractOkaeriConfigSector
import com.dre.brewery.depend.okaeri.configs.OkaeriConfig
import com.dre.brewery.depend.okaeri.configs.annotation.Comment
import com.dre.brewery.depend.okaeri.configs.annotation.CustomKey
import dev.jsinco.recipes.guis.SortMethod
import dev.jsinco.recipes.guis.UnknownRecipeSortMethod
import dev.jsinco.recipes.permissions.PermissionSetter
import org.bukkit.Material

@DefaultCommentSpace(1)
@OkaeriConfigFileOptions("recipesConfig.yml", removeOrphans = true)
class RecipesConfig : AddonConfigFile() {

    @CommentSpace(0)
    @CustomKey("recipe-saving-method")
    @Comment("Recipes can be stored to a player via 2 methods:",
        "LuckPerms, Command",
        "When using LUCKPERMS, Recipes will use LuckPerms' API to set the permission node",
        "When using COMMAND, Recipes will run the command specified in the command field"
    )
    var recipeSavingMethod: PermissionSetter = PermissionSetter.COMMAND


    @CustomKey("permission-command")
    @Comment("The command to run when using COMMAND as the recipe-saving-method",
        "PLACEHOLDERS: %player% - The player's name",
        "              %permission% - The permission node",
        "              %boolean% - The boolean value of what the permission is being set to"
    )
    var permissionCommand: String = "lp user %player% permission set %permission% %boolean%"
    @CustomKey("permission-unset-command")
    var permissionUnsetCommand: String = "lp user %player% permission unset %permission%"



    @CustomKey("recipe-permission-node")
    @Comment("PLACEHOLDERS: %recipe% - The recipe's key in the config with spaces replaced as underscores \"_\"")
    var recipePermissionNode: String = "brewery.recipesaddon.recipe.%recipe%"


    @CustomKey("learn-recipe-upon-creation")
    @Comment("If true, the player will automatically learn the recipe when they create it for the first time",
        "Either by brewing the recipe or by receiving the brew via command")
    var learnRecipeUponCreation: Boolean = false

    @CustomKey("recipe-spawning")
    @Comment("Recipes spawn in loot chests randomly throughout the world",
        "Disable this by setting your chance to -1")
    var recipeSpawning: RecipeSpawningSection = RecipeSpawningSection()
    class RecipeSpawningSection : OkaeriConfig() {
        @CommentSpace(0)
        @Comment("The highest value that the random number can generate to. Ex: 100 means a random number between 0 and 100")
        var bound: Int = 100
        @CommentSpace(0)
        @Comment("The chance that a recipe will spawn in a loot chest")
        var chance: Int = 15
        @CommentSpace(0)
        @CustomKey("blacklisted-recipes")
        @Comment("A list of recipes that will not spawn in loot chests")
        var blacklistedRecipes: List<String> = listOf("ex")
    }

    @CustomKey("recipe-book-item")
    var recipeBookItem: ConfigItemSection = ConfigItemSection.Builder()
        .material(Material.BOOK)
        .name("&6&lRecipe Book")
        .lore("&7Right click to open", "&7your recipe book!")
        .glint(true)
        .build()


    @CustomKey("recipe-item")
    @Comment("The physical recipe item that will spawn in loot chests",
        "PLACEHOLDERS: %recipe% - The recipe's name")
    var recipeItem = ConfigItemSection.Builder()
        .material(Material.PAPER)
        .name("&#F7FFC9%recipe% &fRecipe")
        .lore("&7Right-Click to redeem", "&7this recipe!")
        .glint(true)
        .build()


    @Comment(
        "Full example Gui Border Item",
        "item:",
        "  material: SHORT_GRASS",
        "  slots: [ 0 ]",
        "  display_name: 'example item'",
        "  lore: [ 'example lore', 'another line' ]",
        "  glint: true",
        "  custom_model_data: -1",
    )
    var gui: GuiSection = GuiSection()
    class GuiSection : OkaeriConfig() {
        var title = "&#f670f1&lR&#dd7af6&le&#c584fa&lc&#ac8eff&li&#9c92ff&lp&#8d96ff&le&#7d9aff&ls"
        var size = 54


        @CustomKey("sort-method")
        @Comment("Determines how recipes are sorted in the Gui",
            "When using ALPHABETICAL, recipes are sorted by their name alphabetically, case insensitive",
            "When using DEFINITION, recipes are sorted in the same order they are defined in recipes.yml")
        var sortMethod = SortMethod.ALPHABETICAL

        @CustomKey("unknown-recipe-sort-method")
        @Comment("Determines how recipes a player does not know are sorted in the Gui",
            "When using KNOWN_FIRST, known recipes are shown first",
            "When using MIXED, known and unknown recipes are sorted together",
            "When using UNKNOWN_FIRST, unknown recipes are shown first",
            "The unknown-recipe item must not be AIR for unknown recipes to show up in the Gui")
        var unknownRecipeSortMethod = UnknownRecipeSortMethod.MIXED


        var items = GuiItemsSection()
        class GuiItemsSection : OkaeriConfig() {
            @CustomKey("recipe-gui-item")
            @Comment("The item that will be used to show all the recipes the player has",
                "When material is set to POTION material the RGB/Color specified for the potion in the config will be used",
                "PLACEHOLDERS: %recipe% - The recipe's name",
                "               %difficulty% - The difficulty of the recipe",
                "               %cooking_time% - The cooking time of the recipe",
                "               %distill_runs% - The distill runs of the recipe",
                "               %age% - The age of the recipe",
                "               %barrel_type% - The barrel wood type of the recipe",
                "               %ingredients% - The ingredients of the recipe using the ingredient-format"
            )
            var recipeGuiItem = RecipeGuiItemSection()

            class RecipeGuiItemSection : AbstractOkaeriConfigSector<ConfigItemSection>() {
                @CustomKey("ingredient-format")
                @Comment("The format for the ingredients",
                    "PLACEHOLDERS: %amount% - The amount of the ingredient",
                    "               %ingredient% - The ingredient's name"
                )
                var ingredientFormat = " &#F7FFC9%amount%x &f%ingredient%"
                var material = Material.POTION
                var slots = listOf(19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34)
                @CustomKey("display_name")
                var displayName = "&#F7FFC9%recipe% &fRecipe"
                var lore = listOf(
                    "&fDifficulty&7: &#F7FFC9%difficulty%",
                    "&fCooking Time&7: &#F7FFC9%cooking_time%m",
                    "&fDistill Runs&7: &#F7FFC9%distill_runs%",
                    "&fAge&7: &#F7FFC9%age%yrs &f(Minecraft days)",
                    "&fBarrel Type&7: &#F7FFC9%barrel_type%",
                    "",
                    "&7%lore%",
                    "",
                    "&fIngredients&7:",
                    "%ingredients%"
                )
                var glint = true
                @CustomKey("use-recipe-custom-model-data")
                @CommentSpace(0)
                @Comment("If true, the custom model data will be set to the recipe's custom model data")
                var useRecipeCustomModelData = false
            }

            @CustomKey("unknown-recipe")
            @Comment("The item shown when a player does not know a recipe",
                "Set this to AIR to disable completely and have only *known* recipes show up")
            var unknownRecipe = ConfigItemSection.Builder()
                .material(Material.AIR)
                .name("&#f498f6??? Recipe")
                .lore("&7This recipe is unknown to you")
                .glint(false)
                .build()


            @CustomKey("total_recipes")
            @Comment("PLACEHOLDERS: %total_recipes% - The total amount of recipes the player has out of the total recipes in Brewery's config")
            var totalRecipes = ConfigItemSection.Builder()
                .material(Material.BOOK)
                .slots(49)
                .name("&#f498f6Total Recipes&7: &e%total_recipes%")
                .glint(true)
                .build()


            @CustomKey("next_page")
            var nextPage = ConfigItemSection.Builder()
                .material(Material.ARROW)
                .slots(50)
                .name("&#f498f6Next Page")
                .lore()
                .build()


            @CustomKey("previous_page")
            var previousPage = ConfigItemSection.Builder()
                .material(Material.ARROW)
                .slots(48)
                .name("&#f498f6Previous Page")
                .lore()
                .build()


            @CustomKey("border-items")
            @Comment("Border items")
            var borderItems = BorderItemsSection()
            class BorderItemsSection : AbstractOkaeriConfigSector<ConfigItemSection>() {
                var border1 = ConfigItemSection.Builder()
                    .material(Material.GREEN_STAINED_GLASS_PANE)
                    .slots(0, 8, 45, 53)
                    .name("&0")
                    .build()
                var border2 = ConfigItemSection.Builder()
                    .material(Material.SHORT_GRASS)
                    .slots(1, 7, 46, 52)
                    .name("&0")
                    .build()
                var border3 = ConfigItemSection.Builder()
                    .material(Material.FERN)
                    .slots(2, 6, 47, 51)
                    .name("&0")
                    .build()
                var border4 = ConfigItemSection.Builder()
                    .material(Material.PINK_TULIP)
                    .slots(3, 5, 48, 50)
                    .name("&0")
                    .build()
                var border5 = ConfigItemSection.Builder()
                    .material(Material.LILY_PAD)
                    .slots(4, 49)
                    .name("&0")
                    .build()
            }
        }
    }

    var messages = MessagesSection()
    class MessagesSection : OkaeriConfig() {
        @CustomKey("already-learned")
        var alreadyLearned = "&rYou already know this recipe!"
        @Comment("PLACEHOLDERS: %recipe% the name of the recipe.")
        @CommentSpace(0)
        var learned = "&rYou have learned the '&#F7FFC9%recipe%&r' recipe!"
    }
}