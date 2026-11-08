package com.tr4nce.rabbittools.containers;

import com.tr4nce.rabbittools.RabbitTools;
import com.tr4nce.rabbittools.containers.handlers.HandlerBundle;
import com.tr4nce.rabbittools.containers.handlers.HandlerCapability;
import com.tr4nce.rabbittools.containers.handlers.HandlerShulkerbox;

public final class ContainerRegistrar {
    private ContainerRegistrar() {
    }

    public static void register() {
        RabbitTools.instance.containerManager.register(new HandlerCapability());
        RabbitTools.instance.containerManager.register(new HandlerShulkerbox());
        RabbitTools.instance.containerManager.register(new HandlerBundle());
    }
}