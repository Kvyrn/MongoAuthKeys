package io.github.treesoid.mongoauthkeys.mixin;

import com.mojang.authlib.minecraft.MinecraftSessionService;
import io.github.treesoid.mongoauthkeys.MongoAuthKeys;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientLoginNetworkHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientLoginNetworkHandler.class)
public abstract class ClientLoginNetworkHandlerMixin {
    /*@Inject(method = "joinServerSession", at = @At("TAIL"), cancellable = true)
    private void joinServerSession(String serverId, CallbackInfoReturnable<Text> cir) {
        cir.setReturnValue(null);
    }*/

    @Shadow
    protected abstract MinecraftSessionService getSessionService();

    @Shadow
    @Final
    private MinecraftClient client;

    @Inject(method = "joinServerSession", at = @At("HEAD"), cancellable = true)
    private void joinServerSession(String serverId, CallbackInfoReturnable<Text> cir) {
        cir.setReturnValue(null);
        try {
            this.getSessionService().joinServer(this.client.getSession().getProfile(), this.client.getSession().getAccessToken(), serverId);
        } catch (Exception e) {
            //
        }

        MongoAuthKeys.registerRecivers();
    }
}
