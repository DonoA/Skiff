#include <sys/stat.h>
#include <fcntl.h>
#include <unistd.h>
#include <stdint.h>

#include "../std/skiff_string.h"

int32_t skiff_open_fd(skiff_string_t * fname)
{
    return open(fname->data, 0);
}

void skiff_close_fd(int32_t fd)
{
    close(fd);
}