package chylex.hee.world.structure.tower;
import java.util.Random;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureStart;

public class StructureTower extends StructureStart{
	public StructureTower(){}
	
	@SuppressWarnings("unchecked")
	public StructureTower(World world, Random rand, int x, int z){
		super(x,z);
		components.add(new ComponentScatteredFeatureTower(rand,x*16,z*16));
		updateBoundingBox();
	}
}