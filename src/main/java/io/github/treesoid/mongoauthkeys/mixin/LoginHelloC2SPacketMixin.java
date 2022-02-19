package io.github.treesoid.mongoauthkeys.mixin;

import io.github.treesoid.mongoauthkeys.MongoAuthKeys;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.login.LoginHelloC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LoginHelloC2SPacket.class)
public class LoginHelloC2SPacketMixin {
    @Inject(method = "write", at = @At("TAIL"))
    private void insertMetadata(PacketByteBuf buf, CallbackInfo ci) {
        if (!MongoAuthKeys.version.equals("invalid") && MongoAuthKeys.useOnCurrentServer() && MongoAuthKeys.getKeyPair() != null) {
            buf.writeString(MongoAuthKeys.version);
        }
    }
}
