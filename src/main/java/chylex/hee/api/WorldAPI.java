package chylex.hee.api;
import net.minecraft.entity.EntityLiving;
import net.minecraft.world.biome.BiomeGenBase.SpawnListEntry;
import chylex.hee.api.wrappers.LootList;
import chylex.hee.world.biome.BiomeGenHardcoreEnd;
import chylex.hee.world.structure.island.biome.IslandBiomeBase;
import chylex.hee.world.structure.island.feature.StructureForestSilverfishDungeon;
import chylex.hee.world.structure.tower.ComponentScatteredFeatureTower;
import chylex.hee.world.util.SpawnEntry;

/**
 * API for manipulating with new biomes and loot.
 */
public final class WorldAPI extends AbstractAPI{
	WorldAPI(){}
	
	/**
	 * Adds a monster to the dimension ({@link BiomeGenBase.spawnableMonsterList}).
	 * @param mobClass Class of the mob to spawn.
	 * @param minAmount Minimum amount of mob instances spawned at once.
	 * @param maxAmount Maximum amount of mob instances spawned at once.
	 * @param weight Weight of the spawn entry (Enderman has a weight of 10).
	 */
	public static void addMonsterToDimension(Class<? extends EntityLiving> mobClass, int minGroup, int maxGroup, int weight){
		BiomeGenHardcoreEnd.addMonsterSpawnEntry(new SpawnListEntry(mobClass,weight,minGroup,maxGroup));
	}
	
	/**
	 * Adds a mob spawning entry to specified biome. List of existing entries is in javadoc of each {@link Biome}.
	 * @param biome Biome to add the entry to.
	 * @param mobClass Class of the mob to spawn.
	 * @param maxAmount Maximum amount of mobs of this type that can exist on the island at one point in time.
	 * @param weight Weight of the spawn entry.
	 */
	public static void addMobToBiome(Biome biome, Class<? extends EntityLiving> mobClass, int maxAmount, int weight){
		SpawnEntry entry = new SpawnEntry(mobClass,maxAmount,weight);
		
		switch(biome){
			case InfestedForest: IslandBiomeBase.infestedForest.spawnEntries.add(entry); break;
			case BurningMountains: IslandBiomeBase.burningMountains.spawnEntries.add(entry); break;
			case EnchantedIsland: IslandBiomeBase.enchantedIsland.spawnEntries.add(entry); break;
		}
	}
	
	/**
	 * Returns manipulatable loot list. Always cache the returned object if you plan on using it multiple times!
	 * @param loot Type of loot to use.
	 * @return Instance of {@link chylex.hee.api.wrappers.LootList LootList}, or null if {@code loot} parameter is null.
	 */
	public LootList getLootList(LootType loot){
		switch(loot){
			case DungeonTowerRegular: return new LootList(ComponentScatteredFeatureTower.lootTower);
			case DungeonTowerFuel: return new LootList(ComponentScatteredFeatureTower.lootFuel);
			case SilverfishDungeon: return new LootList(StructureForestSilverfishDungeon.lootDungeon);
			default: return null;
		}
	}
	
	/**
	 * List of biomes that generate in the End.
	 */
	public static enum Biome{
		/**
		 * <strong>{@code Mob type, max amount, weight}</strong><br>
		 * {@code EntitySilverfish, 35, 35}<br>
		 * {@code EntityMobInfestedBat, 8, 10}<br>
		 */
		InfestedForest,
		
		/**
		 * <strong>{@code Mob type, max amount, weight}</strong><br>
		 * {@code EntityMobFireGolem, 14, 10}<br>
		 * {@code EntityMobScorchingLens, 10, 6}<br>
		 */
		BurningMountains,
		
		/**
		 * <strong>{@code Mob type, max amount, weight}</strong><br>
		 * {@code EntityMobEnderGuardian, 9, 30}<br>
		 * {@code EntityMobBabyEnderman, 16, 20}<br>
		 */
		EnchantedIsland
	}

	/**
	 * List of structures which generate loot inside them.
	 */
	public static enum LootType{
		/**
		 * Loot spawned inside chests, dispensers and similar containers in Dungeon Tower.
		 */
		DungeonTowerRegular,
		
		/**
		 * Loot spawned inside fuel slot of furnaces in Dungeon Tower.
		 */
		DungeonTowerFuel,
		
		/**
		 * Loot spawned inside Silverfish Dungeon chest.
		 */
		SilverfishDungeon
	}
}