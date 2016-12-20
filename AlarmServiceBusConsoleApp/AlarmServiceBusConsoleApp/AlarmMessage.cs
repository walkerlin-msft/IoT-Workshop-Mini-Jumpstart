using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace AlarmServiceBusConsoleApp
{
    class AlarmMessage
    {
        public string WinStartTime { get; set; }
        public string WinEndTime { get; set; }
        public string IoTHubDeviceID { get; set; }
        public string AvgLight { get; set; }
    }
}
