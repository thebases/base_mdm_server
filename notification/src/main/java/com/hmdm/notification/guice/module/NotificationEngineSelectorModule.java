package com.hmdm.notification.guice.module;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import com.hmdm.notification.PushSender;
//import com.hmdm.notification.PushSenderMqtt;
import com.hmdm.notification.PushSenderPolling;
import com.hmdm.notification.BasePushSender;

public class NotificationEngineSelectorModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(PushSender.class)
                .annotatedWith(Names.named("Polling"))
                .to(PushSenderPolling.class);
        bind(PushSender.class)
                .annotatedWith(Names.named("MQTT"))
                .to(BasePushSender.class);
    }
}
