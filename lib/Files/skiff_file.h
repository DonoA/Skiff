#include <sys/stat.h>
#include <fcntl.h>
#include <unistd.h>
#include <stdint.h>

#include "../std/skiff_string.h"
#include "../std/skiff_list.h"

int32_t skiff_open_fd(skiff_string_t * fname)
{
    return open(fname->data, O_RDONLY);
}

void skiff_close_fd(int32_t fd)
{
    close(fd);
}

int32_t skiff_read_to_buffer(int32_t fd, skiff_list_t * buffer)
{
    int32_t read_bytes = read(fd, buffer->data, buffer->size);
    return read_bytes;
}

skiff_string_t * skiff_decode_bytes(skiff_list_t * bytes, int32_t len)
{
    skiff_string_static();
    skiff_string_t * str = skalloc(1, sizeof(skiff_string_t) + sizeof(char) * (len + 1));
    str->class_ptr = &skiff_string_interface;
    str->len = len;
    str->data = (char *) (str + 1);
    for (size_t i = 0; i < len; i++)
    {
        str->data[i] = ((char *) bytes->data)[i];
    }
    str->data[len] = 0;
    return str;
}