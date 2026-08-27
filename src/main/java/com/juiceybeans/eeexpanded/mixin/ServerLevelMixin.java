package com.juiceybeans.eeexpanded.mixin;

import com.juiceybeans.eeexpanded.core.ServerLevelRuns;

import net.minecraft.server.level.ServerLevel;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BooleanSupplier;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin implements ServerLevelRuns {

    @Unique
    private static final Map<Integer, Runnable> eeexpanded$scheduledTasks = new HashMap<>();

    @Inject(method = "tick", at = @At("TAIL"))
    public void eeexpanded$tickTask(BooleanSupplier hasTimeLeft, CallbackInfo ci) {
        ServerLevel self = (ServerLevel) (Object) this;
        int currentTick = self.getServer().getTickCount();

        for (Integer tick : eeexpanded$scheduledTasks.keySet().toArray(new Integer[0])) {
            if (tick <= currentTick) {
                Runnable task = eeexpanded$scheduledTasks.remove(tick);
                if (task != null) task.run();
            }
        }
    }

    @Unique
    public void eeexpanded$addServerLevelRun(int tickLimit, Runnable run) {
        ServerLevel self = (ServerLevel) (Object) this;
        int executeAtTick = self.getServer().getTickCount() + tickLimit;
        eeexpanded$scheduledTasks.put(executeAtTick, run);
    }
}
