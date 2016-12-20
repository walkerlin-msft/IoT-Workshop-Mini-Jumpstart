using Newtonsoft.Json.Linq;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace AlarmServiceBusConsoleApp
{
    class C2DCommand
    {
        public const string COMMAND_LIGHT_WARNING = "COMMAND_LIGHT_WARNING";

        public string command { get; set; }
        public string value { get; set; }
        public string time { get; set; }        
    }

}
