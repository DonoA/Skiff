#include <stdlib.h>

#include "skiff_string.h"

skiff_string_t * skiff_int_to_string(int32_t i)
{
    char buf[13] = { 0 };
    sprintf(buf, "%d", i);
    skiff_string_t * str = skiff_string_new_0(0, buf);
    return str;
}