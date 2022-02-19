package io.github.treesoid.mongoauthkeys.mixin;

import io.github.treesoid.mongoauthkeys.MongoAuthKeys;
import io.github.treesoid.mongoauthkeys.util.AddServerButton;
import net.minecraft.client.gui.screen.AddServerScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AddServerScreen.class)
public class AddServerScreenMixin extends Screen {
    @Shadow
    private TextFieldWidget addressField;
    @Unique
    private AddServerButton mongoauthkeys_toggleButton;

    private AddServerScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        mongoauthkeys_toggleButton = this.addDrawableChild(new AddServerButton(10, 10, 150, 20,
                new TranslatableText("mongo-auth-keys.enabledOnServer").formatted(Formatting.GREEN),
                new TranslatableText("mongo-auth-keys.disabledOnServer").formatted(Formatting.RED),
                button -> {
                    if (button instanceof AddServerButton) {
                        ((AddServerButton) button).toggle();
                    }
                }));
        mongoauthkeys_toggleButton.state = MongoAuthKeys.isAllowedOnServer(this.addressField.getText());
    }

    @Inject(method = "addAndClose", at = @At("TAIL"))
    private void onAddAndClose(CallbackInfo ci) {
        if (mongoauthkeys_toggleButton.state) {
            MongoAuthKeys.addAllowedServer(this.addressField.getText());
        } else {
            MongoAuthKeys.removeAllowedServer(this.addressField.getText());
        }
    }

    @Inject(method = "updateAddButton", at = @At("TAIL"))
    private void updateAddButton(CallbackInfo ci) {
        if (mongoauthkeys_toggleButton != null) {
            mongoauthkeys_toggleButton.state = MongoAuthKeys.isAllowedOnServer(this.addressField.getText());
        }
    }
}
