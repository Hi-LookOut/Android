#include <iostream>
#include "include/logd_writer.h"
#include <sys/socket.h>
#include <sys/un.h>
#include <unistd.h>

using namespace std;



extern struct android_log_transport_write logdLoggerWrite;

int main(int argc,char* argv[])
{
    cout<<"hello,client"<<endl;

    // FILE* output;    
    if(!isatty(fileno(stdout))) {
      fprintf(stderr,"You are not a terminal\n");
      exit(1);
    }
    
    FILE* input;
    input = fopen("/dev/tty","r");
    if (!input){
      fprintf(stderr,"Unable to open /dev/tty\n");
      exit(1);
    }
    
    char inputStr[2048];
    printf("please enter the log content:\n");

    fgets(inputStr,2049,input);
    
    printf("*******************\n");
    fprintf(stdout,"the input content is: \n    %s\n",inputStr);
    printf("*******************\n");

    logdLoggerWrite.available(100);

    if (logdLoggerWrite.open()<0){
        fprintf(stderr,"client open logService failed\n");
        return -1;
    }

    struct msghdr hdr;
    struct iovec vec[3];
    char* tag  = "tag,";
    char* msg  = inputStr;

    android_log_header_t header;
    header.tid = 1;
    header.id =110;
    header.realtime = 2;

    vec[0].iov_base = (unsigned char*)&header;
    vec[0].iov_len = sizeof(header);
    vec[1].iov_base = (void*)tag;
    vec[1].iov_len = strlen(tag);
    vec[2].iov_base = (void*)msg;
    vec[2].iov_len = strlen(msg) +1;

    if ( logdLoggerWrite.write(vec,3)<0){
        fprintf(stderr,"client write log failed\n");
        return -1;
    }
    // logdLoggerWrite.close();

    return 0;
}