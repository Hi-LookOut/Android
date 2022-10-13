#include <iostream>
#include <sys/socket.h>
#include <sys/un.h>
#include <unistd.h>

#include "include/logd_reader.h"

using namespace std;

extern struct android_log_transport_read logdLoggerRead;



int main(int argc,char* argv[])
{
    if (argc!=2){
        fprintf(stderr,"只能输入一个参数，当前输入的参数个是：%d\n",argc-1);
        return -1;
    }
    
    printf("viewer: 输入的参数是：%s\n", argv[1]);
    
    logdLoggerRead.available(100);

    if (logdLoggerRead.open()<0){
        fprintf(stderr,"logcat open logService failed\n");
        return -1;
    }
    
    if (logdLoggerRead.read(argv[1])<0){
        fprintf(stderr,"logcat read log failed\n");
        return -1;
    }

    return 0;
}