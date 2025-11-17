using UnityEngine;
using System;
using System.Collections.Generic;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using MQTTnet;
using MQTTnet.Client;
using MQTTnet.Client.Options; // v3.1.2'de bu yeterlidir

public class MqttController : MonoBehaviour
{
    public static event Action<string> OnMessageArrived;

    [Header("HiveMQ Cloud Connection Details")]
    public string brokerAddress = "69935d3a217e4cdb94eef5662762a511.s1.eu.hivemq.cloud";
    public int brokerPort = 8883;
    public string username = "mtnkdr";
    public string password = "3N772AHL0qSRp8";

    [Header("MQTT Topics")]
    public string topicToSubscribe = "updates/unity/device-status";

    private IMqttClient mqttClient;
    private readonly Queue<string> messageQueue = new Queue<string>();

    async void Start()
    {
        var mqttFactory = new MqttFactory();
        mqttClient = mqttFactory.CreateMqttClient();

        var mqttClientOptions = new MqttClientOptionsBuilder()
            .WithClientId(Guid.NewGuid().ToString())
            .WithTcpServer(brokerAddress, brokerPort)
            .WithCredentials(username, password)
            .WithTls(new MqttClientOptionsBuilderTlsParameters
            {
                UseTls = true,
                AllowUntrustedCertificates = true,
                IgnoreCertificateChainErrors = true,
                IgnoreCertificateRevocationErrors = true
            })
            .WithCleanSession()
            .Build();

        mqttClient.UseApplicationMessageReceivedHandler(e =>
        {
            string payload = Encoding.UTF8.GetString(e.ApplicationMessage.Payload);
            lock (messageQueue)
            {
                messageQueue.Enqueue(payload);
            }
        });

        try
        {
            await mqttClient.ConnectAsync(mqttClientOptions, CancellationToken.None);
            Debug.Log("[MQTT] Successfully connected to HiveMQ!");

            await mqttClient.SubscribeAsync(new MqttTopicFilterBuilder().WithTopic(topicToSubscribe).Build());
            Debug.Log($"[MQTT] Subscribed to topic: {topicToSubscribe}");
        }
        catch (Exception e)
        {
            Debug.LogError($"[MQTT] Connection Error: {e.Message}");
        }
    }

    void Update()
    {
        while (messageQueue.Count > 0)
        {
            string message = messageQueue.Dequeue();
            OnMessageArrived?.Invoke(message);
        }
    }

    private async void OnApplicationQuit()
    {
        if (mqttClient != null && mqttClient.IsConnected)
        {
            await mqttClient.DisconnectAsync();
            Debug.Log("[MQTT] Disconnected.");
        }
    }
}