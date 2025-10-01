package dev.jsinco.recipes.data

import dev.jsinco.recipes.core.RecipeView
import java.util.*
import java.util.concurrent.CompletableFuture

interface StorageImpl {

    fun getType(): StorageType

    fun insertOrUpdateRecipeView(playerUuid: UUID, recipeView: RecipeView): CompletableFuture<Void>

    fun removeRecipeView(playerUuid: UUID, recipeKey: String): CompletableFuture<Void>

    fun selectAllRecipeViews(): CompletableFuture<Map<UUID, List<RecipeView>>>

    fun createTables()
}