#include "log_socket_head.h"


class LogReader: public log_socket_head{
   protected:
       virtual bool onDataAvailable(log_message* message);

   public:
       LogReader();
       ~LogReader();
};




