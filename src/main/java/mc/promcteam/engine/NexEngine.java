package mc.promcteam.engine;

import java.util.HashSet;
import java.util.Set;

import mc.promcteam.engine.core.Version;
import mc.promcteam.engine.core.config.CoreConfig;
import mc.promcteam.engine.core.config.CoreLang;
import mc.promcteam.engine.hooks.Hooks;
import mc.promcteam.engine.hooks.external.MythicMobsHK;
import mc.promcteam.engine.hooks.external.VaultHK;
import mc.promcteam.engine.hooks.external.WorldGuardHK;
import mc.promcteam.engine.hooks.external.citizens.CitizensHK;
import mc.promcteam.engine.nms.NMS;
import mc.promcteam.engine.nms.packets.PacketManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;

import mc.promcteam.engine.commands.api.IGeneralCommand;
import mc.promcteam.engine.commands.list.Base64Command;
import mc.promcteam.engine.hooks.HookManager;
import mc.promcteam.engine.manager.editor.EditorManager;
import mc.promcteam.engine.utils.Reflex;
import mc.promcteam.engine.utils.actions.ActionsManager;
import mc.promcteam.engine.utils.craft.CraftManager;

public class NexEngine extends NexPlugin<NexEngine> implements Listener {
	
	private static NexEngine instance;
	
	private CoreConfig cfg;
	private CoreLang lang;
	
	private Set<NexPlugin<?>> plugins;
	private HookManager hookManager;
	
	NMS nms;
	PluginManager pluginManager;
	PacketManager packetManager;
	ActionsManager actionsManager;
	CraftManager craftManager;
	
	VaultHK hookVault;
	CitizensHK hookCitizens;
	WorldGuardHK hookWorldGuard;
	MythicMobsHK hookMythicMobs;
	
	public NexEngine() {
	    instance = this;
	    this.plugins = new HashSet<>();
	}
	
	@NotNull
	public static NexEngine get() {
		return instance;
	}
	
	final boolean loadCore() {
		this.pluginManager = this.getServer().getPluginManager();
		
		if (!this.setupNMS()) {
			this.error("Could not setup NMS version. Plugin will be disabled.");
			return false;
		}
		
		this.getPluginManager().registerEvents(this, this);
		
		this.hookManager = new HookManager(this);
		this.hookManager.setup();
		
		this.packetManager = new PacketManager(this);
		this.packetManager.setup();
		
		this.actionsManager = new ActionsManager(this);
		this.actionsManager.setup();
		
		this.craftManager = new CraftManager(this);
		this.craftManager.setup();
		
		return true;
	}

    private boolean setupNMS() {
    	Version current = Version.CURRENT;
    	this.info("You are running MC version " + current);
    	if (current == null) return false;
    	
    	String pack = NMS.class.getPackage().getName();
    	Class<?> clazz = Reflex.getClass(pack, current.name());
    	if (clazz == null) return false;
    	
    	try {
			this.nms = (NMS) clazz.getConstructor().newInstance();
			this.info("Loaded NMS version: " + current.name());
		} 
    	catch (Exception e) {
			e.printStackTrace();
		}
    	return this.nms != null;
    }
	
	@Override
	public void enable() {
		EditorManager.setup();
		getLogger().info("ProMCCore has been enabled!");
	}

	@Override
	public void disable() {
		// Unregister Custom Actions Engine
		if (this.actionsManager != null) {
			this.actionsManager.shutdown();
			this.actionsManager = null;
		}
		if (this.packetManager != null) {
			this.packetManager.shutdown();
		}
		if (this.hookManager != null) {
			this.hookManager.shutdown();
		}
		if (this.craftManager != null) {
			this.craftManager.shutdown();
			this.craftManager = null;
		}
		EditorManager.shutdown();
	}

	@Override
	public void registerHooks() {
		this.hookVault = this.registerHook(Hooks.VAULT, VaultHK.class);
	}

	@Override
	public void registerCmds(@NotNull IGeneralCommand<NexEngine> mainCommand) {
		mainCommand.addSubCommand(new Base64Command(this));
	}

	@Override
	public void setConfig() {
		this.cfg = new CoreConfig(this);
		this.cfg.setup();
		
		this.lang = new CoreLang(this);
		this.lang.setup();
	}

	@Override
	@NotNull
	public CoreConfig cfg() {
		return this.cfg;
	}

	@Override
	@NotNull
	public CoreLang lang() {
		return this.lang;
	}

	@NotNull
	public HookManager getHookManager() {
		return this.hookManager;
	}
	
	void hookChild(@NotNull NexPlugin<?> child) {
		this.plugins.add(child);
	}

	@NotNull
	public Set<NexPlugin<?>> getChildPlugins() {
		return this.plugins;
	}
	
	@Override
	public void registerEditor() {
		
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onHookLate(PluginEnableEvent e) {
		String name = e.getPlugin().getName();
		if (this.hookMythicMobs == null && name.equalsIgnoreCase(Hooks.MYTHIC_MOBS)) {
			this.hookMythicMobs = this.registerHook(Hooks.MYTHIC_MOBS, MythicMobsHK.class);
			return;
		}
		if (this.hookWorldGuard == null && name.equalsIgnoreCase(Hooks.WORLD_GUARD)) {
			this.hookWorldGuard = this.registerHook(Hooks.WORLD_GUARD, WorldGuardHK.class);
			return;
		}
		if (this.hookCitizens == null && name.equalsIgnoreCase(Hooks.CITIZENS)) {
			this.hookCitizens = this.registerHook(Hooks.CITIZENS, CitizensHK.class);
			return;
		}
	}
}
