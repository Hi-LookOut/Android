#pragma once 

#include <sys/socket.h>

typedef struct __attribute__((__packed__)) {
  int id;
  int tid;
  int realtime;
} android_log_header_t;



struct log_message {
     char* name;
     android_log_header_t* head;
     char* msg;
};

class log_socket_head
{

private:
    log_message* mBufferMessage = nullptr;
    static void *threadStart(void *obj);
    void runListener();
    
protected:
    int mSockId;
    virtual bool onDataAvailable(log_message* message) = 0;

        
public:
        pthread_t mThread;
   

        int socket_make_sockaddr_un(const char *name, int namespaceId, 

        struct sockaddr_un *p_addr, socklen_t *alen);

        int socket_local_server_bind(int s, const char *name, int namespaceId);

        int socket_local_server(const char *name, int namespaceId, int type);
        
        int startListener(int mSock, log_message* msgBuffer);


        log_socket_head();
        ~log_socket_head();
};

