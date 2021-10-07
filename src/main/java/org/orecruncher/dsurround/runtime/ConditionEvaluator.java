package org.orecruncher.dsurround.runtime;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import org.orecruncher.dsurround.lib.GameUtils;
import org.orecruncher.dsurround.lib.scripting.ExecutionContext;
import org.orecruncher.dsurround.runtime.sets.*;

import java.util.Optional;

@Environment(EnvType.CLIENT)
public final class ConditionEvaluator {

    public static ConditionEvaluator INSTANCE = new ConditionEvaluator();

    static {
            // Setup ticker for the variables.  Only want to tick while in game and
            // the GUI is not paused.
            ClientTickEvents.START_CLIENT_TICK.register(client -> {
                if (GameUtils.isInGame() && !client.isPaused())
                    INSTANCE.tick();
            });
    }

    private final ExecutionContext context;

    // Internal visibility for diagnostics
    final BiomeVariables biomeVariables;
    final DimensionVariables dimensionVariables;
    final DiurnalVariables diurnalVariables;
    final PlayerVariables playerVariables;
    final WeatherVariables weatherVariables;
    final EnvironmentState environmentState;

    public ConditionEvaluator() {
        this(true);
    }

    public ConditionEvaluator(boolean cacheMethods) {
        this.context = new ExecutionContext("Conditions", cacheMethods);
        this.context.add(this.biomeVariables = new BiomeVariables());
        this.context.add(this.dimensionVariables = new DimensionVariables());
        this.context.add(this.diurnalVariables = new DiurnalVariables());
        this.context.add(this.playerVariables = new PlayerVariables());
        this.context.add(this.weatherVariables = new WeatherVariables());
        this.context.add(this.environmentState = new EnvironmentState());
    }

    public void tick() {
        this.context.update();
    }

    public boolean check(final String conditions) {
        final Object result = eval(conditions);
        return result instanceof Boolean && (boolean) result;
    }

    public Object eval(final String conditions) {
        if (conditions == null || conditions.length() == 0)
            return true;
        final Optional<Object> result = this.context.eval(conditions);
        return result.orElse(false);
    }
}