package com.hmdm.notification;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.hmdm.notification.persistence.domain.PushMessage;
import com.hmdm.persistence.UnsecureDAO;
import com.hmdm.persistence.domain.Device;
import com.hmdm.util.BackgroundTaskRunnerService;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLSocketFactory;

@Singleton
public class BasePushSender implements PushSender {

    private String serverUri;
    private String clientTag;
    private boolean mqttAuth;
    private boolean useTLS;
    private String mqttAdminUsername;
    private String mqttAdminPassword;
    private UnsecureDAO unsecureDAO;
    private MqttClient client;
    private MqttThrottledSender throttledSender;
    private BackgroundTaskRunnerService taskRunner;
    private MemoryPersistence persistence = new MemoryPersistence();
    private long mqttDelay;
    private Logger log = LoggerFactory.getLogger(BasePushSender.class);

    @Inject
    public BasePushSender(@Named("mqtt.server.uri") String serverUri,
                          @Named("mqtt.client.tag") String clientTag,
                          @Named("mqtt.auth") boolean mqttAuth,
                          @Named("mqtt.ssl") boolean useTLS,
                          @Named("mqtt.admin.username") String mqttAdminUsername,
                          @Named("mqtt.admin.password") String mqttAdminPassword,
                          @Named("mqtt.message.delay") long mqttDelay,
                          MqttThrottledSender throttledSender,
                          BackgroundTaskRunnerService taskRunner,
                          UnsecureDAO unsecureDAO) {
        this.serverUri = serverUri;
        this.clientTag = clientTag;
        this.mqttAuth = mqttAuth;
        this.mqttAdminPassword = mqttAdminPassword;
        this.mqttDelay = mqttDelay;
        this.throttledSender = throttledSender;
        this.taskRunner = taskRunner;
        this.unsecureDAO = unsecureDAO;
        this.useTLS = useTLS;
        this.mqttAdminUsername = mqttAdminUsername;
    }

    @Override
    public void init() {
        log.info("=========== Initializing Base BasePushSender ==========");
        String subTopic = "testtopic/#";
        String pubTopic = "testtopic/1";
        String content = "Hello World - This is the message from MDM";
        int qos = 0;
        try {
            if (useTLS){
                client = new MqttClient("ssl://" + serverUri, "Base-" + clientTag, persistence);

            }
            else {
                client = new MqttClient("tcp://" + serverUri, "Base-" + clientTag, persistence);
            }
            MqttConnectOptions options = new MqttConnectOptions();
            if (mqttAuth) {
                options.setUserName(mqttAdminUsername);
                options.setPassword(mqttAdminPassword.toCharArray());
            }
            if (useTLS){
                System.setProperty("jdk.internal.httpclient.disableHostnameVerification", "true");
                options.setSocketFactory(SSLSocketFactory.getDefault());
            }
            options.setAutomaticReconnect(true);
            // retain session
            options.setCleanSession(true);

            // establish a connection
            log.info("Connecting to broker: server: " + serverUri);
            log.info("Connecting to broker: useTLS: " + (useTLS?"True":"False"));
            log.info("Connecting to broker: user: " + mqttAdminUsername);
            log.info("Connecting to broker: password: " + mqttAdminPassword);
            client.connect(options);


            log.info("Connected");
            log.info("Publishing message: " + content);
            // Subscribe
            log.info("Subscribe Test Topic");
            client.subscribe(subTopic);

            // Required parameters for message publishing
            MqttMessage message = new MqttMessage(content.getBytes());
            message.setQos(qos);
            client.publish(pubTopic, message);
            log.info("Message published");
            log.info("Unsubscribe Test Topic");
            client.unsubscribe(subTopic);

            if (mqttDelay > 0) {
                throttledSender.setClient(client);
                taskRunner.submitTask(throttledSender);
            }

        } catch (MqttException e) {
//            log.info("reason " + e.getReasonCode());
//            log.info("msg " + e.getMessage());
//            log.info("loc " + e.getLocalizedMessage());
//            log.info("cause " + e.getCause());
//            log.info("excep " + e);
           e.printStackTrace();  // Replace with SLF4J log
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public int send(PushMessage message) {
        log.info("=========== Sending Base BasePushSender ==========");
        if (this.client == null || !this.client.isConnected()) {
            log.warn("MQTT client not initialized or disconnected, reconnecting...");
            this.init();
            if (this.client == null || !this.client.isConnected()) {
                log.error("Reconnection failed. MQTT message not sent.");
                return 0;
            }
        }

        // Since this method is used by scheduled task service which is impersonated,
        // we use UnsecureDAO here (which doesn't check the signed user).
        Device device = unsecureDAO.getDeviceById(message.getDeviceId());
        if (device == null) {
            log.info("Device not found");
            // We shouldn't be here!
            return 0;
        }
        try {
            String strMessage = "{messageType: \"" + message.getMessageType() + "\"";
            if (message.getPayload() != null) {
                strMessage += ", payload: " + message.getPayload();
            }
            strMessage += "}";

            MqttMessage mqttMessage = new MqttMessage(strMessage.getBytes());
            mqttMessage.setQos(2);
            String number = device.getOldNumber() == null ? device.getNumber() : device.getOldNumber();
            log.info("Message to send: topic " + number);
            log.info("Message to send: message" + mqttMessage);
            if (mqttDelay == 0) {
                this.client.publish(number, mqttMessage);
            } else {
                throttledSender.send(new MqttEnvelope(number, mqttMessage));
            }

        } catch (Exception e) {
            log.error("Error: ", e);
        }
        return 0;
    }
}
