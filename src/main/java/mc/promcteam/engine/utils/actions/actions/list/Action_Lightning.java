package mc.promcteam.engine.utils.actions.actions.list;

import java.util.List;
import java.util.Set;

import mc.promcteam.engine.NexPlugin;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

import mc.promcteam.engine.utils.actions.actions.IActionExecutor;
import mc.promcteam.engine.utils.actions.actions.IActionType;
import mc.promcteam.engine.utils.actions.params.IParamResult;
import mc.promcteam.engine.utils.actions.params.IParamType;

public class Action_Lightning extends IActionExecutor {

	public Action_Lightning(@NotNull NexPlugin<?> plugin) {
		super(plugin, IActionType.LIGHTNING);
	}

	@Override
	@NotNull
	public List<String> getDescription() {
		return plugin.lang().Core_Editor_Actions_Action_Lightning_Desc.asList();
	}
	
	@Override
	public void registerParams() {
		this.registerParam(IParamType.TARGET);
	}

	@Override
	protected void execute(@NotNull Entity exe, @NotNull Set<Entity> targets, @NotNull IParamResult result) {
		for (Entity e : targets) {
			e.getWorld().strikeLightning(e.getLocation());
		}
	}

	@Override
	public boolean mustHaveTarget() {
		return true;
	}

}
