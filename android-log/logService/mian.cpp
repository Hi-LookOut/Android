#include <iostream>
#include "include/LogReader.h"
#include "include/LogService.h"
using namespace std;



int main(int argc,char* argv[])
{
    cout<<"hello,service"<<endl;

    int sock = -2;
   
    log_message messageBuffer;

    LogService* logService = new LogService();
    sock = logService->socket_local_server("logdw",1,SOCK_DGRAM);
    logService->startListener(sock,&messageBuffer);


    LogReader* reader  = new LogReader();
    sock = reader->socket_local_server("logdr",1,SOCK_SEQPACKET);
    reader->startListener(sock,&messageBuffer);

    pthread_join(logService->mThread,nullptr);

    pthread_join(reader->mThread,nullptr);


    // cout<<"main messageBuffer.name = "<<messageBuffer.name<<endl;
    // cout<<"main messageBuffer.id = "<<messageBuffer.head->id<<endl;
    // cout<<"main messageBuffer.msg = "<<messageBuffer.msg<<endl;
    free(&messageBuffer);

    printf("service end\n");
    return 0;
}