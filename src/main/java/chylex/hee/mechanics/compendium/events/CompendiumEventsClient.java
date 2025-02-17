package chylex.hee.mechanics.compendium.events;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.inventory.Slot;
import net.minecraft.stats.Achievement;
import net.minecraft.util.ChatComponentText;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import chylex.hee.HardcoreEnderExpansion;
import chylex.hee.gui.ContainerEndPowderEnhancements;
import chylex.hee.gui.GuiEnderCompendium;
import chylex.hee.mechanics.compendium.KnowledgeRegistrations;
import chylex.hee.mechanics.compendium.content.KnowledgeObject;
import chylex.hee.mechanics.compendium.objects.IKnowledgeObjectInstance;
import chylex.hee.mechanics.compendium.player.PlayerCompendiumData;
import chylex.hee.mechanics.compendium.util.KnowledgeUtils;
import chylex.hee.packets.PacketPipeline;
import chylex.hee.packets.server.S03SimpleEvent;
import chylex.hee.packets.server.S03SimpleEvent.EventType;
import chylex.hee.proxy.ModClientProxy;
import chylex.hee.proxy.ModCommonProxy;
import chylex.hee.system.achievements.AchievementManager;
import chylex.hee.system.logging.Stopwatch;
import chylex.hee.system.util.ReflectionUtils;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class CompendiumEventsClient{
	
	private static CompendiumEventsClient instance;
	
	public static CompendiumEventsClient getInstance() {
		return instance;
	}
	
	public static void register(){
		if (instance == null)FMLCommonHandler.instance().bus().register(instance = new CompendiumEventsClient());
	}
	
	public static void loadClientData(PlayerCompendiumData data){
		instance.data = data;
		
		if (Minecraft.getMinecraft().currentScreen instanceof GuiEnderCompendium){
			((GuiEnderCompendium)Minecraft.getMinecraft().currentScreen).updateCompendiumData(data);
		}
		
		if (Minecraft.getMinecraft().thePlayer.openContainer instanceof ContainerEndPowderEnhancements){
			((ContainerEndPowderEnhancements)Minecraft.getMinecraft().thePlayer.openContainer).updateClientItems();
		}
	}
	
	public static PlayerCompendiumData getClientData(){
		return instance.data;
	}
	
	public static boolean canOpenCompendium(){
		if (instance.data == null){
			Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(I18n.format("compendium.dataError")));
			return false;
		}
		else return true;
	}
	
	public static void openCompendium(KnowledgeObject<? extends IKnowledgeObjectInstance<?>> obj, GuiScreen prevScreen){
		GuiEnderCompendium compendium = new GuiEnderCompendium(instance.data,prevScreen);
		Minecraft.getMinecraft().displayGuiScreen(compendium);
		
		if (obj != null){
			compendium.showObject(obj);
			compendium.moveToCurrentObject(false);
		}
		else if (!instance.data.seenHelp())compendium.showObject(KnowledgeRegistrations.HELP);
	}
	
	public static int getCompendiumKeyCode(){
		return instance.keyOpenCompendium.getKeyCode();
	}
	
	public static void onObjectDiscovered(int objectID){
		instance.newlyDiscoveredId = (short)objectID;
		instance.newlyDiscoveredTime = System.nanoTime();
	}
	
	public static void showCompendiumAchievement(){
		instance.displayAchievement(AchievementManager.ENDER_COMPENDIUM);
	}
	
	public static void showVoidChestAchievement(){
		ModClientProxy.modifyVoidChestDescription = true;
		instance.displayAchievement(AchievementManager.VOID_CHEST);
		ModClientProxy.modifyVoidChestDescription = false;
	}
	
	private final KeyBinding keyOpenCompendium;
	private PlayerCompendiumData data;
	private short newlyDiscoveredId = -1;
	private long newlyDiscoveredTime = 0L;
	private byte achievementTimer = Byte.MIN_VALUE;
	
	private CompendiumEventsClient(){
		keyOpenCompendium = new KeyBinding(ModCommonProxy.hardcoreEnderbacon ? "key.openCompendium.bacon" : "key.openCompendium",25,"Hardcore Ender Expansion");
		ClientRegistry.registerKeyBinding(keyOpenCompendium);
		Minecraft.getMinecraft().gameSettings.loadOptions();
	}
	
	private void displayAchievement(Achievement achievement){
		Minecraft.getMinecraft().guiAchievement.func_146255_b(achievement);
		instance.achievementTimer = 120;
	}
	
	public boolean isKeybindingValid() {
		if (keyOpenCompendium == null || keyOpenCompendium.getKeyCode() == 0) {
			return false;
		}		
		return true;
	}
	
	@SubscribeEvent
	public void onPlayerLogin(PlayerLoggedInEvent e){
		Stopwatch.time("CompendiumEventsClient - key conflict check");
		
		for(KeyBinding kb:Minecraft.getMinecraft().gameSettings.keyBindings){
			if (isKeybindingValid() && kb != instance.keyOpenCompendium && kb.getKeyCode() == instance.keyOpenCompendium.getKeyCode()){
				HardcoreEnderExpansion.notifications.report(I18n.format("key.openCompendium.conflict").replace("$",I18n.format(kb.getKeyDescription())));
				break;
			}
		}

		Stopwatch.finish("CompendiumEventsClient - key conflict check");
	}
	
	@SubscribeEvent
	public void onClientTick(ClientTickEvent e){
		if (e.phase != Phase.START)return;
		
		Minecraft mc = Minecraft.getMinecraft();
		
		if (achievementTimer > Byte.MIN_VALUE && --achievementTimer == Byte.MIN_VALUE)Minecraft.getMinecraft().guiAchievement.func_146257_b();
		
		if (isKeybindingValid() && (keyOpenCompendium.isPressed() || Keyboard.getEventKeyState() && Keyboard.getEventKey() == keyOpenCompendium.getKeyCode()) && (mc.inGameHasFocus || mc.currentScreen instanceof GuiContainer)){
			if (canOpenCompendium()){
				KnowledgeObject<? extends IKnowledgeObjectInstance<?>> obj = null;
				
				if (mc.inGameHasFocus){
					if (newlyDiscoveredTime != 0L && System.nanoTime()-newlyDiscoveredTime <= 7000000000L){
						obj = KnowledgeObject.getObjectById(newlyDiscoveredId);
						newlyDiscoveredId = -1;
						newlyDiscoveredTime = 0L;
					}
					else obj = CompendiumEvents.getObservation(mc.thePlayer).getObject();
				}
				else{
					GuiContainer container = (GuiContainer)mc.currentScreen;
					List<Slot> slots = container.inventorySlots.inventorySlots;
					ScaledResolution res = new ScaledResolution(mc,mc.displayWidth,mc.displayHeight);
					
					int mouseX = Mouse.getX()*res.getScaledWidth()/mc.displayWidth,
						mouseY = res.getScaledHeight()-Mouse.getY()*res.getScaledHeight()/mc.displayHeight-1;

					int xSize = ReflectionUtils.getFieldValue(container, "xSize");
					int ySize = ReflectionUtils.getFieldValue(container, "ySize");
					
					mouseX -= (container.width-xSize)/2;
					mouseY -= (container.height-ySize)/2;
					
					for(Slot slot:slots){
						if (slot.getHasStack() && slot.func_111238_b() &&
							mouseX >= slot.xDisplayPosition-1 && mouseX <= slot.xDisplayPosition+16 &&
							mouseY >= slot.yDisplayPosition-1 && mouseY <= slot.yDisplayPosition+16){
							obj = KnowledgeUtils.tryGetFromItemStack(slot.getStack());
							break;
						}
					}
					
					if (obj == null)return;
				}
				
				openCompendium(obj,mc.currentScreen);
				
				if (!mc.thePlayer.getStatFileWriter().hasAchievementUnlocked(AchievementManager.ENDER_COMPENDIUM)){
					PacketPipeline.sendToServer(new S03SimpleEvent(EventType.OPEN_COMPENDIUM));
					achievementTimer = Byte.MIN_VALUE;
				}
			} 
		}
	}
}
