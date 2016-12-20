using Microsoft.Azure.Devices;
using Microsoft.ServiceBus.Messaging;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using System;
using System.Collections.Generic;
using System.Configuration;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace AlarmServiceBusConsoleApp
{
    class Program
    {
        /* Service Bus */
        private const string QueueName = "cloud2device";// It's hard-coded for this workshop

        /* IoT Hub */
        private static ServiceClient _serviceClient;

        static void Main(string[] args)
        {   
            Console.WriteLine("Console App for Alarm Service Bus...");

            /* Load the settings from App.config */
            string serviceBusConnectionString = ConfigurationManager.AppSettings["ServiceBus.ConnectionString"];
            Console.WriteLine("serviceBusConnectionString={0}\n", serviceBusConnectionString);
            string iotHubConnectionString = ConfigurationManager.AppSettings["IoTHub.ConnectionString"];
            Console.WriteLine("iotHubConnectionString={0}\n", iotHubConnectionString);

            // Retrieve a Queue Client
            QueueClient queueClient = QueueClient.CreateFromConnectionString(serviceBusConnectionString, QueueName);

            // Retrieve a Service Client of IoT Hub
            _serviceClient = ServiceClient.CreateFromConnectionString(iotHubConnectionString);

            queueClient.OnMessage(message =>
            {
                Console.WriteLine("\n*******************************************************");
                string msg = message.GetBody<String>();
                try
                {
                    AlarmMessage alarmMessage = JsonConvert.DeserializeObject<AlarmMessage>(msg);

                    ProcessAlarmMessage(alarmMessage);

                }
                catch (Exception ex)
                {
                    Console.WriteLine("****  Exception=" + ex.Message);
                }


            });

            Console.ReadLine();
        }

        private static void ProcessAlarmMessage(AlarmMessage alarmMessage)
        {
            WriteHighlightedMessage(
                    alarmMessage.IoTHubDeviceID +
                    " - AvgLight=" + alarmMessage.AvgLight +
                    ", WinStartTime=" + alarmMessage.WinStartTime +
                    ", WinEndTime=" + alarmMessage.WinEndTime,
                    ConsoleColor.Yellow);

            C2DCommand c2dCommand = new C2DCommand();
            c2dCommand.command = C2DCommand.COMMAND_LIGHT_WARNING;
            c2dCommand.value = alarmMessage.AvgLight;
            c2dCommand.time = alarmMessage.WinEndTime;

            SendCloudToDeviceCommand(
                _serviceClient,
                alarmMessage.IoTHubDeviceID,
                c2dCommand).Wait();
        }

        private async static Task SendCloudToDeviceCommand(ServiceClient serviceClient, String deviceId, C2DCommand command)
        {
            var commandMessage = new Message(Encoding.ASCII.GetBytes(JsonConvert.SerializeObject(command)));
            await serviceClient.SendAsync(deviceId, commandMessage);
        }

        private static void WriteHighlightedMessage(string message, System.ConsoleColor color)
        {
            Console.ForegroundColor = color;
            Console.WriteLine(message);
            Console.ResetColor();
        }
    }
}
