package dev.jsinco.recipes.gui

import dev.jsinco.recipes.Recipes
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

object GuiManager {
    
    private val guiCache = ConcurrentHashMap<java.util.UUID, CachedGuiData>()
    private val lastOpenTime = ConcurrentHashMap<java.util.UUID, Long>()
    private val inventoryLocks = ConcurrentHashMap<java.util.UUID, Boolean>()
    
    // Debounce time in milliseconds
    private const val DEBOUNCE_MS = 50L
    // Cache lifetime in milliseconds (30 seconds)
    private const val CACHE_LIFETIME_MS = 30000L
    
    private data class CachedGuiData(
        val gui: RecipesGui,
        val timestamp: Long,
        val adminMode: Boolean
    )
    
    init {
        // Schedule periodic cache cleanup
        Bukkit.getGlobalRegionScheduler().runAtFixedRate(
            Recipes.instance,
            { cleanupExpiredCache() },
            20,  // Run every 20 ticks (1 second)
            20   // Repeat every 20 ticks
        )
    }

    fun openRecipeGui(player: Player) {
        val currentTime = System.currentTimeMillis()
        val lastOpen = lastOpenTime[player.uniqueId] ?: 0L
        
        // Debounce: ignore rapid re-opens
        if (currentTime - lastOpen < DEBOUNCE_MS) {
            return
        }
        lastOpenTime[player.uniqueId] = currentTime
        
        val isAdmin = player.hasPermission("recipes.override.view")
        val playerUuid = player.uniqueId
        val cachedData = guiCache[playerUuid]
        
        // Use cached GUI if admin mode hasn't changed and cache is still valid
        if (cachedData != null && cachedData.adminMode == isAdmin && 
            (currentTime - cachedData.timestamp) < CACHE_LIFETIME_MS) {
            // Open cached GUI on main thread with minimal overhead
            Bukkit.getScheduler().runTask(Recipes.instance, Runnable {
                if (player.isOnline) {
                    // Just open the cached inventory - super fast!
                    player.openInventory(cachedData.gui.getInventory())
                }
            })
            return
        }
        
        // Mark as loading to prevent race conditions
        inventoryLocks[playerUuid] = true
        
        // Build new GUI completely asynchronously
        CompletableFuture.supplyAsync {
            if (isAdmin) {
                Recipes.recipes().values.map { breweryRecipe -> breweryRecipe.generateCompletedView() }
            } else {
                Recipes.recipeViewManager.getViews(playerUuid)
            }
        }.thenApplyAsync { recipeViews ->
            // Create items asynchronously - this is the expensive part
            val items = recipeViews.mapNotNull {
                Recipes.guiIntegration.createFullItem(it)
            }
            
            val gui = RecipesGui(player, items)
            gui.render()
            gui
        }.thenAcceptAsync({ gui ->
            // Only cache and open on main thread
            guiCache[playerUuid] = CachedGuiData(gui, currentTime, isAdmin)
            
            Bukkit.getScheduler().runTask(Recipes.instance, Runnable {
                if (player.isOnline) {
                    player.openInventory(gui.getInventory())
                }
                inventoryLocks.remove(playerUuid)
            })
        }, { command ->
            // Error handler - clear lock and log
            inventoryLocks.remove(playerUuid)
            try {
                command.run()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        })
    }
    
    fun invalidateCache(playerUuid: java.util.UUID) {
        guiCache.remove(playerUuid)
        lastOpenTime.remove(playerUuid)
    }
    
    fun invalidateAllCaches() {
        guiCache.clear()
        lastOpenTime.clear()
    }
    
    private fun cleanupExpiredCache() {
        val currentTime = System.currentTimeMillis()
        guiCache.keys.removeIf { uuid ->
            val data = guiCache[uuid] ?: return@removeIf true
            (currentTime - data.timestamp) > CACHE_LIFETIME_MS
        }
    }

}
