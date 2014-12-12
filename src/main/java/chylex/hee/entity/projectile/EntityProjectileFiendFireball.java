package chylex.hee.entity.projectile;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityLargeFireball;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import chylex.hee.entity.projectile.EntityProjectileGolemFireball.FieryExplosion;
import chylex.hee.proxy.ModCommonProxy;
import chylex.hee.system.util.MathUtil;

public class EntityProjectileFiendFireball extends EntityLargeFireball{
	private double centerX, centerZ;
	private float ang;
	private byte timer;
	
	public EntityProjectileFiendFireball(World world){
		super(world);
		setSize(0.2F,0.2F);
	}
	
	public EntityProjectileFiendFireball(World world, EntityLivingBase shooter, double x, double y, double z, double ang, int timer){
		super(world,shooter,0D,0D,0D);
		setPosition(x,y,z);
		setSize(0.2F,0.2F);
		this.centerX = x;
		this.centerZ = z;
		this.ang = (float)MathUtil.toRad(ang);
		this.timer = (byte)timer;
	}
	
	@Override
	public void onUpdate(){
		if (!worldObj.isRemote && timer > 0 && --timer > 0){
			onEntityUpdate();
			setPosition(centerX+MathHelper.cos(ang)*1.5D,posY,centerZ+MathHelper.sin(ang)*1.5D);
			
			if (timer == 0){
				if (worldObj.playerEntities.isEmpty())setDead();
				else{
					EntityPlayer target = (EntityPlayer)worldObj.playerEntities.get(rand.nextInt(worldObj.playerEntities.size()));
					double diffX = target.posX-posX, diffY = target.posY-posY, diffZ = target.posZ-posZ;
					
					diffX += rand.nextGaussian()*0.1D;
					diffY += rand.nextGaussian()*0.1D;
					diffZ += rand.nextGaussian()*0.1D;
					
					double dist = MathUtil.distance(diffX,diffY,diffZ);
					accelerationX = diffX/dist*0.1D;
					accelerationY = diffY/dist*0.1D;
					accelerationZ = diffZ/dist*0.1D;
				}
			}
		}
		else super.onUpdate();
	}

	@Override
	protected void onImpact(MovingObjectPosition mop){
		if (!worldObj.isRemote){
			if (mop.entityHit != null)mop.entityHit.attackEntityFrom(DamageSource.causeFireballDamage(this,shootingEntity),ModCommonProxy.opMobs ? 12F : 7F);

			Explosion explosion = new FieryExplosion(worldObj,shootingEntity,posX,posY,posZ,ModCommonProxy.opMobs ? 3.4F : 2.8F);
			explosion.doExplosionA();
			explosion.doExplosionB(true);
			
			setDead();
		}
	}
	
	@Override
	public boolean isBurning(){
		return false;
	}
}
