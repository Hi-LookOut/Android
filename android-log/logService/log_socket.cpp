#include <sys/socket.h>
#include <sys/un.h>
#include <unistd.h>
#include <errno.h>
#include <stddef.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <iostream>

#include "include/log_socket_head.h"

using namespace std;

#define SOCK_TYPE_MASK 0xf
#define ANDROID_RESERVED_SOCKET_PREFIX "../socket/"

log_socket_head::log_socket_head(){}

log_socket_head::~log_socket_head(){}

int log_socket_head::socket_make_sockaddr_un(const char *name, int namespaceId, 
        struct sockaddr_un *p_addr, socklen_t *alen)
{
    memset (p_addr, 0, sizeof (*p_addr));
    size_t namelen;
    namelen = strlen(name) + strlen(ANDROID_RESERVED_SOCKET_PREFIX);
            /* unix_path_max appears to be missing on linux */
    if (namelen > sizeof(*p_addr) 
            - offsetof(struct sockaddr_un, sun_path) - 1) {
        goto error;
    }

    strcpy(p_addr->sun_path, ANDROID_RESERVED_SOCKET_PREFIX);
    strcat(p_addr->sun_path, name);
    

    p_addr->sun_family = AF_LOCAL;
    *alen = namelen + offsetof(struct sockaddr_un, sun_path) + 1;
    return 0;
error:
    return -1;
}


int log_socket_head::socket_local_server_bind(int s, const char *name, int namespaceId)
{
    struct sockaddr_un addr;
    socklen_t alen;
    int n;
    int err;

    err = socket_make_sockaddr_un(name, namespaceId, &addr, &alen);

    if (err < 0) {
        return -1;
    }

    /* basically: if this is a filesystem path, unlink first */

    unlink(addr.sun_path);
    
    cout << "p_addr->sun_path:" <<  addr.sun_path<< endl;

    n = 1;
    setsockopt(s, SOL_SOCKET, SO_REUSEADDR, &n, sizeof(n));
    // bind
    if(bind(s, (struct sockaddr *) &addr, alen) < 0) {
        cout << "socket_local_server_bind()  failed" << endl;
        return -1;
    }

    return s;

}

int log_socket_head::socket_local_server(const char *name, int namespaceId, int type)
{
    int err;
    int s;

    s = socket(AF_LOCAL, type, 0);
    if (s < 0) {
       cout << "socket create failed" << endl;
       return -1;
    }
    

    err = socket_local_server_bind(s, name, namespaceId);

    if (err < 0) {
        close(s);
        cout << "socket bind failed" << endl;
        return -1;
    }


    if ((type & SOCK_TYPE_MASK) == SOCK_SEQPACKET) {
        int ret;
        cout << "SOCK_SEQPACKET socket listen" << endl;
        ret = listen(s, 4);

        if (ret < 0) {
            close(s);
            cout << "SOCK_SEQPACKET socket listen failed" << endl;
            return -1;
        }
    }

    return s;
}


int log_socket_head::startListener(int mSock, log_message* message) {
   if (mSock == -1) {
      printf("Failed to start unbound listener");
      errno = EINVAL;
      close(mSock);
      return -1;
   }

   mBufferMessage = message;
   mSockId = mSock;
   if (pthread_create(&mThread, nullptr, log_socket_head::threadStart, this)) {
      printf("pthread_create (%s)\n", strerror(errno));
      return -1;
   }


   return 0;
}


void log_socket_head::runListener(){
  while (true){   
        if (!onDataAvailable(mBufferMessage)){
            printf("onDataAvailable end \n");
            break;
        }
    }
}


void *log_socket_head::threadStart(void *obj) {
    log_socket_head* logSocket = reinterpret_cast<log_socket_head *>(obj);

    printf("thread start\n");
    
    logSocket->runListener();
    
    printf("thread end\n");

    pthread_exit(nullptr);
    return nullptr;
}
