#include "log_socket_head.h"

class LogService : public log_socket_head{

protected:
    virtual bool onDataAvailable(log_message* message);

public:
    LogService();
    ~LogService();
};

