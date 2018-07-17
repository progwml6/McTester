package org.spongepowered.mctester.internal.mixin;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import org.apache.commons.lang3.Validate;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.mctester.internal.interfaces.IMixinMinecraft;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.world.WorldSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.mctester.api.RunnerEvents;

import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import javax.annotation.Nullable;

// McTester always runs in a deobfuscated environment - it depends on GradleStart, after all!
// Therefore, we disable all remapping, since we'll never need it.
@Mixin(value = Minecraft.class, remap = false)
public abstract class MixinMinecraft implements IMixinMinecraft {

    @Shadow public abstract void displayGuiScreen(@Nullable GuiScreen guiScreenIn);

    @Shadow public abstract void launchIntegratedServer(String folderName, String worldName, @Nullable WorldSettings worldSettingsIn);

    @Shadow private volatile boolean running;

    @Shadow protected abstract void clickMouse();

    @Shadow protected abstract void rightClickMouse();

    @Shadow @Final private Queue<FutureTask<?>> scheduledTasks;

    @Shadow public GameSettings gameSettings;
    private boolean leftClicking;
    private boolean rightClicking;

    private boolean allowPause = false;

    @Inject(method = "init", at = @At(value = "RETURN"))
    public void onInitDone(CallbackInfo ci) {
        RunnerEvents.setClientInit();
    }

    /*@Redirect(method = "init", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;serverName:Ljava/lang/String;", ordinal = 0))
    public String onGetServerName(Minecraft minecraft) {
        return "blah";
    }

    @Redirect(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/fml/client/FMLClientHandler;connectToServerAtStartup(Ljava/lang/String;I)V"))
    public void onConnect(FMLClientHandler handler, String serverName, int serverPort) {
        this.displayGuiScreen(null);

        long seed = new Random().nextLong();
        String folderName = "MCTestWorld-" + String.valueOf(seed).substring(0, 5);

        WorldSettings worldsettings = new WorldSettings(seed, GameType.CREATIVE, false, false, WorldType.FLAT);
        this.launchIntegratedServer(folderName, folderName, worldsettings);
    }*/

    @Redirect(method = "shutdownMinecraftApplet", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/fml/common/asm/transformers/TerminalTransformer$ExitVisitor;systemExitCalled(I)V", remap = false))
    public void onSystemExitCalled(int code) {
        // Notify any listenres that the game has closed, but don't actually
        // call System.exit here. We want to let JUnit exit cleanly.
        RunnerEvents.setGameClosed();
    }

    @Inject(method = "stopIntegratedServer", at = @At("HEAD"), cancellable = true)
    private static void onStopIntegratedServer(CallbackInfo ci) {
        // If we're already shutting down, don't try to shutdown again
        if (Minecraft.getMinecraft() != null && !((IMixinMinecraft) Minecraft.getMinecraft()).isRunning()) {
            ci.cancel();
        }
    }

    @Inject(method = "processKeyBinds", at = @At("HEAD"))
    public void onProcessKeyBinds(CallbackInfo ci) {
        if (this.leftClicking) {

        }

        if (this.rightClicking) {

        }
    }

    @Override
    public <T> ListenableFuture<T> addScheduledTaskAlwaysDelay(Callable<T> callable) {
        Validate.notNull(callable);
        ListenableFutureTask<T> listenablefuturetask = ListenableFutureTask.<T>create(callable);

        synchronized (this.scheduledTasks)
        {
            this.scheduledTasks.add(listenablefuturetask);
            return listenablefuturetask;
        }
    }


    @Override
    public boolean isRunning() {
        return this.running;
    }

    @Override
    public void leftClick() {
        this.clickMouse();
    }

    @Override
    public void holdLeftClick(boolean clicking) {
        this.leftClicking = clicking;

        KeyBinding attack = this.gameSettings.keyBindAttack;

        KeyBinding.setKeyBindState(attack.getKeyCode(), clicking);
        if (clicking) {
            KeyBinding.onTick(attack.getKeyCode());
        }
    }

    @Override
    public void holdRightClick(boolean clicking) {
        this.rightClicking = clicking;

        KeyBinding useItem = this.gameSettings.keyBindUseItem;

        KeyBinding.setKeyBindState(useItem.getKeyCode(), clicking);
        if (clicking) {
            KeyBinding.onTick(useItem.getKeyCode());
        }
    }

    @Override
    public void rightClick() {
        this.rightClickMouse();
    }

    @Redirect(method = "runGameLoop", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiScreen;doesGuiPauseGame()Z"))
    public boolean onDoesGuiPauseGame(GuiScreen screen) {
        return this.allowPause && screen.doesGuiPauseGame();
    }

    @Override
    public void setAllowPause(boolean allowPause) {
        this.allowPause = allowPause;
    }
}
